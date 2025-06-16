package com.air.airspeed.vpn;

import android.util.Log;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ConcurrentLinkedQueue;

//created by liu 2020-01-10

public abstract class TcpTunnel implements KeyHandler {
    private static String TAG = "TcpTunnel";

    public static long sessionCount;
    protected InetSocketAddress mDestAddress;
    /**
     * 自己的Channel
     */

    private SocketChannel mInnerChannel;
    /**
     * 发送数据缓存
     */

    private Selector mSelector;
    /**
     * http报文
     */
    private boolean isHttpsRequest = false;
    /**
     * 与外网的通信两个Tunnel负责，一个负责Apps与TCP代理服务器的通信，一个负责TCP代理服务器
     * 与外网服务器的通信，Apps与外网服务器的数据交换靠这两个Tunnel来进行
     */
    private TcpTunnel mBrotherTunnel;
    private boolean mDisposed;
    private InetSocketAddress mServerEP;
    short portKey;
    ConcurrentLinkedQueue<ByteBuffer> needWriteData = new ConcurrentLinkedQueue<>();

    public TcpTunnel(SocketChannel innerChannel, Selector selector) {
        mInnerChannel = innerChannel;
        mSelector = selector;
        sessionCount++;
    }

    public TcpTunnel(InetSocketAddress serverAddress, Selector selector, short portKey) throws IOException {
        SocketChannel innerChannel = SocketChannel.open();
        innerChannel.configureBlocking(false);
        this.mInnerChannel = innerChannel;
        this.mSelector = selector;
        this.mServerEP = serverAddress;
        this.portKey = portKey;
        sessionCount++;
    }

    @Override
    public void onKeyReady(SelectionKey key) {
        if (key.isReadable()) {
            onReadable(key);
        } else if (key.isWritable()) {
            onWritable(key);
        } else if (key.isConnectable()) {
            onConnectable();
        }
    }

    /**
     * 方法调用次序：
     * connect() -> onConnectable() -> onConnected()[子类实现]
     * beginReceived() ->  onReadable() -> afterReceived()[子类实现]
     */

    protected abstract void onConnected() throws Exception;

    protected abstract boolean isTunnelEstablished();

    protected abstract void beforeSend(ByteBuffer buffer) throws Exception;

    protected abstract void afterReceived(ByteBuffer buffer) throws Exception;

    protected abstract void onDispose();

    public void setBrotherTunnel(TcpTunnel brotherTunnel) {
        this.mBrotherTunnel = brotherTunnel;
    }


    public void connect(InetSocketAddress destAddress) throws Exception {
        //保护socket不走VPN
        if (VpnServiceHelper.protect(mInnerChannel.socket())) {
            mDestAddress = destAddress;
            //注册连接事件
            mInnerChannel.register(mSelector, SelectionKey.OP_CONNECT, this);
            mInnerChannel.connect(mServerEP);
            Log.i(TAG,"Connecting to "+mServerEP);
        } else {
            throw new Exception("VPN protect socket failed.");
        }
    }

    public void onConnectable() {
        try {
            if (mInnerChannel.finishConnect()) {
                //通知子类TCP已连接，子类可以根据协议实现握手等
                onConnected();
                Log.i(TAG,"Connected to "+ mServerEP);
            } else {
                Log.e(TAG,"Connect to failed. "+mServerEP);
                this.dispose();
            }
        } catch (Exception e) {
           e.printStackTrace(System.err);
           Log.e(TAG,"Connect to "+mServerEP+" failed: "+e);
            this.dispose();
        }
    }

    protected void beginReceived() throws Exception {
        if (mInnerChannel.isBlocking()) {
            mInnerChannel.configureBlocking(false);
        }
        mSelector.wakeup();
        //注册读事件
        mInnerChannel.register(mSelector, SelectionKey.OP_READ, this);
    }

    public void onReadable(SelectionKey key) {
        try {
            ByteBuffer buffer = ByteBuffer.allocate(LocalVpnService.MUTE_SIZE);
            buffer.clear();
            int bytesRead = mInnerChannel.read(buffer);
            if (bytesRead > 0) {
                buffer.flip();
                //先让子类处理，例如解密数据
                afterReceived(buffer);

                sendToBrother(key, buffer);

            } else if (bytesRead < 0) {

                this.dispose();
            }
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
            Log.e(TAG,"onReadable catch an exception: "+ex);
            this.dispose();
        }
    }



    protected void sendToBrother(SelectionKey key, ByteBuffer buffer) throws Exception {
        //将读到的数据，转发给兄弟
        if (isTunnelEstablished() && buffer.hasRemaining()) {
            //发送之前，先让子类处理，例如做加密等。
            //    mBrotherTunnel.beforeSend(buffer);
            mBrotherTunnel.getWriteDataFromBrother(buffer);

        }
    }

    private void getWriteDataFromBrother(ByteBuffer buffer) {
        //如果没有数据尝试直接写
        if (buffer.hasRemaining() && needWriteData.size() == 0) {

            int writeSize = 0;
            try {
                writeSize = write(buffer);
            } catch (Exception e) {
                writeSize = 0;
                e.printStackTrace();
            }
            if (writeSize > 0) {
                return;
            }
        }
        needWriteData.offer(buffer);
        try {
            mSelector.wakeup();
            mInnerChannel.register(mSelector, SelectionKey.OP_READ | SelectionKey.OP_WRITE, this);
        } catch (ClosedChannelException e) {
            e.printStackTrace();
        }
    }

    protected int write(ByteBuffer buffer) throws Exception {
        int byteSendSum = 0;
        beforeSend(buffer);
        while (buffer.hasRemaining()) {
            int byteSent = mInnerChannel.write(buffer);
            byteSendSum += byteSent;
            if (byteSent == 0) {
                break; //不能再发送了，终止循环
            }
        }
        return byteSendSum;

    }


    public void onWritable(SelectionKey key) {
        try {
            //发送之前，先让子类处理，例如做加密等
            ByteBuffer mSendRemainBuffer = needWriteData.poll();
            if (mSendRemainBuffer == null) {
                return;
            }

            write(mSendRemainBuffer);
            if (needWriteData.size() == 0) {
                try {
                    mSelector.wakeup();
                    mInnerChannel.register(mSelector, SelectionKey.OP_READ, this);
                } catch (ClosedChannelException e) {
                    e.printStackTrace();
                }

            }
        } catch (Exception ex) {
           ex.printStackTrace(System.err);
           Log.e(TAG,"onWritable catch an exception: "+ ex);
           this.dispose();
        }
    }

    protected void onTunnelEstablished() throws Exception {
        this.beginReceived(); //开始接收数据
        mBrotherTunnel.beginReceived(); //兄弟也开始接收数据吧
    }

    public void dispose() {
        disposeInternal(true);
    }

    void disposeInternal(boolean disposeBrother) {
        if (!mDisposed) {
            try {
                mInnerChannel.close();
            } catch (Exception ex) {
                ex.printStackTrace(System.err);
                Log.e(TAG,"InnerChannel close catch an exception: "+ ex);
            }

            if (mBrotherTunnel != null && disposeBrother) {
                //把兄弟的资源也释放了
                mBrotherTunnel.disposeInternal(false);
            }

            mInnerChannel = null;
            mSelector = null;
            mBrotherTunnel = null;
            mDisposed = true;
            sessionCount--;

            onDispose();
            NatSessionManager.removeSession(portKey);
        }
    }

    public void setIsHttpsRequest(boolean isHttpsRequest) {
        this.isHttpsRequest = isHttpsRequest;
    }

    public boolean isHttpsRequest() {
        return isHttpsRequest;
    }
}
