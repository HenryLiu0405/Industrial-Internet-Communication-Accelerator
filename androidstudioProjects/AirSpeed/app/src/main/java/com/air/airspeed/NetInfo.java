package com.air.airspeed;

public class NetInfo {
    private int uid;
    private int type;   //协议类型  TCP：0  TCP6：1 UDP:2  UDP6: 3
    private int localport;
    private long localip;
    private String localaddress;
    private int remoteport;
    private long remoteip;
    private String remoteaddress;
    private long datalength;
    private int isspeed;  // 0:not speed;  1: speed
    private long deltalength;
    private int netcount;

    private String token;
    private String internetIP;
    private int operator;
    private int socketstate;
    /*  00  “ERROR_STATUS”,
        01  “TCP_ESTABLISHED”,
        02  “TCP_SYN_SENT”,
        03  “TCP_SYN_RECV”,
        04  “TCP_FIN_WAIT1″,
        05  “TCP_FIN_WAIT2″,
        06  “TCP_TIME_WAIT”,
        07  “TCP_CLOSE”,
        08  “TCP_CLOSE_WAIT”,
        09  “TCP_LAST_ACK”,
        0A  “TCP_LISTEN”,
        0B  “TCP_CLOSING”,*/

    public int getUid() {
        return uid;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getLocalPort() {
        return localport;
    }

    public void setLocalPort(int localport) {
        this.localport = localport;
    }

    public long getLocalIp() {
        return localip;
    }

    public void setLocalIp(long localip) {
        this.localip = localip;
    }
    public String getLocalAddress() {
        return localaddress;
    }
    public void setLocalAddress(String localaddress) {
        this.localaddress = localaddress;
    }

    public int getRemotePort() {
        return remoteport;
    }

    public void setRemotePort(int remoteport) {
        this.remoteport = remoteport;
    }

    public long getRemoteIp() {
        return remoteip;
    }

    public void setRemoteIp(long remoteip) {
        this.remoteip = remoteip;
    }
    public String getRemoteAddress() {
        return remoteaddress;
    }
    public void setRemoteAddress(String remoteaddress) {
        this.remoteaddress = remoteaddress;
    }

    public long getDataLength() {
        return datalength;
    }

    public void setDataLength(long datalength) {
        this.datalength = datalength;
    }

    public int getIsspeed() {
        return isspeed;
    }

    public void setIsspeed(int isspeed) {
        this.isspeed = isspeed;
    }

    public long getDeltalength(){
        return deltalength;
    }
    public void setDeltalength(long deltalength) {
        this.deltalength = deltalength;
    }
    public int getSocketstate(){
        return socketstate;
    }
    public void setSocketstate(int socketstate) {
        this.socketstate = socketstate;
    }
    public int getNetCount() {
        return netcount;
    }
    public void setNetCount(int netcount) {
        this.netcount = netcount;
    }
    public void setToken(String token) {
        this.token = token;
    }
    public String getToken() {
        return token;
    }
    public void setInternetIP(String internetIP) {
        this.internetIP = internetIP;
    }
    public String getInternetIP() {
        return internetIP;
    }
    public void setOperator(int operator){
        this.operator = operator;
    }
    public int getOperator() {
        return operator;
    }

    //以下适配新增

}
