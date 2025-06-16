package com.air.airspeed.vpn;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.air.airspeed.AppInfo;
import com.air.airspeed.NetApp;

import java.util.List;

//created by liu 2020-01-15
public class PortHostService extends Service{
    private static final String ACTION = "action";
    private static final String TAG = "PortHostService";
    private static PortHostService instance;
    private boolean isRefresh = false;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        NetFileManager.getInstance().init(getApplicationContext());
        instance = this;
    }

    public static PortHostService getInstance() {
        return instance;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        instance = null;
    }

    public List<NatSession> getAndRefreshSessionInfo() {
        List<NatSession> allSession = NatSessionManager.getAllSession();
        refreshSessionInfo(allSession);
        return allSession;

    }

    public void refreshSessionInfo() {
        List<NatSession> allSession = NatSessionManager.getAllSession();
        refreshSessionInfo(allSession);
    }

    private void refreshSessionInfo(List<NatSession> netConnections) {
        if (isRefresh || netConnections == null) {
            return;
        }
        boolean needRefresh = false;
        for (NatSession connection : netConnections) {
            if (connection.appInfo == null) {
                needRefresh = true;
                break;
            }
        }
        if (!needRefresh) {
            return;
        }
        isRefresh = true;
        try {
            NetFileManager.getInstance().refresh();
            /*AppInfo appInfo = new AppInfo();
            for (AppInfo tmp:NetApp.mInstance.getApp()){
                if (tmp.getIsAccelerated()){
                    appInfo = tmp;
                    break;
                }
            }*/

            for (NatSession connection : netConnections) {
                if (connection.appInfo == null) {
                    //connection.appInfo = appInfo;
                    int searchPort = connection.localPort & 0XFFFF;
                    Integer uid = NetFileManager.getInstance().getUid(searchPort);

                    if (uid != null) {
                        Log.d(TAG, "can not find uid");
                        connection.appInfo = AppInfo.createFromUid(uid);
                    }
                }
            }
        } catch (Exception e) {
            Log.d(TAG,"failed to refreshSessionInfo "+e.getMessage());
        }
        isRefresh = false;
    }


    public static void startParse(Context context) {
        Intent intent = new Intent(context, PortHostService.class);
        context.startService(intent);
    }

    public static void stopParse(Context context) {
        Intent intent = new Intent(context, PortHostService.class);
        context.stopService(intent);
    }
}
