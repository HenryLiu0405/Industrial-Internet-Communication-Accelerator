package com.air.airspeed;

import android.app.ActivityManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.FormBody;
import okhttp3.Request;

public class LifeService extends Service {
    private  int count = 0;
    private final int NOT_SPEED_SEND = 254;
    private final int SERVER_NOSPEED = 255;
    private final int IS_SPEED = 1;
    private final int NOT_SPEED = 0;

    public boolean isBackgroud() {
        ActivityManager aM = (ActivityManager) getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> appProcesses = aM.getRunningAppProcesses();
        List<AppInfo> lsApp = NetApp.mInstance.getApp();
        for (ActivityManager.RunningAppProcessInfo appProcess:appProcesses) {
            for (AppInfo app:lsApp) {
                if (appProcess.processName.equals(app.getPackName())) {
                    if (appProcess.importance != ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                        count ++;
                        if (count >= 5 && app.getIsBackground() != true) {
                            app.setIsBackground(true);
                        }
                        return true;
                    }else {
                        count = 0;
                        app.setIsBackground(false);
                        return false;
                    }
                }
            }
        }
        return true;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        executeMonitor();
    }

    public void executeMonitor() {
        ScheduledExecutorService monitor = Executors.newScheduledThreadPool(1);
        monitor.scheduleAtFixedRate(new EchoServer(),10,2*60,TimeUnit.SECONDS);
    }

    @SuppressWarnings("WrongConstant")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        flags = START_STICKY;
        return super.onStartCommand(intent,flags,startId);
    }



    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    class EchoServer implements Runnable {
        @Override
        public void run() {
            //isBackgroud();
            heartsend();
            nospeedagain();
        }
    }

    public void heartsend() {
        String userName = NetApp.mInstance.getUser().getUserName();
        FormBody.Builder mFormBodyBuild = new FormBody.Builder();

        if (userName == null) {
           return;
        }
        mFormBodyBuild.add("UserName",userName);

        FormBody mFormBody = mFormBodyBuild.build();
        Request request = new Request.Builder()
                .url("http://p22n940119.iok.la/AirSpeed/HeartServlet")
                .post(mFormBody)
                .build();

        HttpUtil.sendOkHttpRequest(request, new okhttp3.Callback() {
            @Override
            public void onResponse(Call call, okhttp3.Response response) throws IOException {
                Log.d("heartrequest", "already send and receive response");

            }

            @Override
            public void onFailure(Call call, IOException e) {
                Log.d("heart no response", Log.getStackTraceString(e));

            }
        });
    }

    public void nospeedagain() {
        String userName;

        if (NetApp.mInstance.getUser().getUserName() != null) {
            userName = NetApp.mInstance.getUser().getUserName();
        }
        else {
            return;
        }
        ArrayList<AppInfo> lsApp = NetApp.mInstance.getApp();
        if (lsApp == null){
            return;
        }
        for (AppInfo lApp:lsApp) {
            ArrayList<NetInfo> lsNet = lApp.lsNet;
            if (lsNet == null){
                continue;

            }
            for (NetInfo lNet:lsNet) {
                if (lNet.getIsspeed() == NOT_SPEED_SEND){
                    //定时删除没有收到服务器应答的流
                    FormBody.Builder mFormBodyBuild1 = new FormBody.Builder();
                    mFormBodyBuild1.add("SpeedType", "0");
                    //mFormBodyBuild.add("Time",dateTime.getCurrentTimebyFormat("yyyyMMddHHmmss"));
                    if (userName != null) {
                        mFormBodyBuild1.add("UserName", userName);
                    }
                    mFormBodyBuild1.add("AppName", lApp.getAppName());
                    mFormBodyBuild1.add("PackageName", lApp.getPackName());
                    mFormBodyBuild1.add("Protocol",String.valueOf(lNet.getType()));
                    mFormBodyBuild1.add("LocalAddress", lNet.getLocalAddress());
                    mFormBodyBuild1.add("LocalPort", String.valueOf(lNet.getLocalPort()));
                    mFormBodyBuild1.add("RemoteAddress", lNet.getRemoteAddress());
                    mFormBodyBuild1.add("RemotePort", String.valueOf(lNet.getRemotePort()));
                    mFormBodyBuild1.add("Token", lNet.getToken());

                    mFormBodyBuild1.add("PublicIP", lNet.getInternetIP());
                    mFormBodyBuild1.add("Operator", String.valueOf(lNet.getOperator()));
                    mFormBodyBuild1.add("DataLength",String.valueOf(lNet.getDataLength()));

                    FormBody mFormBody1 = mFormBodyBuild1.build();
                    Request request1 = new Request.Builder()
                            .url("http://p22n940119.iok.la/AirSpeed/SpeedServlet")
                            .post(mFormBody1)
                            .build();
                    Log.d("lifeservice", "have built request cancel");
                    Log.d("lifeservice","cancel link is "+lNet.getRemoteAddress()+" "+lNet.getLocalPort());

                    HttpUtil.sendOkHttpRequest(request1, new okhttp3.Callback() {
                        @Override
                        public void onResponse(Call call, okhttp3.Response response) throws IOException {
                            Log.d("lifeservice", "already send and receive response cancel");
                            try {
                                Log.d("lifeservice", "into try");
                                String responseData = response.body().string();
                                Log.d("lifeservice", "remsg is "+responseData);

                                JSONObject jsonObject = (JSONObject) new JSONObject(responseData).get("params");
                                Log.d("lifeservice", "Json is decode");

                                ArrayList<AppInfo> lApp = NetApp.mInstance.getApp();
                                for (AppInfo info1:lApp) {
                                    if (info1.getAppName().equals(jsonObject.getString("AppName"))
                                            && info1.getPackName().equals(jsonObject.getString("PackageName"))) {
                                        Log.d("lifeservice", "app is found");
                                        ArrayList<NetInfo> lNet = info1.lsNet;
                                        for (NetInfo net:lNet) {
                                            if (net.getType() == Integer.parseInt(jsonObject.getString("Protocol"))
                                                    && net.getLocalAddress().equals(jsonObject.getString("LocalAddress"))
                                                    && net.getLocalPort() == Integer.parseInt(jsonObject.getString("LocalPort"))
                                                    && net.getRemoteAddress().equals(jsonObject.getString("RemoteAddress"))
                                                    && net.getRemotePort() == Integer.parseInt(jsonObject.getString("RemotePort"))) {
                                                Log.d("lifeservice", "have set nospeed " + net.getRemoteAddress()+" "+ String.valueOf(net.getLocalPort()));
                                                net.setIsspeed(SERVER_NOSPEED);
                                            }
                                        }
                                    }
                                }
                            }catch (JSONException e) {
                                Log.d("lifeservice fail", Log.getStackTraceString(e));
                            }
                        }
                        @Override
                        public void onFailure(Call call, IOException e) {
                            Log.d("lifeservice noresponse", Log.getStackTraceString(e));

                        }
                    });
                }
            }
        }
    }
}














