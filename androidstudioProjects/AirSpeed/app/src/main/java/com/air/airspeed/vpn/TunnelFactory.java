package com.air.airspeed.vpn;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

//created by liu 2020-01-10

public class TunnelFactory {
    public static TcpTunnel wrap(SocketChannel channel, Selector selector) {
        TcpTunnel tunnel = new RawTcpTunnel(channel, selector);
        NatSession session = NatSessionManager.getSession((short) channel.socket().getPort());
        if (session != null) {
            tunnel.setIsHttpsRequest(session.isHttpsSession);
        }
        return tunnel;
    }

    public static TcpTunnel createTunnelByConfig(InetSocketAddress destAddress, Selector selector, short portKey) throws IOException {
        return new RemoteTcpTunnel(destAddress, selector,portKey);
    }
}
