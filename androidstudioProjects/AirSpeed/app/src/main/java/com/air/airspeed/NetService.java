package com.air.airspeed;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;


import java.net.Inet4Address;

public class NetService extends Service
{
    private NetFile netFile;

    @Override
    public void onCreate()
    {
        NetFile netFile = new NetFile(this);
        netFile.start();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        return super.onStartCommand(intent,flags,startId);
    }

    @Override
    public boolean onUnbind(Intent intent)
    {
        return super.onUnbind(intent);
    }

    @Override
    public IBinder onBind(Intent intent)
    {
        //Intent intent = getIntent();
        //Bundle bd = intent.getExtras();
        //pkg = bd.getString("SpeedAPPpkg");
        //cls = bd.getString("SpeedAPPcls");
        return null;
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        netFile.stop();
        netFile = null;
    }

}
