package com.air.airspeed.vpn;

//created by liu 2019-12-16 路由表 保存源端口 目标端口 目标IP

import android.os.Build;
import android.support.annotation.RequiresApi;

import com.air.airspeed.AppInfo;

import java.io.Serializable;

public class NatSession {
    public static final String TCP = "TCP";
    public static final String UDP = "UDP";
    public String type;
    public String ipAndPort;
    public long remoteIP;
    public int remotePort;
    public String remoteHost;   //远端主机名
    public long localIP;
    public int localPort;
    public int bytesSent;
    public int packetSent;   //记录数据包发送包数，例如tcp数据 前两个数据包是和服务器握手使用的数据包
    public long receiveByteNum;
    public long receivePacketNum;
    public long lastRefreshTime;
    public boolean isHttpsSession;
    public String requestUrl;
    public String pathUrl;
    public String method;
    public long connectionStartTime = System.currentTimeMillis();
    public long vpnStartTime;
    public boolean isHttp;
    public AppInfo appInfo;

    public int Protocol;    //记录该流的协议号
    public int PacketRev;   //记录接收到的数据包数

    @Override
    public String toString() {
        return String.format("%s/%s:%d packet: %d", remoteHost, CommonMethods.ipLongToString(remoteIP),
                remotePort & 0xFFFF, packetSent);
    }

    public String getUniqueName() {
        String uinID = ipAndPort + connectionStartTime;
        return String.valueOf(uinID.hashCode());
    }

    public void refreshIpAndPort() {
        long remoteIPStr1 = (remoteIP & 0XFF000000) >> 24 & 0XFF;
        long remoteIPStr2 = (remoteIP & 0X00FF0000) >> 16;
        long remoteIPStr3 = (remoteIP & 0X0000FF00) >> 8;
        long remoteIPStr4 = remoteIP & 0X000000FF;
        String remoteIPStr = "" + remoteIPStr1 + ":" + remoteIPStr2 + ":" + remoteIPStr3 + ":" + remoteIPStr4;
        ipAndPort = type + ":" + remoteIPStr + ":" + remotePort + " " + ((int) localPort & 0XFFFF);
    }

    public String getType() {
        return type;
    }

    public String getIpAndPort() {
        return ipAndPort;
    }

    public long getRemoteIP() {
        return remoteIP;
    }

    public int getRemotePort() {
        return remotePort;
    }

    public String getRemoteHost() {
        return remoteHost;
    }

    public int getLocalPort() {
        return localPort;
    }

    public int getBytesSent() {
        return bytesSent;
    }

    public int getPacketSent() {
        return packetSent;
    }

    public long getReceiveByteNum() {
        return receiveByteNum;
    }

    public long getReceivePacketNum() {
        return receivePacketNum;
    }

    public long getRefreshTime() {
        return lastRefreshTime;
    }

    public boolean isHttpsSession() {
        return isHttpsSession;
    }

    public String getRequestUrl() {
        return requestUrl;
    }

    public String getPathUrl() {
        return pathUrl;
    }

    public String getMethod() {
        return method;
    }



    public long getConnectionStartTime() {
        return connectionStartTime;
    }

    public long getVpnStartTime() {
        return vpnStartTime;
    }

    public static class NatSesionComparator implements java.util.Comparator<NatSession> {

        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        @Override
        public int compare(NatSession o1, NatSession o2) {
            if (o1 == o2) {
                return 0;
            }
            return Long.compare(o2.lastRefreshTime, o1.lastRefreshTime);
        }
    }
}
