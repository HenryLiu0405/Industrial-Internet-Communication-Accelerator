package com.air.airspeed.vpn;

import android.util.SparseArray;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

//created by liu 2019-12-16 NAT管理对象就是将数据包的源端口作为key，保存目标ip和目标port

public class NatSessionManager {
    public final static int TYPE_TCP = 0;
    public final static int TYPE_TCP6 = 1;
    public final static int TYPE_UDP = 2;
    public final static int TYPE_UDP6 = 3;
    //public final static int TYPE_CONNTRACK = 3;
    public final static int TYPE_RAW = 4;
    public final static int TYPE_RAW6 = 5;
    public final static int TYPE_MAX = 6;
    /**
     * 会话保存的最大个数
     */

    static final int MAX_SESSION_COUNT = 64;
    /**
     * 会话保存时间
     */

    private static final long SESSION_TIME_OUT_NS = 60 * 1000L;
    public static final ConcurrentHashMap<Integer, NatSession> sessions = new ConcurrentHashMap<>();

    /**
     * 通过本地端口获取会话信息
     *
     * @param portKey 本地端口
     * @return 会话信息
     */
    public static NatSession getSession(int portKey) {
        return sessions.get(portKey);
    }

    /**
     * 获取会话个数
     *
     * @return 会话个数
     */
    public static int getSessionCount() {
        return sessions.size();
    }

    /**
     * 清除过期的会话
     */
    static void clearExpiredSessions() {
        long now = System.currentTimeMillis();
        Set<Map.Entry<Integer, NatSession>> entries = sessions.entrySet();
        Iterator<Map.Entry<Integer, NatSession>> iterator = entries.iterator();
        while (iterator.hasNext()) {
            Map.Entry<Integer, NatSession> next = iterator.next();
            if (now - next.getValue().lastRefreshTime > SESSION_TIME_OUT_NS) {
                iterator.remove();
            }
        }
    }

    public static void clearAllSession() {
        sessions.clear();
    }

    public static List<NatSession> getAllSession() {
        ArrayList<NatSession> natSessions = new ArrayList<>();
        Set<Map.Entry<Integer, NatSession>> entries = sessions.entrySet();
        Iterator<Map.Entry<Integer, NatSession>> iterator = entries.iterator();
        while (iterator.hasNext()) {
            Map.Entry<Integer, NatSession> next = iterator.next();
            natSessions.add(next.getValue());
        }
        return natSessions;
    }

    /**
     * 创建会话
     *
     * @param portKey    源端口
     * @param remoteIP   远程ip
     * @param remotePort 远程端口
     * @return NatSession对象
     */
    public static NatSession createSession(int portKey, long localIP,long remoteIP, int remotePort, String type) {
        if (sessions.size() > MAX_SESSION_COUNT) {
            clearExpiredSessions(); //清除过期的会话
        }

        NatSession session = new NatSession();
        // session.lastRefreshTime = System.currentTimeMillis();
        session.lastRefreshTime = System.currentTimeMillis();
        session.remoteIP = remoteIP;
        session.remotePort = remotePort;
        session.localPort = portKey;
        session.localIP = localIP;


        if (session.remoteHost == null) {
            session.remoteHost = CommonMethods.ipLongToString(remoteIP);
        }
        session.type = type;
        if (type.equals(NatSession.TCP)){
            session.Protocol = TYPE_TCP;
        }else if (type.equals(NatSession.UDP)){
            session.Protocol = TYPE_UDP;
        }
        session.refreshIpAndPort();
        sessions.put(portKey, session);


        return session;
    }

    public static void removeSession(int portKey) {
        sessions.remove(portKey);
    }


}







