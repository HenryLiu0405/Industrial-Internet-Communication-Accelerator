package com.air.airspeed.vpn;
import android.annotation.TargetApi;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

//created by liu 2020-01-10

public class TcpProxyServer implements Runnable{
    private static final String TAG = "TcpProxyServer";
    public boolean Stopped;
    public int port;

    Selector mSelector;
    ServerSocketChannel mServerSocketChannel;
    Thread mServerThread;

    public TcpProxyServer(int port) throws IOException {
        try{
            this.mSelector = Selector.open();
        }catch (IOException e){
            e.printStackTrace();
        }


        this.mServerSocketChannel = ServerSocketChannel.open();
        this.mServerSocketChannel.configureBlocking(false);
        this.mServerSocketChannel.socket().bind(new InetSocketAddress(port));
        this.mServerSocketChannel.register(mSelector, SelectionKey.OP_ACCEPT);
        this.port =  mServerSocketChannel.socket().getLocalPort();

        Log.i(TAG,"AsyncTcpServer listen on: "+ mServerSocketChannel.socket().getLocalSocketAddress()
                +" "+this.port);
    }

    /**
     * 启动TcpProxyServer线程
     */
    public void start() {
        mServerThread = new Thread(this, "TcpProxyServerThread");
        mServerThread.start();
        Log.d(TAG,"TcpProxyServerThread is start");
    }

    public void stop() {
        this.Stopped = true;
        if (mSelector != null) {
            try {
                mSelector.close();
                mSelector = null;
            } catch (Exception ex) {
                Log.e(TAG,"TcpProxyServer mSelector.close() catch an exception1: " + ex);
                ex.printStackTrace();
            }
        }

        if (mServerSocketChannel != null) {
            try {
                mServerSocketChannel.close();
                mServerSocketChannel = null;
            } catch (Exception ex) {
                ex.printStackTrace(System.err);
                Log.e(TAG,"TcpProxyServer mServerSocketChannel.close() catch an exception2: "+ex);
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.N)
    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void run() {
        Log.d(TAG,"I go into running...");
        try {
            while (true) {
                Log.d(TAG,"go into lisenting...");
                int select = mSelector.select(1000);
                if (select == 0) {
                    Thread.sleep(100);
                    continue;
                }
                Set<SelectionKey> selectionKeys = mSelector.selectedKeys();
                Log.d(TAG,"keys is: "+String.valueOf(selectionKeys.size()));
                if (selectionKeys == null) {
                    continue;
                }

                Iterator<SelectionKey> keyIterator = mSelector.selectedKeys().iterator();
                while (keyIterator.hasNext()) {
                    Log.d(TAG,"there is keys");
                    SelectionKey key = keyIterator.next();
                    if (key.isValid()) {
                        try {
                            if (key.isAcceptable()) {
                                Log.d(TAG, "isAcceptable");
                                onAccepted(key);
                            } else {
                                Object attachment = key.attachment();
                                if (attachment instanceof KeyHandler) {
                                    ((KeyHandler) attachment).onKeyReady(key);
                                }
                            }

                        } catch (Exception ex) {
                            ex.printStackTrace(System.err);
                            Log.e(TAG,"udp iterate SelectionKey catch an exception: "+ex);
                        }
                    }
                    keyIterator.remove();
                }


            }
        } catch (Exception e) {
            e.printStackTrace(System.err);
            Log.e(TAG,"TcpProxyServer catch an exception: "+e);
        } finally {
            this.stop();
            Log.i(TAG,"TcpProxyServer thread exited.");
        }
    }

    InetSocketAddress getDestAddress(SocketChannel localChannel) {
        short portKey = (short) localChannel.socket().getPort();
        NatSession session = NatSessionManager.getSession(portKey);
        if (session != null) {
            return new InetSocketAddress(localChannel.socket().getInetAddress(), session.remotePort & 0xFFFF);
        }
        return null;
    }

    void onAccepted(SelectionKey key) {
        TcpTunnel localTunnel = null;
        try {
            SocketChannel localChannel = mServerSocketChannel.accept();
            localTunnel = TunnelFactory.wrap(localChannel, mSelector);
            short portKey = (short) localChannel.socket().getPort();
            InetSocketAddress destAddress = getDestAddress(localChannel);
            if (destAddress != null) {
                TcpTunnel remoteTunnel = TunnelFactory.createTunnelByConfig(destAddress, mSelector, portKey);
                //关联兄弟
                remoteTunnel.setIsHttpsRequest(localTunnel.isHttpsRequest());
                remoteTunnel.setBrotherTunnel(localTunnel);
                localTunnel.setBrotherTunnel(remoteTunnel);
                //开始连接
                remoteTunnel.connect(destAddress);
            }
        } catch (Exception ex) {
           ex.printStackTrace(System.err);
           Log.e(TAG,"TcpProxyServer onAccepted catch an exception: "+ ex);
            if (localTunnel != null) {
                localTunnel.dispose();
            }
        }
    }

}
