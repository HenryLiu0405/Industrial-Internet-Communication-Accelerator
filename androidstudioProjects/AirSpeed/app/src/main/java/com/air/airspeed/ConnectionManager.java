package com.air.airspeed;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.future.IoFuture;
import org.apache.mina.core.future.IoFutureListener;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.serialization.ObjectSerializationCodecFactory;
import org.apache.mina.filter.logging.LoggingFilter;
import org.apache.mina.transport.socket.nio.NioSocketConnector;
import org.xml.sax.helpers.DefaultHandler;

import java.lang.ref.WeakReference;
import java.net.InetSocketAddress;

public class ConnectionManager {
    private static final String  BROADCAST_ACTION = "com.air.airspeed.mina.broadcast";
    private static final String MESSAGE = "message";
    private ConnectionConfig mConfig;
    private WeakReference<Context> mContext;
    private NioSocketConnector mConnection;
    private IoSession mSession;
    private InetSocketAddress mAddress;

    public ConnectionManager(ConnectionConfig config){
        this.mConfig = config;
        this.mContext = new WeakReference<Context>(config.getContext());
        init();
    }
    private void init(){
        mAddress = new InetSocketAddress(mConfig.getIp(),mConfig.getPort());
        mConnection = new NioSocketConnector();
        mConnection.getSessionConfig().setReadBufferSize(mConfig.getReadBufferSize());
        mConnection.getFilterChain().addLast("logging",new LoggingFilter());
        mConnection.getFilterChain().addLast("codec",new ProtocolCodecFilter(new ObjectSerializationCodecFactory()));
        mConnection.setHandler(new DefaultHandler(mContext.get()));
        mConnection.setDefaultRemoteAddress(mAddress);
    }
    //连接服务器
    public boolean connect(){
        Log.e("tag","is preparing to connect");

        try{
            ConnectFuture future = mConnection.connect();
            Log.e("tag","connect1");
            future.addListener(new IoFutureListener<ConnectFuture>() {
                @Override
                public void operationComplete(ConnectFuture ioFuture) {
                    Log.e("tag","connect2");
                    mSession = ioFuture.getSession();

                    SessionManager.getInstance().setSession(mSession);
                    Log.e("tag","connect3");
                }
            });

            //mSession = future.getSession();

            //SessionManager.getInstance().setSession(mSession);
            Log.e("tag","connect4");
        }catch (Exception e){
            e.printStackTrace();
            Log.e("tag","failed to connect");
            return false;
        }
        return mSession == null?false:true;


    }
    //断开连接
    public void disContect(){
        mConnection.dispose();
        mConnection=null;
        mSession = null;
        mAddress = null;
        mContext = null;
        Log.e("tag","disconnecting");
    }

    private static class DefaultHandler extends IoHandlerAdapter{
        private Context mContext;
        private DefaultHandler(Context context){
            this.mContext = context;
        }
        @Override
        public void sessionOpened(IoSession session) throws Exception {
            super.sessionOpened(session);
        }
        @Override
        public void messageReceived(IoSession session, Object message) throws Exception{
            Log.e("tag","received message of server" + message.toString());
            if (mContext != null){
                Intent intent = new Intent(BROADCAST_ACTION);
                intent.putExtra(MESSAGE, message.toString());
                LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
            }
        }
    }
}
