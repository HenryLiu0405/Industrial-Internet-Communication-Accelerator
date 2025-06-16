package com.air.airspeed.vpn;

import android.net.VpnService;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

//created by liu 2020-01-10

public class UDPTunnel implements KeyHandler{
    private static final String TAG = UDPTunnel.class.getSimpleName();
    private final VpnService vpnService;
    private final Selector selector;
    private final UDPServer vpnServer;
    private final Queue<Packet> outputQueue;

    private Packet referencePacket;
    private SelectionKey selectionKey;

    private DatagramChannel channel;
    private final ConcurrentLinkedQueue<Packet> toNetWorkPackets = new ConcurrentLinkedQueue<>();
    private static final int HEADER_SIZE = Packet.IP4_HEADER_SIZE + Packet.UDP_HEADER_SIZE;
    private int portKey;
    String ipAndPort;
    private final NatSession session;
    private final Handler handler;

    public UDPTunnel(VpnService vpnService, Selector selector, UDPServer vpnServer, Packet packet, Queue<Packet> outputQueue, int portKey) {
        this.vpnService = vpnService;
        this.selector = selector;
        this.vpnServer = vpnServer;
        this.referencePacket = packet;
        ipAndPort = packet.getIpAndPort();
        this.outputQueue = outputQueue;
        this.portKey = portKey;
        session = NatSessionManager.getSession(portKey);
        handler = new Handler(Looper.getMainLooper());
    }


    private void processKey(SelectionKey key) {
        if (key.isWritable()) {
            processSend();
        } else if (key.isReadable()) {
            processReceived();
        }
        updateInterests();
    }

    private void processReceived() {
        Log.d(TAG, "processReceived:" + ipAndPort);
        ByteBuffer receiveBuffer = SocketUtils.getByteBuffer();
        // Leave space for the header
        receiveBuffer.position(HEADER_SIZE);
        int readBytes = 0;
        try {
            readBytes = channel.read(receiveBuffer);
        } catch (Exception e) {
            Log.d(TAG, "failed to read udp datas ");
            vpnServer.closeUDPConn(this);
            return;
        }
        if (readBytes == -1) {
            vpnServer.closeUDPConn(this);
            Log.d(TAG, "read  data error :" + ipAndPort);
        } else if (readBytes == 0) {
            Log.d(TAG, "read no data :" + ipAndPort);
        } else {
            Log.d(TAG, "read readBytes:" + readBytes + "ipAndPort:" + ipAndPort);
            Packet newPacket = referencePacket.duplicated();
            newPacket.updateUDPBuffer(receiveBuffer, readBytes);
            receiveBuffer.position(HEADER_SIZE + readBytes);
            outputQueue.offer(newPacket);
            Log.d(TAG, "read  data :readBytes:" + readBytes + "ipAndPort:" + ipAndPort);
            session.receivePacketNum++;
            session.receiveByteNum += readBytes;
            session.lastRefreshTime = System.currentTimeMillis();
        }
    }

    private void saveData(byte[] array, int saveSize, boolean isRequest) {

    }

    private void processSend() {
        Log.d(TAG, "processWriteUDPData " + ipAndPort);
        Packet toNetWorkPacket = getToNetWorkPackets();
        if (toNetWorkPacket == null) {
            Log.d(TAG, "write data  no packet ");
            return;
        }
        try {
            ByteBuffer payloadBuffer = toNetWorkPacket.backingBuffer;
            session.packetSent++;
            int sendSize = payloadBuffer.limit() - payloadBuffer.position();
            session.bytesSent += sendSize;


            session.lastRefreshTime = System.currentTimeMillis();
            while (payloadBuffer.hasRemaining()) {
                channel.write(payloadBuffer);
            }


        } catch (IOException e) {
            Log.w(TAG, "Network write error: " + ipAndPort, e);
            vpnServer.closeUDPConn(this);
        }
    }

    public void initConnection() {
        Log.d(TAG, "init  ipAndPort:" + ipAndPort);
        InetAddress destinationAddress = referencePacket.ip4Header.destinationAddress;
        int destinationPort = referencePacket.udpHeader.destinationPort;
        try {
            channel = DatagramChannel.open();
            vpnService.protect(channel.socket());
            channel.configureBlocking(false);
            channel.connect(new InetSocketAddress(destinationAddress, destinationPort));
            selector.wakeup();
            selectionKey = channel.register(selector,
                    SelectionKey.OP_READ, this);
        } catch (IOException e) {
            SocketUtils.closeResources(channel);
            return;
        }
        referencePacket.swapSourceAndDestination();
        addToNetWorkPacket(referencePacket);
    }

    public void processPacket(Packet packet) {
        addToNetWorkPacket(packet);
        updateInterests();
    }

    public void close() {
        try {
            if (selectionKey != null) {
                selectionKey.cancel();
            }
            if (channel != null) {
                channel.close();
            }

            if (session.appInfo == null && PortHostService.getInstance() != null) {
                PortHostService.getInstance().refreshSessionInfo();
            }
            //需要延迟一秒在保存 等到app信息完全刷新
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    ThreadProxy.getInstance().execute(new Runnable() {
                        @Override
                        public void run() {
                            if (session.receiveByteNum == 0 && session.bytesSent == 0) {
                                return;
                            }
                        }
                    });
                }
            }, 1000);
        } catch (Exception e) {
            Log.w(TAG, "error to close UDP channel IpAndPort" + ipAndPort + ",error is " + e.getMessage());
        }

    }


    Packet getToNetWorkPackets() {
        return toNetWorkPackets.poll();
    }

    void addToNetWorkPacket(Packet packet) {
        toNetWorkPackets.offer(packet);
        updateInterests();
    }

    DatagramChannel getChannel() {
        return channel;
    }

    void updateInterests() {
        int ops;
        if (toNetWorkPackets.isEmpty()) {
            ops = SelectionKey.OP_READ;
        } else {
            ops = SelectionKey.OP_WRITE | SelectionKey.OP_READ;
        }
        selector.wakeup();
        selectionKey.interestOps(ops);
        Log.d(TAG, "updateInterests ops:" + ops + ",ip" + ipAndPort);
    }

    Packet getReferencePacket() {
        return referencePacket;
    }


    @Override
    public void onKeyReady(SelectionKey key) {
        processKey(key);
    }

    public int getPortKey() {
        return portKey;
    }
}
