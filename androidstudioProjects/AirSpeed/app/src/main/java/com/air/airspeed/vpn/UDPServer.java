package com.air.airspeed.vpn;

import android.net.VpnService;
import android.util.Log;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

//created by liu 2020-01-10

public class UDPServer implements Runnable{
    private String TAG = UDPServer.class.getSimpleName();
    private VpnService vpnService;
    private ConcurrentLinkedQueue<Packet> outputQueue;
    private Selector selector;

    private boolean isClose = false;

    private static final int MAX_UDP_CACHE_SIZE = 50;
    private final MyLRUCache<Integer, UDPTunnel> udpConnections =
            new MyLRUCache<>(MAX_UDP_CACHE_SIZE, new MyLRUCache.CleanupCallback<UDPTunnel>() {
                @Override
                public void cleanUp(UDPTunnel udpTunnel) {
                    udpTunnel.close();
                }

            });


    public void start() {
        Thread thread = new Thread(this, "UDPServer");
        thread.start();
    }

    public UDPServer(VpnService vpnService, ConcurrentLinkedQueue<Packet> outputQueue) {
        try {
            selector = Selector.open();
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.vpnService = vpnService;
        this.outputQueue = outputQueue;
        this.selector = selector;

    }


    public void processUDPPacket(Packet packet,int portKey) {
        UDPTunnel udpConn = getUDPConn(portKey);
        if (udpConn == null) {
            udpConn = new UDPTunnel(vpnService, selector, this, packet, outputQueue,portKey);
            putUDPConn(portKey, udpConn);
            udpConn.initConnection();
        } else {
            udpConn.processPacket(packet);
        }
    }


    public void closeAllUDPConn() {
        synchronized (udpConnections) {
            Iterator<Map.Entry<Integer, UDPTunnel>> it = udpConnections.entrySet().iterator();
            while (it.hasNext()) {
                it.next().getValue().close();
                it.remove();
            }
        }
    }


    public void closeUDPConn(UDPTunnel connection) {
        synchronized (udpConnections) {
            connection.close();
            udpConnections.remove(connection.getPortKey());
        }
    }

    public UDPTunnel getUDPConn(int portKey) {
        synchronized (udpConnections) {
            return udpConnections.get(portKey);
        }
    }

    void putUDPConn(int ipAndPort, UDPTunnel connection) {
        synchronized (udpConnections) {
            udpConnections.put(ipAndPort, connection);
        }

    }

    @Override
    public void run() {
        try {
            while (true) {
                int select = selector.select();
                if (select == 0) {
                    Thread.sleep(5);
                }
                Iterator<SelectionKey> keyIterator = selector.selectedKeys().iterator();
                while (keyIterator.hasNext()) {
                    SelectionKey key = keyIterator.next();
                    if (key.isValid()) {
                        try {
                            Object attachment = key.attachment();
                            if (attachment instanceof KeyHandler) {
                                ((KeyHandler) attachment).onKeyReady(key);
                            }

                        } catch (Exception ex) {
                            ex.printStackTrace(System.err);
                            Log.e(TAG,"TcpProxyServer iterate SelectionKey catch an exception: "+ ex);
                        }
                    }
                    keyIterator.remove();
                }


            }
        } catch (Exception e) {
            e.printStackTrace(System.err);
            Log.e(TAG,"TcpProxyServer catch an exception: "+ e);
        } finally {
            this.stop();
            Log.i(TAG,"TcpServer thread exited.");
        }
    }

    private void stop() {
        try {
            selector.close();
            selector = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
