package com.air.airspeed.vpn;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;

// created by liu 2020-01-10

public class CommonMethods {
    private static final String TAG = "CommonMethods";

    public static InetAddress ipIntToInet4Address(int ip) {
        byte[] ipAddress = new byte[4];
        writeInt(ipAddress, 0, ip);
        try {
            return Inet4Address.getByAddress(ipAddress);
        }catch (UnknownHostException e){
            e.printStackTrace();
            return null;
        }
    }

    public static String ipBytesToString(byte[] ip){
        return String.format("%s.%s.%s.%s", ip[0]&0x00FF, ip[1]&0x00FF,
                ip[2]&0x00FF, ip[3]&0x00FF);
    }

    public static short readShort(byte[] data, int offset) {
        int num = ((data[offset] & 0xFF) << 8 | data[offset+1] & 0xFF) & 0xFFFF;
        return (short) num;
    }

    //获取每一位的地址并将它们右移，或就是将每8位的地址整合在一起
    public static int readInt(byte[] data, int offset) {
        int num = ((data[offset] & 0xFF) << 24) |
                ((data[offset + 1] & 0xFF) << 16) |
                ((data[offset + 2] & 0xFF) << 8)  |
                (data[offset + 3] & 0xFF);
        return num;
    }

    public static void writeInt(byte[] data, int offset, long value) {
        data[offset] = (byte) (value >> 24);
        data[offset+1] = (byte) (value >> 16);
        data[offset+2] = (byte) (value >> 8);
        data[offset+3] = (byte) (value);
    }

    public static void writeShort(byte[] data, int offset, int value) {
        data[offset] = (byte) (value >> 8);
        data[offset+1] = (byte) (value & 0xFF);
    }

    //将IP细致转化为x.x.x.x的形式
    public static String ipLongToString(long ip) {
        return String.format("%s.%s.%s.%s", (ip >> 24) & 0x00FF, (ip>>16) & 0x00FF,
                (ip>>8) & 0xFF, ip & 0xFF);
    }

    //将x.x.x.x的IP地址转化为32位二进制数字地址
    public static long ipStringToLong(String ip) {
        String[] strings = ip.split("\\.");
        long num = (Long.parseLong(strings[0]) << 24) |
                (Long.parseLong(strings[1]) << 16) |
                (Long.parseLong(strings[2]) << 8) |
                (Long.parseLong(strings[3]));
        return num;
    }

    //计算校验和
    public static short checksum(long sum, byte[] buf,int offset, int len) {
        sum += getsum(buf, offset, len);
        while ((sum>>16) > 0) {
            sum = (sum & 0xFFFF) + (sum >> 16);
        }
        return (short)~sum;
    }

    public static long getsum(byte[] buf, int offset, int len) {
        long sum = 0;
        while (len > 1){
            sum += readShort(buf, offset) & 0xFFFF;
            offset +=2;
            len -= 2;
        }
        if (len > 0){
            sum += (buf[offset] & 0xFF) << 8;
        }
        return sum;
    }

    //计算IP包的校验和
    public static boolean ComputeIPChecksum(IPHeader ipHeader) {
        short oldCrc = ipHeader.getCrc();
        ipHeader.setCrc((short) 0);
        short newCrc = CommonMethods.checksum(0, ipHeader.mData,ipHeader.mOffset,ipHeader.getHeaderLength());
        ipHeader.setCrc(newCrc);
        return oldCrc == newCrc;
    }

    //计算TCP校验和，TCP伪首部+TCP首部+TCP数据
    //TCP检验和 = 整个TCP报文（不含检验和部分） +  源地址 + 目标地址 + 协议 + tcp报文长度
    public static boolean ComputeTCPChecksum(IPHeader ipHeader, TCPHeader tcpHeader) {
        ComputeIPChecksum(ipHeader);
        int ipData_len =  ipHeader.getDataLength();
        if (ipData_len < 0){
            return false;
        }
        long sum = getsum(ipHeader.mData, ipHeader.mOffset+ipHeader.offset_src_ip, 8);
        sum += ipHeader.getProtocol() & 0xFF;
        sum += ipData_len;
        short oldCrc = tcpHeader.getCrc();
        tcpHeader.setCrc((short) 0);
        short newCrc = checksum(sum, tcpHeader.mData, tcpHeader.mOffset, ipData_len);
        tcpHeader.setCrc(newCrc);
        return oldCrc == newCrc;
    }

    //计算伪首部长度
    /*
    public static long getPseudoHeadLength(IPacket iPacket, int len) {
        byte[] buf = iPacket.m_Data;
        int offset = iPacket.m_Offset + IPacket.SOURCE_IP_BIT;
        long sum = 0;
        //ip包中地址占8字节  计算地址
        int address = 8;
        while (address > 1) {
            sum += readShort(buf,offset) & 0xFFFF;
            offset +=2;
            address -=2;
        }
        if (address > 1) {   //可能会有剩余的字节
            sum += (buf[offset] & 0xFF) << 8;
        }
        //在此基础上计算TCP包长度
        sum += len;
        return  sum;
    }*/

    //计算UDP校验和
    //UDP检验和 = 整个UDP报文（不合检验和部分） +  源地址 + 目标地址 + 协议 + UDP报文长度
    public static boolean ComputeUDPChecksum(IPHeader ipHeader, UDPHeader udpHeader) {
        ComputeIPChecksum(ipHeader);
        int ipData_len = ipHeader.getDataLength();
        if (ipData_len < 0) {
            return false;
        }

        //计算伪首部和
        long sum = getsum(ipHeader.mData, ipHeader.mOffset + IPHeader.offset_src_ip, 8);
        sum += ipHeader.getProtocol() & 0xFF;
        sum += ipData_len;
        short oldCrc = udpHeader.getCrc();
        udpHeader.setCrc((short) 0);

        short newCrc = checksum(sum, udpHeader.mData, udpHeader.mOffset, ipData_len);

        udpHeader.setCrc(newCrc);
        return oldCrc == newCrc;
    }

}
