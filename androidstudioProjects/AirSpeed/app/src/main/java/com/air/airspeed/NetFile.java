package com.air.airspeed;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ApplicationInfo;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.renderscript.ScriptGroup;
import android.telephony.TelephonyManager;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Scanner;
import java.util.Collections;

import okhttp3.FormBody;
import okhttp3.Request;
import okhttp3.Call;
import okhttp3.OkHttpClient;

import static java.lang.StrictMath.abs;

public class NetFile
{
    public final static int TYPE_TCP = 0;
    public final static int TYPE_TCP6 = 1;
    public final static int TYPE_UDP = 2;
    public final static int TYPE_UDP6 = 3;
    //public final static int TYPE_CONNTRACK = 3;
    public final static int TYPE_RAW = 4;
    public final static int TYPE_RAW6 = 5;
    public final static int TYPE_MAX = 6;

    public final static int MSG_NET_CALLBACK = 1;
    public final static String MSG_NET_GET = "com.air.airspeed.get";

    private static Context context;
    private Handler handler;
    private NetThread netThread;

    private final static int DATA_LOCAL = 2;
    private final static int DATA_REMOTE = 3;
    private final static int SOCKET_STATE = 4;
    private final static int DATA_LENGTH = 5;
    private final static int DATA_UID = 8;
    private final static int IS_SPEED = 1;
    private final static int NOT_SPEED = 0;
    private final static int NOT_SPEED_SEND = 254;
    private final static int SERVER_NOSPEED = 255;
    private final static int TCP_ESTABLISHED = 1;

    private final static int SPEED_STREAM = 5;

    private final static boolean isAdd = false;

    private final static int MOBILE = 0;
    private final static int UNICOM = 1;
    private final static int TELECOM =2;


    public NetFile(Context context)
    {
        this.context = context;

        handler = new Handler(new Handler.Callback(){
            @Override
            public boolean handleMessage(Message msg) {

                if(msg.what == MSG_NET_CALLBACK)
                {
                    Intent intent = new Intent();
                    intent.setAction(MSG_NET_GET);
                    if(NetFile.this.context != null) {
                        NetFile.this.context.sendBroadcast(intent);
                    }
                }
                return false;
            }
        });


    }



    public void start()
    {
        netThread = new NetThread(handler);
        netThread.start();
    }

    public void stop()
    {
        handler = null;
        context = null;
        netThread.stopNet();
    }

    public static class NetThread extends Thread
    {
        private Handler handler;
        private boolean isStop;
        private String pkg;
        private String cls;
        private StringBuilder sbBuilder = new StringBuilder();

        private NetThread(Handler handler)
        {
            this.handler = handler;
            isStop = false;
        }

        public void stopNet()
        {
            isStop = true;
            handler = null;
        }

        public void execute(String[] cmmand, String directory, int type) throws IOException
        {
            NetInfo netInfo = null;
            String sTmp = null;

            ProcessBuilder builder = new ProcessBuilder(cmmand);

            if (directory != null) {
                builder.directory(new File(directory));
            }
            builder.redirectErrorStream(true);
            Process process = builder.start();
            InputStream is = process.getInputStream();

            Scanner s = new Scanner(is);
            int icount = NetApp.mInstance.getAppCount();
            NetApp.mInstance.setAppCount(icount+1);
            s.useDelimiter("\n");
             while(s.hasNextLine()) {
                /*if (type ==3){
                    sTmp = s.nextLine();
                    Log.d("conntrack", sTmp);
                    return;
                }*/
                sTmp = s.nextLine();
                netInfo = selectData(sTmp);
                if(netInfo != null) {
                    netInfo.setType(type);
                    saveToList(netInfo);
                    //Log.d("execute", "netInfo is" + netInfo);
                }
             }

            //获得每个加速应用的最大数据的5条记录
            findSpeedItemandreport();

        }

        private int strToInt(String value, int iHex, int iDefault)
        {
            int iValue = iDefault;
            if (value == null)
            {
                return iValue;
            }

            try{
                iValue = Integer.parseInt(value,iHex);
            }
            catch (NumberFormatException e){
                e.printStackTrace();
            }

            return iValue;
        }

        private long strToLong(String value,int iHex, int iDefault)
        {
            long iValue = iDefault;
            if(value == null)
            {
                return iValue;
            }

            try
            {
                iValue = Long.parseLong(value, iHex);
            }
            catch (NumberFormatException e)
            {
                e.printStackTrace();
            }

            return iValue;
        }

        private NetInfo selectData(String sData)
        {
            Log.d("/proc/net", sData);

            String sSplitItem[] = sData.split("\\s+");
            String sTmp = null;
            int uid = 0;
            int state=0;
            StringBuilder localip = new StringBuilder();
            StringBuilder remoteip = new StringBuilder();

            sTmp = sSplitItem[SOCKET_STATE];
            state = strToInt(sTmp,16,0);
            if(sSplitItem.length <9 || state != 1)
            {
                return null;
            }

            //Log.d("/proc/net", sData);

            //pkg = NetApp.mInstance.getPackageName();
            //try{
               // PackageManager pm = NetApp.mInstance.getPackageManager();
                //ApplicationInfo ai = pm.getApplicationInfo(pkg, 0);
               // ApplicationInfo ai = pm.getApplicationInfo(pkg, PackageManager.GET_ACTIVITIES);
               // uid = ai.uid;
            //} catch (PackageManager.NameNotFoundException e){
             //   e.printStackTrace();
            //}

            ArrayList<AppInfo> lsApp = NetApp.mInstance.getApp();
            boolean found = false;
            for (AppInfo info:lsApp)  {
                uid = info.getUid();

                    Log.d("NetFile.selectData","uid is " + uid);
                    Log.d("NetFile.selectData", "AppName is "+info.getAppName() + info.getIsAccelerated());
                    Log.d("NetFile.selectData", "sSplitItem.uid" + sSplitItem[DATA_UID]);

                if (strToInt(sSplitItem[DATA_UID],10,0)== uid && info.getIsAccelerated() ==true) {
                    found = true;
                    break;
                }
            }
            if(!found) {
                return null;
            }

            NetInfo netInfo = new NetInfo();

            netInfo.setUid(strToInt(sSplitItem[DATA_UID],10,0));

            sTmp = sSplitItem[DATA_LOCAL];
            String sLocalItem[] = sTmp.split(":");
            if(sLocalItem.length < 2)
            {
                return null;
            }
            netInfo.setLocalPort(strToInt(sLocalItem[1],16,0));

            sTmp = sLocalItem[0];
            int locallen = sTmp.length();
            if(locallen < 8)
            {
                return null;
            }
            sTmp = sTmp.substring(locallen-8);
            netInfo.setLocalIp(strToLong(sTmp,16,0));
            localip.setLength(0);
            localip.append(strToInt(sTmp.substring(6,8),16,0))
                    .append(".")
                    .append(strToInt(sTmp.substring(4,6),16,0))
                    .append(".")
                    .append(strToInt(sTmp.substring(2,4),16,0))
                    .append(".")
                    .append(strToInt(sTmp.substring(0,2),16,0));
            sTmp = localip.toString();
            if (sTmp.equals("0.0.0.0") || sTmp.equals("127.0.0.1")) {
                return null;
            }
            netInfo.setLocalAddress(sTmp);

            sTmp = sSplitItem[DATA_REMOTE];
            String sRemoteItem[] = sTmp.split(":");
            if(sRemoteItem.length < 2)
            {
                return null;
            }
            netInfo.setRemotePort(strToInt(sRemoteItem[1],16,0));

            sTmp = sRemoteItem[0];
            int remotelen = sTmp.length();
            if(remotelen < 8)
            {
                return null;
            }
            sTmp = sTmp.substring(remotelen-8);
            netInfo.setRemoteIp(strToLong(sTmp,16,0));
            remoteip.setLength(0);
            remoteip.append(strToInt(sTmp.substring(6,8),16,0))
                    .append(".")
                    .append(strToInt(sTmp.substring(4,6),16,0))
                    .append(".")
                    .append(strToInt(sTmp.substring(2,4),16,0))
                    .append(".")
                    .append(strToInt(sTmp.substring(0,2),16,0));
            sTmp = remoteip.toString();
            if (sTmp.equals("0.0.0.0") || sTmp.equals("127.0.0.1")) {
                return null;
            }
            String iphead = sTmp.substring(0,7);
            if(iphead.equals("192.168")) {
                return null;
            }
            netInfo.setRemoteAddress(sTmp);

            sTmp = sSplitItem[DATA_LENGTH];
            String sDataItem[] = sTmp.split(":");
            //netInfo.setDataLength(strToLong(sDataItem[0] + sDataItem[1],16,0));
            long txqueue = strToLong(sDataItem[0],16,0);
            if (txqueue != 0) {
                Log.d("txqueue", "txqueue is "+ txqueue);
            }
            long rxqueue = strToLong(sDataItem[1],16,0);
            if (rxqueue != 0) {
                Log.d("rxqueue", "rxqueue is "+ rxqueue);
            }
            netInfo.setDataLength(txqueue+rxqueue);

            sTmp = sSplitItem[SOCKET_STATE];
            netInfo.setSocketstate(strToInt(sTmp,16,0));

           Log.d("queue DataLength", "datalength is" + netInfo.getDataLength());

            return netInfo;

        }

        private void saveToList(NetInfo netInfo)
        {
            if(netInfo == null)
            {
                return;
            }

            ArrayList<AppInfo> sApp = NetApp.mInstance.getApp();
            if (sApp == null)
            {
                return;
            }
            ArrayList<NetInfo> lsNet = null;
            for (AppInfo info:sApp) {
                if (info.getUid() != netInfo.getUid())
                {
                    continue;
                }

                lsNet = info.lsNet;
                if (lsNet == null)
                {
                    lsNet = new ArrayList<>();
                    info.lsNet = lsNet;
                }

                boolean found = false;
                for (NetInfo tmp: lsNet)
                {
                    if (tmp.getUid() == netInfo.getUid() && tmp.getLocalIp() == netInfo.getLocalIp()
                            && tmp.getLocalPort() == netInfo.getLocalPort()
                            && tmp.getRemoteIp() == netInfo.getRemoteIp()
                            && tmp.getRemotePort() == netInfo.getRemotePort())
                    {
                        found = true;
                        long delta = netInfo.getDataLength() - tmp.getDataLength();
                        tmp.setDeltalength(delta);
                        tmp.setNetCount(NetApp.mInstance.getAppCount());
                        if (tmp.getDataLength() != netInfo.getDataLength())
                        {
                            tmp.setDataLength(netInfo.getDataLength());
                        }
                        break;

                    }

                }

                Log.d("Netfile", "app is " + info.getAppName());
                Log.d("Netfile","package is " +info.getPackName());
                Log.d("Netfile","found is " + found);

                if (!found)  //&& netInfo.getSocketstate() == TCP_ESTABLISHED)
                {
                    netInfo.setDeltalength(netInfo.getDataLength());
                    netInfo.setNetCount(NetApp.mInstance.getAppCount());
                    lsNet.add(netInfo);
                    //android.os.Debug.waitForDebugger();
                    Log.d("NetFile", "address is " + netInfo.getUid()+ netInfo.getLocalIp()
                            + netInfo.getLocalPort() + netInfo.getRemoteIp() + netInfo.getRemotePort());
                    Log.d("Netfile","uid is " + netInfo.getUid());
                    Log.d("Netfile", "Local is " + netInfo.getLocalIp() +" " + netInfo.getLocalPort());
                    Log.d("Netfile", "Remote is " + netInfo.getRemoteIp() +" " + netInfo.getRemotePort());
                    Log.d("Netfile","delta is " + netInfo.getDeltalength());
                }
                break;
            }
        }

        private void findSpeedItemandreport()
        {
            ArrayList<AppInfo> lsApp = NetApp.mInstance.getApp();
            String userName = null;
            int remotePort=0;
            long remoteIp=0;
            String appid = "";
            String token = "";
            String internetIP = "";

            if (NetApp.mInstance.getUser().getUserName() == null) {
                return;
            }else {
                userName = NetApp.mInstance.getUser().getUserName();
            }

            for (AppInfo info:lsApp) {
                ArrayList<NetInfo> lsNet = info.lsNet;
                if (lsNet != null) {

                int operator = getInternetOperator();
                internetIP = getSecondeIP();
                if (internetIP == "") {
                    internetIP = getInternetIP();
                }
                if (operator == TELECOM) {
                    appid = getAppId(info.getPackName());
                    token = getToken(appid);
                }
                if (token == null ){
                    token = "";
                }

                if (info.getIsAccelerated() == false) {
                    if (info.getState() == 1) {     // || (info.getIsBackground() == true && info.getState()==1) ) {
                        info.setState(0);
                        for (NetInfo net:lsNet) {
                            if (net.getIsspeed() == IS_SPEED)
                            {
                                FormBody.Builder mFormBodyBuild0 = new FormBody.Builder();
                                mFormBodyBuild0.add("SpeedType", "0");
                                //mFormBodyBuild.add("Time",dateTime.getCurrentTimebyFormat("yyyyMMddHHmmss"));
                                if (userName != null) {
                                    mFormBodyBuild0.add("UserName", userName);
                                }
                                mFormBodyBuild0.add("AppName", info.getAppName());
                                mFormBodyBuild0.add("PackageName", info.getPackName());
                                mFormBodyBuild0.add("Protocol", String.valueOf(net.getType()));
                                mFormBodyBuild0.add("LocalAddress", net.getLocalAddress());
                                mFormBodyBuild0.add("LocalPort", String.valueOf(net.getLocalPort()));
                                mFormBodyBuild0.add("RemoteAddress", net.getRemoteAddress());
                                mFormBodyBuild0.add("RemotePort", String.valueOf(net.getRemotePort()));
                                mFormBodyBuild0.add("Token", net.getToken());
                                mFormBodyBuild0.add("PublicIP", net.getInternetIP());
                                mFormBodyBuild0.add("Operator", String.valueOf(operator));
                                mFormBodyBuild0.add("DataLength", String.valueOf(net.getDataLength()));
                                net.setIsspeed(NOT_SPEED_SEND);

                                FormBody mFormBody0 = mFormBodyBuild0.build();
                                Request request = new Request.Builder()
                                        .url("http://p22n940119.iok.la/AirSpeed/SpeedServlet")
                                        .post(mFormBody0)
                                        .build();
                                Log.d("dopostrequest", "have built request cancel all");
                                Log.d("delete link","all cancel link is "+net.getRemoteAddress()+" "+net.getLocalPort());

                                HttpUtil.sendOkHttpRequest(request, new okhttp3.Callback() {
                                    @Override
                                    public void onResponse(Call call, okhttp3.Response response) throws IOException {
                                        Log.d("dopostrequest", "already send and receive response all");
                                        try {
                                            Log.d("delete link", "into try all");
                                            String responseData = response.body().string();
                                            Log.d("delete link", "all remsg is "+responseData);
                                            JSONObject jsonObject = (JSONObject) new JSONObject(responseData).get("params");
                                            Log.d("delete link", "Json is decode all");

                                            ArrayList<AppInfo> lApp = NetApp.mInstance.getApp();
                                            for (AppInfo info1:lApp) {
                                                if (info1.getAppName().equals(jsonObject.getString("AppName"))
                                                        && info1.getPackName().equals(jsonObject.getString("PackageName"))) {
                                                    Log.d("delete link", "app is found all");
                                                    ArrayList<NetInfo> lNet = info1.lsNet;
                                                    for (NetInfo net:lNet) {
                                                        if (net.getType() == Integer.parseInt(jsonObject.getString("Protocol"))
                                                                && net.getLocalAddress().equals(jsonObject.getString("LocalAddress"))
                                                                && net.getLocalPort() == Integer.parseInt(jsonObject.getString("LocalPort"))
                                                                && net.getRemoteAddress().equals(jsonObject.getString("RemoteAddress"))
                                                                && net.getRemotePort() == Integer.parseInt(jsonObject.getString("RemotePort"))) {
                                                            net.setIsspeed(SERVER_NOSPEED);
                                                            Log.d("delete link", "have set nospeed all" + net.getRemoteAddress()+" "+ String.valueOf(net.getLocalPort()));
                                                        }
                                                    }
                                                }
                                            }
                                        }catch (JSONException e) {
                                            Log.d("delete speed fail all", Log.getStackTraceString(e));
                                        }
                                    }

                                    @Override
                                    public void onFailure(Call call, IOException e) {
                                        Log.d("no response all", Log.getStackTraceString(e));

                                    }
                                });
                            }
                        }
                    }
                    return;
                }

                Collections.sort(lsNet, new Comparator<NetInfo>() {
                    @Override
                    public int compare(NetInfo o1, NetInfo o2) {
                        return new Long(o2.getDeltalength() - o1.getDeltalength()).intValue();
                    }
                });

                int i = 0;
                NetInfo tmp = new NetInfo();
                //for (NetInfo tmp: lsNet) {
                for (i = lsNet.size()-1; i >=0; i--) {
                    tmp = lsNet.get(i);
                    if (tmp.getNetCount() != NetApp.mInstance.getAppCount()) {
                        continue;
                    }
                    tmp.setToken(token);
                    tmp.setInternetIP(internetIP);
                    tmp.setOperator(operator);

                    if (i < SPEED_STREAM && tmp.getDeltalength()!= 0){
                        if (tmp.getIsspeed() != IS_SPEED){  //加速新的流
                            info.setState(1);
                            tmp.setIsspeed(IS_SPEED);
                            FormBody.Builder mFormBodyBuild = new FormBody.Builder();
                            mFormBodyBuild.add("SpeedType", "1");
                            //mFormBodyBuild.add("Time",dateTime.getCurrentTimebyFormat("yyyyMMddHHmmss"));
                            if (userName != null) {
                                mFormBodyBuild.add("UserName", userName);
                            }
                            mFormBodyBuild.add("AppName", info.getAppName());
                            mFormBodyBuild.add("PackageName", info.getPackName());
                            mFormBodyBuild.add("Protocol", String.valueOf(tmp.getType()));
                            mFormBodyBuild.add("LocalAddress", tmp.getLocalAddress());
                            mFormBodyBuild.add("LocalPort", String.valueOf(tmp.getLocalPort()));
                            mFormBodyBuild.add("RemoteAddress", tmp.getRemoteAddress());
                            mFormBodyBuild.add("RemotePort", String.valueOf(tmp.getRemotePort()));
                            mFormBodyBuild.add("Token", token);
                            mFormBodyBuild.add("PublicIP", internetIP);
                            mFormBodyBuild.add("Operator", String.valueOf(operator));
                            mFormBodyBuild.add("DataLength",String.valueOf(tmp.getDataLength()));

                            FormBody mFormBody = mFormBodyBuild.build();
                            Request request = new Request.Builder()
                                    .url("http://p22n940119.iok.la/AirSpeed/SpeedServlet")
                                    .post(mFormBody)
                                    .build();
                            Log.d("dopostrequest", "have built request");
                            Log.d("max","max netinfo delta is " + i +" " + tmp.getDeltalength());
                            Log.d("max","max remote is "+ i +" " + tmp.getRemoteAddress()+" "+tmp.getRemotePort());
                            Log.d("max","max local is "+ i +" " + tmp.getLocalAddress()+" "+tmp.getLocalPort());

                            HttpUtil.sendOkHttpRequest(request, new okhttp3.Callback() {
                                @Override
                                public void onResponse(Call call, okhttp3.Response response) throws IOException {
                                    Log.d("dopostrequest", "already send and receive response speed" );
                                    try {
                                        Log.d("delete link", "into try speed" );
                                        String responseData = response.body().string();
                                        Log.d("delete link", "speed remsg is " + responseData );

                                        JSONObject jsonObject = (JSONObject) new JSONObject(responseData).get("params");
                                        Log.d("delete link", "Json is decode speed");

                                        ArrayList<AppInfo> lApp = NetApp.mInstance.getApp();
                                        for (AppInfo info1:lApp) {
                                            if (info1.getAppName().equals(jsonObject.getString("AppName"))
                                                    && info1.getPackName().equals(jsonObject.getString("PackageName"))) {
                                                Log.d("delete link", "app is found speed");
                                                ArrayList<NetInfo> lNet = info1.lsNet;
                                                for (NetInfo net:lNet) {
                                                    if (net.getType() == Integer.parseInt(jsonObject.getString("Protocol"))
                                                            && net.getLocalAddress().equals(jsonObject.getString("LocalAddress"))
                                                            && net.getLocalPort() == Integer.parseInt(jsonObject.getString("LocalPort"))
                                                            && net.getRemoteAddress().equals(jsonObject.getString("RemoteAddress"))
                                                            && net.getRemotePort() == Integer.parseInt(jsonObject.getString("RemotePort"))) {

                                                        Log.d("delete link", "have set nospeed speed" + net.getRemoteAddress()+" "+ String.valueOf(net.getLocalPort()));
                                                    }
                                                }
                                            }
                                        }
                                    }catch (JSONException e) {
                                        Log.d("delete speed fail speed", Log.getStackTraceString(e));
                                    }

                                }

                                @Override
                                public void onFailure(Call call, IOException e) {
                                    Log.d("no response speed", Log.getStackTraceString(e));

                                }
                            }); //HttpUtil.sendOkHttpRequest
                        }  //end of if (tmp.getIsspeed() != IS_SPEED) 加速新的流

                    } //end of i < SPEED_STREAM
                    else {   //(i >= SPEED_STREAM || del =0)
                        if(tmp.getIsspeed() == IS_SPEED){   //删除旧的流
                            tmp.setIsspeed(NOT_SPEED);

                            FormBody.Builder mFormBodyBuild1 = new FormBody.Builder();

                            mFormBodyBuild1.add("SpeedType", "0");
                            //mFormBodyBuild.add("Time",dateTime.getCurrentTimebyFormat("yyyyMMddHHmmss"));
                            if (userName != null) {
                                mFormBodyBuild1.add("UserName", userName);
                            }
                            mFormBodyBuild1.add("AppName", info.getAppName());
                            mFormBodyBuild1.add("PackageName", info.getPackName());
                            mFormBodyBuild1.add("Protocol",String.valueOf(tmp.getType()));
                            mFormBodyBuild1.add("LocalAddress", tmp.getLocalAddress());
                            mFormBodyBuild1.add("LocalPort", String.valueOf(tmp.getLocalPort()));
                            mFormBodyBuild1.add("RemoteAddress", tmp.getRemoteAddress());
                            mFormBodyBuild1.add("RemotePort", String.valueOf(tmp.getRemotePort()));
                            mFormBodyBuild1.add("Token", tmp.getToken());

                            mFormBodyBuild1.add("PublicIP", tmp.getInternetIP());
                            mFormBodyBuild1.add("Operator", String.valueOf(operator));
                            mFormBodyBuild1.add("DataLength",String.valueOf(tmp.getDataLength()));

                            FormBody mFormBody1 = mFormBodyBuild1.build();
                            Request request1 = new Request.Builder()
                                    .url("http://p22n940119.iok.la/AirSpeed/SpeedServlet")
                                    .post(mFormBody1)
                                    .build();
                            Log.d("dopostrequest", "have built request cancel");
                            Log.d("delete link","cancel link is "+tmp.getRemoteAddress()+" "+tmp.getLocalPort());


                            HttpUtil.sendOkHttpRequest(request1, new okhttp3.Callback() {
                                @Override
                                public void onResponse(Call call, okhttp3.Response response) throws IOException {
                                    Log.d("dopostrequest", "already send and receive response cancel");
                                    try {
                                        Log.d("delete link", "into try");
                                        String responseData = response.body().string();
                                        Log.d("delete link", "remsg is "+responseData);

                                        JSONObject jsonObject = (JSONObject) new JSONObject(responseData).get("params");
                                        Log.d("delete link", "Json is decode");

                                        ArrayList<AppInfo> lApp = NetApp.mInstance.getApp();
                                        for (AppInfo info1:lApp) {
                                            if (info1.getAppName().equals(jsonObject.getString("AppName"))
                                                    && info1.getPackName().equals(jsonObject.getString("PackageName"))) {
                                                Log.d("delete link", "app is found");
                                                ArrayList<NetInfo> lNet = info1.lsNet;
                                                for (NetInfo net:lNet) {
                                                    if (net.getType() == Integer.parseInt(jsonObject.getString("Protocol"))
                                                            && net.getLocalAddress().equals(jsonObject.getString("LocalAddress"))
                                                            && net.getLocalPort() == Integer.parseInt(jsonObject.getString("LocalPort"))
                                                            && net.getRemoteAddress().equals(jsonObject.getString("RemoteAddress"))
                                                            && net.getRemotePort() == Integer.parseInt(jsonObject.getString("RemotePort"))) {
                                                        Log.d("delete link", "have set nospeed " + net.getRemoteAddress()+" "+ String.valueOf(net.getLocalPort()));
                                                        net.setIsspeed(SERVER_NOSPEED);
                                                    }
                                                }
                                            }
                                        }
                                    }catch (JSONException e) {
                                        Log.d("delete speed fail", Log.getStackTraceString(e));
                                    }
                                }
                                @Override

                                public void onFailure(Call call, IOException e) {
                                    Log.d("no response delete", Log.getStackTraceString(e));

                                }
                            });  //end of HttpUtil.sendOkHttpRequest
                        }  //end of 删除旧的流
                    }  //end of i >= SPEED_STREAM

                } //end of for(NetInfo tmp:lsNet)
                } //if (lsNet != null)
            }  //for (AppInfo info:lsApp)
            return;
        }
        /*
                        /*以下为5流
                if (lsNet.size() <= 5)
                {
                    for (NetInfo tmp:lsNet)
                    {
                        tmp.setIsspeed(IS_SPEED);
                    }
                }else {
                    for (int i=0; i<5; i++)
                    {
                        NetInfo max = new NetInfo();
                        max.setDataLength(0);
                        for (NetInfo tmp:lsNet)
                        {
                            if (max.getDataLength() <= tmp.getDataLength())
                            {
                                max = tmp;
                            }
                        }
                        lsNet.get(lsNet.indexOf(max)).setIsspeed(IS_SPEED);

                    }
                }*/
/*
        ArrayList<NetInfo> stream = new ArrayList<>();
                max.setDeltalength(0);
                max.setDataLength(0);
        int i=0;
                for (NetInfo tmp : lsNet) {
        if (tmp.getNetCount() != NetApp.mInstance.getAppCount()) {
            continue;
        }
                    /*if (i == 0){
                        max = tmp;
                        Log.d("max netinfo", "max netinfo i is " +i);
                        i++;
                        Log.d("max netinfo", "max netinfo i++ is " +i);
                    }*/
        /*if (max.getDeltalength() <= tmp.getDeltalength() && tmp.getDeltalength() != 0) {
            max = tmp;
            Log.d("max netinfo","max netinfo delta is " + max.getDeltalength());
            Log.d("max netinfo","max remote is "+max.getRemoteAddress()+" "+max.getRemotePort());
            Log.d("max netinfo","max local is "+max.getLocalAddress()+" "+max.getLocalPort());
        }
    }
                if (lsNet.indexOf(max) < 0) {
        return;
    }


        NetInfo olditem = new NetInfo();
                for (NetInfo tmp:lsNet) {
        if (tmp.getIsspeed() == IS_SPEED) {
            remotePort = tmp.getRemotePort();
            remoteIp = tmp.getRemoteIp();
            olditem = tmp;

            //tmp.setIsspeed(NOT_SPEED);
            if (max.getRemoteIp() == remoteIp && max.getRemotePort() == remotePort
                    && max.getLocalIp() == olditem.getLocalIp()
                    && max.getLocalPort() == olditem.getLocalPort()
                    && max.getType() == olditem.getType()) {

                return;
            }
            //删除旧的流
            FormBody.Builder mFormBodyBuild1 = new FormBody.Builder();
            mFormBodyBuild1.add("SpeedType", "0");
            //mFormBodyBuild.add("Time",dateTime.getCurrentTimebyFormat("yyyyMMddHHmmss"));
            if (userName != null) {
                mFormBodyBuild1.add("UserName", userName);
            }
            mFormBodyBuild1.add("AppName", info.getAppName());
            mFormBodyBuild1.add("PackageName", info.getPackName());
            mFormBodyBuild1.add("Protocol",String.valueOf(olditem.getType()));
            mFormBodyBuild1.add("LocalAddress", olditem.getLocalAddress());
            mFormBodyBuild1.add("LocalPort", String.valueOf(olditem.getLocalPort()));
            mFormBodyBuild1.add("RemoteAddress", olditem.getRemoteAddress());
            mFormBodyBuild1.add("RemotePort", String.valueOf(olditem.getRemotePort()));
            mFormBodyBuild1.add("Token", olditem.getToken());

            mFormBodyBuild1.add("PublicIP", olditem.getInternetIP());
            mFormBodyBuild1.add("Operator", String.valueOf(operator));
            mFormBodyBuild1.add("DataLength",String.valueOf(olditem.getDataLength()));

            FormBody mFormBody1 = mFormBodyBuild1.build();
            Request request1 = new Request.Builder()
                    .url("http://p22n940119.iok.la/AirSpeed/SpeedServlet")
                    .post(mFormBody1)
                    .build();
            Log.d("dopostrequest", "have built request cancel");
            Log.d("delete link","cancel link is "+olditem.getRemoteAddress()+" "+olditem.getLocalPort());
            olditem.setIsspeed(NOT_SPEED);

            HttpUtil.sendOkHttpRequest(request1, new okhttp3.Callback() {
                @Override
                public void onResponse(Call call, okhttp3.Response response) throws IOException {
                    Log.d("dopostrequest", "already send and receive response cancel");
                    try {
                        Log.d("delete link", "into try");
                        String responseData = response.body().string();
                        Log.d("delete link", "remsg is "+responseData);

                        JSONObject jsonObject = (JSONObject) new JSONObject(responseData).get("params");
                        Log.d("delete link", "Json is decode");

                        ArrayList<AppInfo> lApp = NetApp.mInstance.getApp();
                        for (AppInfo info1:lApp) {
                            if (info1.getAppName().equals(jsonObject.getString("AppName"))
                                    && info1.getPackName().equals(jsonObject.getString("PackageName"))) {
                                Log.d("delete link", "app is found");
                                ArrayList<NetInfo> lNet = info1.lsNet;
                                for (NetInfo net:lNet) {
                                    if (net.getType() == Integer.parseInt(jsonObject.getString("Protocol"))
                                            && net.getLocalAddress().equals(jsonObject.getString("LocalAddress"))
                                            && net.getLocalPort() == Integer.parseInt(jsonObject.getString("LocalPort"))
                                            && net.getRemoteAddress().equals(jsonObject.getString("RemoteAddress"))
                                            && net.getRemotePort() == Integer.parseInt(jsonObject.getString("RemotePort"))) {
                                        Log.d("delete link", "have set nospeed " + net.getRemoteAddress()+" "+ String.valueOf(net.getLocalPort()));
                                        net.setIsspeed(SERVER_NOSPEED);
                                    }
                                }
                            }
                        }
                    }catch (JSONException e) {
                        Log.d("delete speed fail", Log.getStackTraceString(e));
                    }
                }
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.d("no response delete", Log.getStackTraceString(e));

                }
            });  //end of HttpUtil.sendOkHttpRequest

        }
    }

        //if (max.getDataLength() < 1000) {
        //  return;
        //}
        int index = lsNet.indexOf(max);
                lsNet.get(index).setIsspeed(IS_SPEED);
                lsNet.get(index).setToken(token);
                lsNet.get(index).setInternetIP(internetIP);
                lsNet.get(index).setOperator(operator);
                info.setState(1);

        //申请新的流加速
        //DateTime dateTime = new DateTime();
        FormBody.Builder mFormBodyBuild = new FormBody.Builder();
                        mFormBodyBuild.add("SpeedType", "1");
        //mFormBodyBuild.add("Time",dateTime.getCurrentTimebyFormat("yyyyMMddHHmmss"));
                        if (userName != null) {
        mFormBodyBuild.add("UserName", userName);
    }
                        mFormBodyBuild.add("AppName", info.getAppName());
                        mFormBodyBuild.add("PackageName", info.getPackName());
                        mFormBodyBuild.add("Protocol", String.valueOf(max.getType()));
                        mFormBodyBuild.add("LocalAddress", max.getLocalAddress());
                        mFormBodyBuild.add("LocalPort", String.valueOf(max.getLocalPort()));
                        mFormBodyBuild.add("RemoteAddress", max.getRemoteAddress());
                        mFormBodyBuild.add("RemotePort", String.valueOf(max.getRemotePort()));
                        mFormBodyBuild.add("Token", token);
                        mFormBodyBuild.add("PublicIP", internetIP);
                        mFormBodyBuild.add("Operator", String.valueOf(operator));
                        mFormBodyBuild.add("DataLength",String.valueOf(max.getDataLength()));

        FormBody mFormBody = mFormBodyBuild.build();
        Request request = new Request.Builder()
                .url("http://p22n940119.iok.la/AirSpeed/SpeedServlet")
                .post(mFormBody)
                .build();
                        Log.d("dopostrequest", "have built request");
                    Log.d("max","max netinfo delta is " + max.getDeltalength());
                    Log.d("max","max remote is "+max.getRemoteAddress()+" "+max.getRemotePort());
                    Log.d("max","max local is "+max.getLocalAddress()+" "+max.getLocalPort());

                        HttpUtil.sendOkHttpRequest(request, new okhttp3.Callback() {
        @Override
        public void onResponse(Call call, okhttp3.Response response) throws IOException {
            Log.d("dopostrequest", "already send and receive response speed");
            try {
                Log.d("delete link", "into try speed");
                String responseData = response.body().string();
                Log.d("delete link", "speed remsg is "+responseData);

                JSONObject jsonObject = (JSONObject) new JSONObject(responseData).get("params");
                Log.d("delete link", "Json is decode speed");

                ArrayList<AppInfo> lApp = NetApp.mInstance.getApp();
                for (AppInfo info1:lApp) {
                    if (info1.getAppName().equals(jsonObject.getString("AppName"))
                            && info1.getPackName().equals(jsonObject.getString("PackageName"))) {
                        Log.d("delete link", "app is found speed");
                        ArrayList<NetInfo> lNet = info1.lsNet;
                        for (NetInfo net:lNet) {
                            if (net.getType() == Integer.parseInt(jsonObject.getString("Protocol"))
                                    && net.getLocalAddress().equals(jsonObject.getString("LocalAddress"))
                                    && net.getLocalPort() == Integer.parseInt(jsonObject.getString("LocalPort"))
                                    && net.getRemoteAddress().equals(jsonObject.getString("RemoteAddress"))
                                    && net.getRemotePort() == Integer.parseInt(jsonObject.getString("RemotePort"))) {

                                Log.d("delete link", "have set nospeed speed" + net.getRemoteAddress()+" "+ String.valueOf(net.getLocalPort()));
                            }
                        }
                    }
                }
            }catch (JSONException e) {
                Log.d("delete speed fail speed", Log.getStackTraceString(e));
            }

        }

        @Override
        public void onFailure(Call call, IOException e) {
            Log.d("no response speed", Log.getStackTraceString(e));

        }
    });*/ //HttpUtil.sendOkHttpRequest


        public void read(int type)
        {
            try
            {
                switch (type)
                {
                    case TYPE_TCP:
                        String[] ARGS = {"cat", "/proc/net/tcp"};
                        execute(ARGS, "/", TYPE_TCP);
                        break;
                    case TYPE_TCP6:
                        String[] ARGS1 = {"cat", "/proc/net/tcp6"};
                        execute(ARGS1,"/", TYPE_TCP6 );
                        break;
                    case TYPE_UDP:
                        String[] ARGS2 = {"cat", "/proc/net/udp"};
                        execute(ARGS2, "/", TYPE_UDP);
                        break;
                    case TYPE_UDP6:
                        String[] ARGS3 = {"cat", "/proc/net/udp6"};
                        execute(ARGS3, "/", TYPE_UDP6);
                        break;
                    /*case TYPE_CONNTRACK:
                        String[] ARGS3 = {"cat", "/proc/net/nf_conntrack"};
                        execute(ARGS3, "/",TYPE_CONNTRACK);
                        break;*/
                    case TYPE_RAW:
                        String[] ARGS4 = {"cat", "/proc/net/raw"};
                        execute(ARGS4, "/",TYPE_RAW);
                        break;
                    case TYPE_RAW6:
                        String[] ARGS5 = {"cat", "/proc/net/raw6"};
                        execute(ARGS5, "/", TYPE_RAW6);
                        break;
                    default:
                        break;
                }
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }

        private void sleep(int millis)
        {
            try
            {
                Thread.sleep(millis);
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }
        @Override
        public void run()
        {
            final String PATH_TCP = "/proc/net/tcp";
            final String PATH_TCP6 = "/proc/net/tcp6";
            final String PATH_UDP = "/proc/net/udp";
            final String PATH_UDP6 = "/proc/net/udp6";
            //final String PATH_CONNTRACK = "/proc/net/nf_conntrack";
            final String PATH_RAW = "/proc/net/raw";
            final String PATH_RAW6 = "/proc/net/raw6";

            File file[] = new File[TYPE_MAX];
            file[0] = new File(PATH_TCP);
            file[1] = new File(PATH_TCP6);
            file[2] = new File(PATH_UDP);
            file[3] = new File(PATH_UDP6);
            //file[3] = new File(PATH_CONNTRACK);
            file[4] = new File(PATH_RAW);
            file[5] = new File(PATH_RAW6);

            long lastTime[] = new long[TYPE_MAX];
            Arrays.fill(lastTime, 0);

            if (NetApp.mInstance.getUser().isoutofdate()) {
                return;
            }

            while(true)
            {
                if(isStop)
                {
                    break;
                }
                if (NetApp.mInstance.getApp() == null)
                {
                    sleep(1000);
                    continue;
                }
                for (int i=0; i< TYPE_MAX; i++)  //TYPE_MAX
                {
                    if (i>0){
                        break;
                    }

                    long iTime = file[i].lastModified();
                    if (iTime != lastTime[i])
                    {
                        read(i);
                        lastTime[i] = iTime;
                    }
                }
                if (handler != null)
                {
                    handler.sendEmptyMessage(MSG_NET_CALLBACK);
                }
                sleep(500);
            }
        }


    }

    private static int getInternetOperator(){
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP_MR1) {
            return 0xff;
        }

       TelephonyManager teleM = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        if (teleM == null) {
            return 0xff;
        }

        String internetOperator = teleM.getSimOperator();
        if (internetOperator == null || "".equals(internetOperator)){
            return 0xff;
        }
        switch (internetOperator) {
            case "46000":
            case "46002":
            case "46007":
            case "46008":
                return MOBILE;
            case "46001":
            case "46006":
            case "46009":
                return UNICOM;
            case "46003":
            case "46005":
            case "46011":
                return TELECOM;

        }
        return 0xff;
    }

    private static String getAppId(String packagename) {
        switch (packagename) {
            case "com.eg.android.AlipayGphone":
            case "com.baidu.wallet":
            case "com.wangyin.payment":
            case "com.tencent.tmgp.sgame":
            case "com.tencent.cldts":
            case "com.netease.zjz":
            case "com.netease.hyxd":
            case "com.tencent.tmgp.pubgmhd":
            case "com.tencent.tmgp.pubgm":
                return "bcm";
            case "com.ss.android.ugc.aweme":
            case "com.smile.gifmaker":
            case "com.ss.android.ugc.live":
            case "com.yixia.videoeditor":
            case "com.youku.phone":
            case "com.qiyi.video":
            case "com.tencent.qqlive":
            case "air.tv.douyu.android":
            case "com.panda.videoliveplatform":
            case "com.taobao.taobao":
            case "com.jingdong.app.mall":
            case "com.xuemeng.pingduoduo":
            case "com.sankuai.meituan":
            case "com.suning.mobile.ebuy":
            case "com.taobao.idlefish":
            case "cn.missfresh.application":
            case "ctrip.android.view":
            case "com.Qunar":
            case "com.taobao.trip":
            case "com.dp.android.elong":
            case "com.tuniu.app.ui":
            case "com.sankuai.meituan.takeoutnew":
            case "me.ele":
            case "com.dianping.v1":
                return "bcm4M";
            default:
                return "bcm2M";
        }
    }

    private static String getToken(String appid) {
        String token = "";
        try {
            String address = "http://qos.189.cn/qos-api/getToken?appid=" + appid;
            URL url = new URL(address);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setUseCaches(false);
            conn.setRequestMethod("GET");

            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                InputStream in = conn.getInputStream();
                //流转换为字符串
                BufferedReader reader = new BufferedReader(new InputStreamReader(in));

                String tmp = "";
                StringBuilder retJSON = new StringBuilder();
                while ((tmp = reader.readLine()) != null) {
                    retJSON.append(tmp + "\n");
                }
                in.close();

                try {
                    JSONObject jsonObject = new JSONObject(retJSON.toString());
                    token = jsonObject.getString("result");
                    return token;
                }catch (JSONException e){
                    e.printStackTrace();
                }
            }
        }catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static String getInternetIP() {
        String ip = "";
        try {
            String address = "http://ip.taobao.com/service/getIpInfo2.php?ip=myip";
            URL url = new URL(address);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setUseCaches(false);
            conn.setRequestMethod("GET");
            conn.setRequestProperty("user-agent","Mozilla/5.0(Windows NT 6.3;WOW64)  " +
                    "AppleWebKit/537.36(KHTML,like Gecko) Chrome/51.0.2704.7 " +
                    "Safari/537.36");

            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                InputStream in = conn.getInputStream();

                BufferedReader reader = new BufferedReader(new InputStreamReader(in));

                String tmp = "";
                StringBuilder reJSON = new StringBuilder();
                while ((tmp = reader.readLine()) != null) {
                    reJSON.append(tmp + "\n");
                }

                JSONObject jsonObject = new JSONObject(reJSON.toString());
                String code = jsonObject.getString("code");

                if (code.equals("0")) {
                    JSONObject data = jsonObject.getJSONObject("data");
                    ip = data.getString("ip");
                    return ip;
                }
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
        return ip;
    }

    public static String getSecondeIP() {
        String ip = "";
        try {
            String address = "http://pv.sohu.com/cityjson?ie=utf-8";
            URL url = new URL(address);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setUseCaches(false);
            conn.setRequestMethod("GET");
            conn.setRequestProperty("user-agent","Mozilla/5.0(Windows NT 6.3;WOW64)  " +
                    "AppleWebKit/537.36(KHTML,like Gecko) Chrome/51.0.2704.7 " +
                    "Safari/537.36");

            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                InputStream in = conn.getInputStream();

                BufferedReader reader = new BufferedReader(new InputStreamReader(in));

                String tmp = "";
                StringBuilder reJSON = new StringBuilder();
                while ((tmp = reader.readLine()) != null) {
                    reJSON.append(tmp + "\n");
                }
                in.close();
                int start = reJSON.indexOf("{");
                int end = reJSON.indexOf("}");
                String json = reJSON.substring(start, end+1);
                if (json != null) {
                    try {
                        JSONObject jsonObject = new JSONObject(json);
                        ip = jsonObject.getString("cip");
                        return  ip;
                    }catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
        return ip;
    }
}




























