package com.air.airspeed;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.HandlerThread;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

public class MinaService extends Service {
    private ConnectionThread thread;

    @Override
    public void onCreate(){
        super.onCreate();
        thread = new ConnectionThread("mina",getApplicationContext());
        thread.start();
        Log.e("tag","start thread to connect");
    }
    @Override
    public int onStartCommand(Intent intent,int flags,int startId) {
        return super.onStartCommand(intent,flags,startId);
    }
    @Override
    public void onDestroy(){
        super.onDestroy();
        thread.disConnect();
        thread = null;
    }
    @Nullable
    @Override
    public IBinder onBind(Intent intent){
        return null;
    }

    class ConnectionThread extends HandlerThread{
        private Context context;
        boolean isConnection;
        ConnectionManager mManager;
        public ConnectionThread(String name,Context context){
            super(name);
            this.context = context;
            ConnectionConfig config = new ConnectionConfig.Builder(context)
                    .setIp("192.168.3.4")
                    .setPort(9226)
                    .setReadBufferSize(10240)
                    .setConnectionTimeout(10000).builder();

            mManager = new ConnectionManager(config);
        }
        @Override
        protected void onLooperPrepared(){
            while (true){
                isConnection = mManager.connect();
                if (isConnection){
                    Log.e("tag","success to connect");
                    SessionManager.getInstance().writeToServer(NetApp.mInstance.getUser().getUserName());
                    break;
                }
                try{
                    Log.e("tag","attempt to reconnect");
                    Thread.sleep(3000);
                }catch (InterruptedException e){
                    e.printStackTrace();
                }
            }
        }
        public void disConnect(){
            mManager.disContect();
        }
    }
}
