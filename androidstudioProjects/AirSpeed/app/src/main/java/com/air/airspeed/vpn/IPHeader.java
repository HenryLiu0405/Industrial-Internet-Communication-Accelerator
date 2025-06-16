package com.air.airspeed.vpn;

//created by liu 2020-01-10

public class IPHeader {
    public static final short IP = 0x0800;
    public static final byte ICMP = 1;
    public static final byte TCP = 6;  //6: TCP协议号
    public static final byte UDP = 17; //17: UDP协议号
    public static final byte offset_proto = 9; //9：8位协议偏移
    public static final int offset_src_ip = 12; //12：源ip地址偏移
    public static final int offset_dest_ip = 16; //16：目标ip地址偏移
    static final byte offset_ver_ihl = 0; //0: 版本号（4bits） + 首部长度（4bits）
    static final byte offset_tos = 1; //1：服务类型偏移
    static final short offset_tlen = 2; //2：总长度偏移
    static final short offset_identification = 4; //4：16位标识符偏移
    static final short offset_flags_fo = 6; //6：标志（3bits）+ 片偏移（13bits）
    static final byte offset_ttl = 8; //8：生存时间偏移
    static final short offset_crc = 10; //10：首部校验和偏移
    static final int offset_op_pad = 20; //20：选项 + 填充

    public byte[] mData;
    public int mOffset;

    public IPHeader(byte[] data, int offset) {
        mData = data;
        mOffset = offset;
    }

    public void Default() {
        setHeaderLength(20);
        setTos((byte) 0);
        setTotalLength(0);
        setIdentification(0);
        setFlagsAndOffset((short) 0);
        setTTL((byte) 64);
    }

    public int getDataLength() {
        return this.getTotalLength() - this.getHeaderLength();
    }

    public int getHeaderLength() {
        return (mData[mOffset + offset_ver_ihl] & 0x0F) * 4;
    }

    public void setHeaderLength(int value) {
        // 4 << 4 表示版本为IPv4
        mData[mOffset + offset_ver_ihl] = (byte) ((4 << 4) | (value / 4));
    }

    public byte getTos() {
        return mData[mOffset + offset_tos];
    }

    public void setTos(byte value) {
        mData[mOffset + offset_tos] = value;
    }

    public int getTotalLength() {
        return CommonMethods.readShort(mData, mOffset + offset_tlen) & 0xFFFF;
    }

    public void setTotalLength(long value) {
        CommonMethods.writeShort(mData, mOffset + offset_tlen, (short) value);
    }

    public int getIdentification() {
        return CommonMethods.readShort(mData, mOffset + offset_identification) & 0xFFFF;
    }

    public void setIdentification(int value) {
        CommonMethods.writeShort(mData, mOffset + offset_identification, (short) value);
    }

    public short getFlagsAndOffset() {
        return CommonMethods.readShort(mData, mOffset + offset_flags_fo);
    }

    public void setFlagsAndOffset(short value) {
        CommonMethods.writeShort(mData, mOffset + offset_flags_fo, value);
    }

    public byte getTTL() {
        return mData[mOffset + offset_ttl];
    }

    public void setTTL(byte value) {
        mData[mOffset + offset_ttl] = value;
    }

    public byte getProtocol() {
        return mData[mOffset + offset_proto];
    }

    public void setProtocol(byte value) {
        mData[mOffset + offset_proto] = value;
    }

    public short getCrc() {
        return CommonMethods.readShort(mData, mOffset + offset_crc);
    }

    public void setCrc(short value) {
        CommonMethods.writeShort(mData, mOffset + offset_crc, value);
    }

    public long getSourceIP() {
        return CommonMethods.readInt(mData, mOffset + offset_src_ip);
    }

    public void setSourceIP(long value) {
        CommonMethods.writeInt(mData, mOffset + offset_src_ip, value);
    }

    public long getDestinationIP() {
        return CommonMethods.readInt(mData, mOffset + offset_dest_ip);
    }

    public void setDestinationIP(long value) {
        CommonMethods.writeInt(mData, mOffset + offset_dest_ip, value);
    }

    @Override
    public String toString() {
        return String.format("%s->%s Pro=%s, HLen=%d", CommonMethods.ipLongToString(getSourceIP()),
                CommonMethods.ipLongToString(getDestinationIP()), getProtocol(), getHeaderLength());
    }
}
