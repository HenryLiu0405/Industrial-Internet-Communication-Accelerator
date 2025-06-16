package com.air.airspeed.vpn;

//created by liu 2020-01-10

import java.net.PortUnreachableException;

public class UDPHeader {
    public static final short offset_src_port = 0;   // 端口号
    public static final short offset_dest_port = 2; //目的端口号
    public static final short offset_tlen = 4;  //数据报长度
    public static final short offset_crc = 6;  //校验和

    public byte[] mData;
    public int mOffset;

    public UDPHeader(byte[] data, int offset) {
        this.mData = data;
        this.mOffset = offset;
    }

    public int getSourcePort() {
        return CommonMethods.readShort(mData, mOffset + offset_src_port);
    }

    public int getDestinationPort() {
        return CommonMethods.readShort(mData, mOffset + offset_dest_port);
    }

    public int getTotalLength() {
        return CommonMethods.readShort(mData, mOffset + offset_tlen) & 0xFFFF;
    }

    public short getCrc() {
        return CommonMethods.readShort(mData, mOffset + offset_crc);
    }

    public void setSourcePort(int port) {
        CommonMethods.writeShort(mData, mOffset + offset_src_port, (short) port);
    }

    public void setDestinationPort(int port) {
        CommonMethods.writeShort(mData, mOffset + offset_dest_port, (short) port);
    }

    public void setTotalLength(int length){
        CommonMethods.writeShort(mData, mOffset+offset_tlen,(short)length);
    }

    public void setCrc(short value){
        CommonMethods.writeShort(mData,mOffset+offset_crc,value);
    }

    @Override
    public String toString() {
        return String.format("%d->%d", getSourcePort()&0xFFFF, getDestinationPort()&0xFFFF);
    }
}
