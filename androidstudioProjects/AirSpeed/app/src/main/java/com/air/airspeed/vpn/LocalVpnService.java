package com.air.airspeed.vpn;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
//import android.net.LocalSocket;
//import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.icu.text.LocaleDisplayNames;
import android.net.VpnService;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.ParcelFileDescriptor;
import android.os.TransactionTooLargeException;
import android.support.annotation.RequiresApi;
import android.util.Log;

import com.air.airspeed.AppInfo;
import com.air.airspeed.NetApp;
//import com.air.airspeed.R;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.lang.String;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.security.auth.callback.Callback;

import static com.air.airspeed.vpn.VPNConstants.VPN_SP_NAME;

//created by liu 2019-12-13

public class LocalVpnService  extends VpnService implements Runnable {
    public static final String TAG = "LocalVpnService";

    public static String VPN_ADDRESS = "10.0.0.2";  //当前只支持IPV4
    private static final String VPN_ROUTE = "0.0.0.0";  // intercept everythiing
    // private static final String GOOGLE_DNS_FIRST = "8.8.8.8";
    //private static final String GOOGLE_DNS_SECOND = "8.8.4.4";
    //private static final String AMERICA = "208.67.222.222";
    private static final String HK_DNS_SECOND = "205.252.144.228";
    public final static int TYPE_TCP = 0;
    public final static int TYPE_TCP6 = 1;
    public final static int TYPE_UDP = 2;
    public final static int TYPE_UDP6 = 3;
    //public final static int TYPE_CONNTRACK = 3;
    public final static int TYPE_RAW = 4;
    public final static int TYPE_RAW6 = 5;
    public final static int TYPE_MAX = 6;
    private static final String CHINA_DNS_FIRST = "114.114.114.114";
    public static final String BROADCAST_VPN_STATE = "com.air.speed.localvpn.VPN_STATE";
    public static final String SELECT_PACKAGE_ID = "select_protect_package_id";

    public static LocalVpnService INSTANCE;
    private static int ID;
    private static long LOCAL_IP;
    private final UDPHeader mUDPHeader;
    private final ByteBuffer mDNSBuffer;
    private boolean IsRunning = false;
    private Thread mVPNThread;
    private ParcelFileDescriptor mVPNInterface;
    private TcpProxyServer mTcpProxyServer;
    private FileOutputStream mVPNOutputStream;
    private FileInputStream in;

    private byte[] mPacket;
    private IPHeader mIPHeader;
    private TCPHeader mTCPHeader;
    private Handler mHandler;
    private ConcurrentLinkedQueue<Packet> udpQueue;
    private UDPServer udpServer;
    private String selectPackage;
    public static final int MUTE_SIZE = 2560;
    private int mReceivedBytes;
    private int mSentBytes;
    public static long vpnStarTime;
    public static String lastVpnStartTimeFormat = null;


    private String apppackage;
    //private LocalSocket localSocket;

   // private Callback callback;    //提供给主活动加速流的五元组，以便主活动发给自己的服务器进行加速

    public LocalVpnService() {
        ID++;
        mHandler = new Handler();
        mPacket = new byte[MUTE_SIZE];
        mIPHeader = new IPHeader(mPacket, 0);
        // offset = ip头部长度
        mTCPHeader = new TCPHeader(mPacket, 20);
        mUDPHeader = new UDPHeader(mPacket, 20);
        // offseta = ip头部长度+ udp头部长度 = 28
        mDNSBuffer = ((ByteBuffer) ByteBuffer.wrap(mPacket).position(28)).slice();
        INSTANCE = this;

        Log.i(TAG, "new LocalVPNService: " + ID);
        Log.e(TAG,"build stack: "+ Log.getStackTraceString(new Throwable()));
    }

    //启动工作线程
    @Override
    public void onCreate() {
        Log.e(TAG,"create stack: "+ Log.getStackTraceString(new Throwable()));
        Log.i(TAG, "VPNService created: " + ID);
        VpnServiceHelper.onVpnServiceCreated(this);
        mVPNThread = new Thread(this, "VPNServiceThread");
        mVPNThread.start();
        setVpnRunningStatus(true);  //设置 IsRuning = true
        super.onCreate();

        ArrayList<AppInfo> lsApp = NetApp.mInstance.getApp();
        if (lsApp != null) {
            for (AppInfo tmp : lsApp) {
                if (tmp.getIsAccelerated() == true) {
                    apppackage = tmp.getPackName();
                }
            }
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    //停止VPN工作线程
    @Override
    public void onDestroy() {
        Log.i(TAG, "VPNService destroyed: " + ID);
        if (mVPNThread != null) {
            mVPNThread.interrupt();
        }
        VpnServiceHelper.onVpnServiceDestroy();
        super.onDestroy();
    }

    //建立VPN，同时监听
    @RequiresApi(api = Build.VERSION_CODES.N)
    private void startVpn() throws Exception {
        Log.d(TAG, "LocalVpnService established");
        this.mVPNInterface = establishVPN();  //获得文件操作符实例
        startStream();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void startStream() throws Exception {
        int size = 0;
        in = new FileInputStream(mVPNInterface.getFileDescriptor()); //读取数据包
        mVPNOutputStream = new FileOutputStream(mVPNInterface.getFileDescriptor());   //写回响应数据包
        while (size != -1 && IsRunning) {
            boolean hasWrite = false;    //检测数据包是否成功变换地址
            size = in.read(mPacket);
            if (size > 0) {
                if (mTcpProxyServer.Stopped) {
                    Log.d(TAG,"TCPserver stopped");
                    in.close();
                    throw new Exception("TCPServer stopped");
                }
                hasWrite = onIPacketReceived(mIPHeader, size);

                //调用回调接口，获取加速五元组信息给主活动
                //callback.getSpeedStream();
            }
            if (!hasWrite) {
                Packet packet = udpQueue.poll();
                if (packet != null) {
                    ByteBuffer bufferFromNetwork = packet.backingBuffer;
                    bufferFromNetwork.flip();
                    mVPNOutputStream.write(bufferFromNetwork.array());
                }
            }
            Thread.sleep(10);
        }
        in.close();
        disconnectVPN();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private boolean onIPacketReceived(IPHeader ipHeader, int size) throws IOException {
        boolean hasWrite = false;

        switch (ipHeader.getProtocol()) {
            case IPHeader.TCP:
                hasWrite = onTcpPacketReceived(ipHeader, size);
                break;
            case IPHeader.UDP:
                onUdpPacketReceived(ipHeader, size);
                break;
            default:
                break;
        }
        return hasWrite;
    }

    private void onUdpPacketReceived(IPHeader ipHeader, int size) throws UnknownHostException {

        UDPHeader udpHeader = mUDPHeader;
        int portKey = udpHeader.getSourcePort();
        NatSession session = NatSessionManager.getSession(portKey);
        if (session == null || session.remoteIP != ipHeader.getDestinationIP() || session.remotePort
                != udpHeader.getDestinationPort()) {
            session = NatSessionManager.createSession(portKey, ipHeader.getSourceIP(), ipHeader.getDestinationIP(), udpHeader.getDestinationPort(), NatSession.UDP);
            session.vpnStartTime = vpnStarTime;
            ThreadProxy.getInstance().execute(new Runnable() {
                @Override
                public void run() {
                    if(PortHostService.getInstance()!=null){
                        PortHostService.getInstance().refreshSessionInfo();
                    }

                }
            });
        }
        session.lastRefreshTime = System.currentTimeMillis();
        session.packetSent++;

        byte[] bytes = Arrays.copyOf(mPacket, mPacket.length);
        ByteBuffer byteBuffer = ByteBuffer.wrap(bytes, 0, size);
        byteBuffer.limit(size);
        Packet packet = new Packet(byteBuffer);
        udpServer.processUDPPacket(packet, portKey);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private boolean onTcpPacketReceived(IPHeader ipHeader, int size) throws IOException {
        boolean hasWrite = false;
        TCPHeader tcpHeader = mTCPHeader;
        //设置tcpacket真正的offset
        tcpHeader.mOffset = mIPHeader.getHeaderLength();
        if (tcpHeader.getSourcePort() == mTcpProxyServer.port) {   //tcp数据包由tcpServer返回，这里是从服务器返回的数据送往应用
            Log.d(TAG, "process tcp packet from net");
            NatSession session = NatSessionManager.getSession(tcpHeader.getDestinationPort());
            if (session != null) {
                ipHeader.setSourceIP(ipHeader.getDestinationIP());  //natSession.remotIP
                tcpHeader.setSourcePort(session.remotePort);
                ipHeader.setDestinationIP(LOCAL_IP);
                session.PacketRev++;

                CommonMethods.ComputeTCPChecksum(ipHeader, tcpHeader);
                mVPNOutputStream.write(ipHeader.mData, ipHeader.mOffset, size);
                mReceivedBytes += size;
            } else {
                Log.d(TAG, "natSession = null: " + ipHeader + " " + tcpHeader);
            }
        } else {    //这里是应用发出的数据先绕到本地服务器
            Log.d(TAG, "process tcp packet to net");
            //添加端口映射
            int portKey = tcpHeader.getSourcePort();
            NatSession session = NatSessionManager.getSession(portKey);
            if (session == null || session.remoteIP != ipHeader.getDestinationIP()
                    || session.remotePort != tcpHeader.getDestinationPort()) {
                session = NatSessionManager.createSession(portKey, ipHeader.getSourceIP(), ipHeader.getDestinationIP(),
                        tcpHeader.getDestinationPort(), NatSession.TCP);
                session.vpnStartTime = vpnStarTime;
                ThreadProxy.getInstance().execute(new Runnable() {
                    @Override
                    public void run() {
                        PortHostService instance = PortHostService.getInstance();
                        if (instance != null) {
                            instance.refreshSessionInfo();
                        }
                    }
                });
            }
            session.lastRefreshTime = System.currentTimeMillis();
            session.packetSent++;    //计算当前数据包发送的是第几个，前两个与服务器建立联接
            int tcpDataSize = ipHeader.getDataLength() - tcpHeader.getHeaderLength();
            if (session.packetSent == 2 && tcpDataSize == 0) {
                return false;
                //丢弃tcp握手的第二个ACK报文。因为客户端发送数据时也会带上ACK，这样可以再服务器Accept之前分析出host信息
            }
            //分析数据，找到HOST
            if (session.bytesSent == 0 && tcpDataSize > 10) {
                int dataOffset = tcpHeader.mOffset + tcpHeader.getHeaderLength();
                HttpRequestHeaderParser.parseHttpRequestHeader(session, tcpHeader.mData, dataOffset,
                        tcpDataSize);
                Log.i(TAG, "Host: " + session.remoteHost);
                Log.i(TAG, "Request: " + session.method + " " + session.requestUrl);
            } else if (session.bytesSent > 0 && !session.isHttpsSession
                    && session.isHttp && session.remoteHost == null && session.requestUrl == null) {
                int dataOffset = tcpHeader.mOffset + tcpHeader.getHeaderLength();
                session.remoteHost = HttpRequestHeaderParser.getRemoteHost(tcpHeader.mData, dataOffset,
                        tcpDataSize);
                session.requestUrl = "http://" + session.remoteHost + "/" + session.pathUrl;
            }

            //转发给本地服务器tcpServer
            ipHeader.setSourceIP(ipHeader.getDestinationIP());
            ipHeader.setDestinationIP(LOCAL_IP);
            tcpHeader.setDestinationPort(mTcpProxyServer.port);

            CommonMethods.ComputeTCPChecksum(ipHeader, tcpHeader);
            mVPNOutputStream.write(ipHeader.mData, ipHeader.mOffset, size);
            //out.write(iPacket.m_Data, iPacket.m_Offset, size);
            session.bytesSent += tcpDataSize;
            mSentBytes += size;
        }
        hasWrite = true;
        return hasWrite;
    }

    private void waitUtilPrepared() {
        while (prepare(this) != null) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private ParcelFileDescriptor establishVPN() throws Exception {
        Builder builder = new Builder();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            try {
                builder.addAllowedApplication(apppackage);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        }
        builder.setMtu(MUTE_SIZE);
        Log.i(TAG, "setMtu: " + ProxyConfig.Instance.getMtu());

        ProxyConfig.IPAddress ipAddress = ProxyConfig.Instance.getDefaultLocalIP();
        LOCAL_IP = CommonMethods.ipStringToLong(ipAddress.Address);
        builder.addAddress(ipAddress.Address, ipAddress.PrefixLength);
        Log.i(TAG, "addAddress: " + ipAddress.Address + " " + ipAddress.PrefixLength);
        builder.addRoute(VPN_ROUTE, 0);
        //builder.addDnsServer(GOOGLE_DNS_FIRST);
        builder.addDnsServer(CHINA_DNS_FIRST);
        builder.addDnsServer(HK_DNS_SECOND);
        //builder.addDnsServer(GOOGLE_DNS_SECOND);
        //builder.addDnsServer(AMERICA);
        vpnStarTime = System.currentTimeMillis();
        lastVpnStartTimeFormat = TimeFormatUtil.formatYYMMDDHHMMSS(vpnStarTime);

        builder.setSession("LocalVpnService");
        ParcelFileDescriptor pfdDescriptor = builder.establish();
        return pfdDescriptor;
    }

    //此线程主要开启两个服务器
    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void run() {
        try {
            Log.i(TAG, "VPNService work thread is Running: " + ID);
            waitUtilPrepared();
            udpQueue = new ConcurrentLinkedQueue<>();

            //启动TCP代理服务器
            mTcpProxyServer = new TcpProxyServer(0);
            mTcpProxyServer.start();
            udpServer = new UDPServer(this, udpQueue);
            udpServer.start();
            NatSessionManager.clearAllSession();
            if(PortHostService.getInstance()!=null){
                PortHostService.startParse(getApplicationContext());
            }
            Log.i(TAG, "DnsProxy started");
            //ProxyConfig.Instance.onVpnStart(this);
            while (IsRunning) {
                startVpn();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            Log.i(TAG, "VpnService terminated");
            //ProxyConfig.Instance.onVpnEnd(this);
            dispose();
        }
    }

    public void disconnectVPN() {
        try {
            if (mVPNInterface != null) {
                mVPNInterface.close();
                mVPNInterface = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.mVPNOutputStream = null;
    }

    private synchronized void dispose() {
        try {
            disconnectVPN();   //断开VPN
            //停止TCP代理服务器
            if (mTcpProxyServer != null) {
                mTcpProxyServer.stop();
                mTcpProxyServer = null;
                Log.i(TAG, "TcpProxyServer stopped");
            }
            if (udpServer != null) {
                udpServer.closeAllUDPConn();
            }
            ThreadProxy.getInstance().execute(new Runnable() {
                @Override
                public void run() {
                    if(PortHostService.getInstance()!=null){
                        PortHostService.getInstance().refreshSessionInfo();
                    }
                    PortHostService.stopParse(getApplicationContext());
                }
            });

            stopSelf();
            setVpnRunningStatus(false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean vpnRunningStatus() {
        return IsRunning;
    }

    //设置mVpnThread的运行状态
    public void setVpnRunningStatus(boolean isRunning) {
        this.IsRunning = isRunning;
    }

    //定义回调接口
   /* public static interface Callback {
        void getSpeedStream();   //获取加速流的五元组信息
    }

    //提供接口回调方法
    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    private StreamBinder sBinder = new StreamBinder();

    public class StreamBinder extends Binder {
        //申明方法返回值是service本身
        public LocalVpnService getService() {
            return LocalVpnService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return sBinder;
    }*/
}














