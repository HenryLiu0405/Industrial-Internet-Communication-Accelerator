package com.air.airspeed.vpn;

import android.os.Handler;
import android.os.Looper;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.Selector;

//created by liu 2020-01-10

public class RemoteTcpTunnel extends RawTcpTunnel{
    NatSession session;
    private final Handler handler;

    public RemoteTcpTunnel(InetSocketAddress serverAddress, Selector selector, short portKey) throws IOException {
        super(serverAddress, selector, portKey);
        session = NatSessionManager.getSession(portKey);
        handler = new Handler(Looper.getMainLooper());

    }


    @Override
    protected void afterReceived(ByteBuffer buffer) throws Exception {
        super.afterReceived(buffer);
        refreshSessionAfterRead(buffer.limit());

    }

    @Override
    protected void beforeSend(ByteBuffer buffer) throws Exception {
        super.beforeSend(buffer);
        refreshAppInfo();
    }

    private void refreshAppInfo() {
        if (session.appInfo != null) {
            return;
        }
        if (PortHostService.getInstance() != null) {
            ThreadProxy.getInstance().execute(new Runnable() {
                @Override
                public void run() {
                    PortHostService.getInstance().refreshSessionInfo();
                }
            });
        }
    }

    private void refreshSessionAfterRead(int size) {

        session.receivePacketNum++;
        session.receiveByteNum += size;

    }

    @Override
    protected void onDispose() {
        super.onDispose();
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
    }
}
