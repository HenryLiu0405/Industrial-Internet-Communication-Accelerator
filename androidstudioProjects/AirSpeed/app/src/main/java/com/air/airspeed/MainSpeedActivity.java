package com.air.airspeed;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.NetworkInfo;
import android.net.VpnService;
import android.os.Build;
import android.os.IBinder;
import android.os.SystemClock;
import android.provider.Telephony;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.util.SparseArray;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;

import com.air.airspeed.bottombar.BottomBar;
import com.air.airspeed.bottombar.fragment.Fragment1;
import com.air.airspeed.bottombar.fragment.Fragment2;
import com.air.airspeed.bottombar.fragment.Fragment3;
import com.air.airspeed.vpn.CommonMethods;
import com.air.airspeed.vpn.IPHeader;
import com.air.airspeed.vpn.LocalVpnService;
import com.air.airspeed.vpn.NatSession;
import com.air.airspeed.vpn.NatSessionManager;
import com.air.airspeed.vpn.Packet;
import com.air.airspeed.vpn.VpnServiceHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import okhttp3.Call;
import okhttp3.FormBody;
import okhttp3.Request;

import static android.net.NetworkInfo.State.UNKNOWN;
import static com.air.airspeed.vpn.VpnServiceHelper.START_VPN_SERVICE_REQUEST_CODE;
import static java.lang.Thread.sleep;

public class MainSpeedActivity extends AppCompatActivity {
    public final static int TYPE_TCP = 0;
    public final static int TYPE_TCP6 = 1;
    public final static int TYPE_UDP = 2;
    public final static int TYPE_UDP6 = 3;
    //public final static int TYPE_CONNTRACK = 3;
    public final static int TYPE_RAW = 4;
    public final static int TYPE_RAW6 = 5;
    public final static int TYPE_MAX = 6;

    private final static int IS_SPEED = 1;
    private final static int NOT_SPEED = 0;
    private final static int NOT_SPEED_SEND = 254;
    private final static int SERVER_NOSPEED = 255;
    private final static int TCP_ESTABLISHED = 1;

    private final static int SPEED_STREAM = 5;
    private final static int MOBILE = 0;
    private final static int UNICOM = 1;
    private final static int TELECOM = 2;


    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private BrowseAppInfoAdapter browseAppInfoAdapter;
    private boolean isFirst;
    //private LocalVpnService.StreamBinder streamBinder;

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_speed);

        switch (0) {
            case 1:
                JSONObject jsoObj;
                String date = null;
                String second = null;
                try {
                    jsoObj = new JSONObject();
                    date = jsoObj.getString("date");
                    second = jsoObj.getString("second");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                test.settime(date, second);
                break;
        }


        BottomBar bottomBar = (BottomBar) findViewById(R.id.bottom_bar);
        bottomBar.setContainer(R.id.f1_container)
                .setTitleBeforeAndAfterColor("#999999", "#ff5d5e")
                .setFirstChecked(1)
                .setTitleSize(12)
                .setTitleIconMargin(1)
                .setIconHeight(40)
                .setIconWidth(40)
                .addItem(Fragment1.class, "应用", R.drawable.item1_before, R.drawable.item1_after)
                .addItem(Fragment2.class, "加速", R.drawable.item2_before, R.drawable.item2_after)
                .addItem(Fragment3.class, "我的", R.drawable.item3_before, R.drawable.item3_after)
                .build();
        bottomBar.setOnTouchListener(new BottomTouchListener());

        //pref = getSharedPreferences("UserInfo",MODE_PRIVATE);
        //deleteSharedPreferencesFile(pref);

        pref = getSharedPreferences("UserInfo", MODE_PRIVATE);

        if (NetApp.mInstance.isFirstTime == true) {
            NetApp.mInstance.isFirstTime = false;
            try {
                setAppViewfromShared();
                setApps2NetApp();

                boolean isSaved = pref.getBoolean("isSaved", false);
                boolean rememberpwd = pref.getBoolean("rememberpwd", false);
                if (isSaved && rememberpwd) {
                    login();
                } else {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setMessage("请先登录/注册");
                    builder.setTitle("提示");
                    builder.setPositiveButton("确认", new android.content.DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface arg0, int arg1) {
                            //跳到登录界面
                            Intent intent = new Intent(MainSpeedActivity.this, LoginActivity.class);
                            startActivity(intent);

                            arg0.dismiss();
                        }
                    });
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        } else {
            setAppViewfromNetApp();
            Log.d("MainSpeed", "isFirst is false");
        }

        //Intent bindIntent = new Intent(this, LocalVpnService.class);
        //bindService(bindIntent, connection, BIND_WAIVE_PRIORITY);

    }

    class BottomTouchListener implements View.OnTouchListener {
        int target = -1;

        @Override
        public boolean onTouch(View view, MotionEvent event) {
            int viewWidth = view.getWidth() / 3;
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    target = (int) event.getX() / viewWidth;
                    break;
                case MotionEvent.ACTION_UP:
                    if (event.getY() < 0) {
                        break;
                    }
                    if (target == ((int) event.getX() / viewWidth)) {
                        switch (target) {
                            case 0:
                                Intent intent = new Intent(MainSpeedActivity.this, MainActivity.class);
                                startActivity(intent);
                                break;
                            case 2:
                                Intent intent1 = new Intent(MainSpeedActivity.this, LoginSuccessActivity.class);
                                startActivity(intent1);
                                break;
                        }
                    }
                    break;
            }
            return false;
        }
    }


    private void login() {
        final String username = pref.getString("UserName", "");
        final String password = pref.getString("Password", "");

        FormBody.Builder mFormBodyBuild = new FormBody.Builder();
        mFormBodyBuild.add("UserName", username);
        mFormBodyBuild.add("Password", password);

        FormBody mFormBody = mFormBodyBuild.build();
        Request request = new Request.Builder()
                .url("http://p22n940119.iok.la/AirSpeed/LoginServlet")
                .post(mFormBody)
                .build();
        Log.d("MainSpeedActivity", "have built login request");

        HttpUtil.sendOkHttpRequest(request, new okhttp3.Callback() {
            @Override
            public void onResponse(Call call, okhttp3.Response response) throws IOException {
                Log.d("MainSpeedActivity", "already send and receive response");
                try {
                    String responseData = response.body().string();
                    JSONObject jsonObject = (JSONObject) new JSONObject(responseData).get("params");
                    String result = jsonObject.getString("Result");
                    if (result.equals("success")) {
                        User user = new User();
                        if (!username.equals(jsonObject.getString("UserName")) || !password.equals(jsonObject.getString("Password"))) {
                            return;
                        }
                        user.setUserName(username);
                        user.setPassWord(password);
                        user.setDueDate(jsonObject.getString("MemberDueDate"));
                        user.setCredits(jsonObject.getInt("Credits"));
                        user.setVersion(jsonObject.getString("Version"));
                        user.setHeadPic(jsonObject.getString("HeadPic"));
                        saveInfo(user);

                        Boolean isSelectApps = pref.getBoolean("isSelectApps", false);
                        if (isSelectApps) {
                            MainSpeedActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    //showApps();
                                }
                            });
                        } else {
                            MainSpeedActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    final AlertDialog.Builder builder = new AlertDialog.Builder(MainSpeedActivity.this);
                                    builder.setMessage("请先选择需要加速的应用");
                                    builder.setTitle("提示");
                                    builder.setPositiveButton("确认", new android.content.DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface arg0, int arg1) {
                                            arg0.dismiss();
                                            Intent intent = new Intent(MainSpeedActivity.this, MainActivity.class);
                                            startActivity(intent);
                                        }
                                    });
                                    AlertDialog dialog = builder.create();
                                    dialog.show();

                                }
                            });
                        }

                    } else if (result.equals("outofdate")) {
                        MainSpeedActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                final AlertDialog.Builder builder = new AlertDialog.Builder(MainSpeedActivity.this);
                                builder.setMessage("账号已过期，请购买会员");
                                builder.setTitle("提示");
                                builder.setPositiveButton("确认", new android.content.DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface arg0, int arg1) {

                                        arg0.dismiss();
                                    }
                                });
                                AlertDialog dialog = builder.create();
                                dialog.show();

                            }
                        });
                        try {
                            sleep(2000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        //save info to sharedpreference
                        User user = new User();
                        user.setUserName(jsonObject.getString("UserName"));
                        user.setPassWord(jsonObject.getString("Password"));
                        user.setDueDate(jsonObject.getString("MemberDueDate"));
                        user.setCredits(jsonObject.getInt("Credits"));
                        user.setVersion(jsonObject.getString("Version"));
                        user.setHeadPic(jsonObject.getString("HeadPic"));
                        saveInfo(user);
                        Intent intent = new Intent(MainSpeedActivity.this, MemberCenterActivity.class);
                        startActivity(intent);
                    } else {
                        MainSpeedActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                final AlertDialog.Builder builder = new AlertDialog.Builder(MainSpeedActivity.this);
                                builder.setMessage("账号或密码错误");
                                builder.setTitle("提示");
                                builder.setPositiveButton("确认", new android.content.DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface arg0, int arg1) {

                                        arg0.dismiss();
                                    }
                                });
                                AlertDialog dialog = builder.create();
                                dialog.show();

                            }
                        });
                    }
                } catch (JSONException e) {
                    Log.e("TAG", e.getMessage(), e);
                    Log.d("JSonException", Log.getStackTraceString(e));
                    MainSpeedActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            final AlertDialog.Builder builder = new AlertDialog.Builder(MainSpeedActivity.this);
                            builder.setMessage("服务器返回异常，请重试");
                            builder.setTitle("提示");
                            builder.setPositiveButton("确认", new android.content.DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface arg0, int arg1) {

                                    arg0.dismiss();
                                }
                            });
                            AlertDialog dialog = builder.create();
                            dialog.show();

                        }
                    });
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {
                Log.d("fail response", Log.getStackTraceString(e));
                MainSpeedActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        final AlertDialog.Builder builder = new AlertDialog.Builder(MainSpeedActivity.this);
                        builder.setMessage("网络无连接，请稍后再试");
                        builder.setTitle("提示");
                        builder.setPositiveButton("确认", new android.content.DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {

                                arg0.dismiss();
                            }
                        });
                        AlertDialog dialog = builder.create();
                        dialog.show();

                    }
                });
            }
        });
    }

    private void setApps2NetApp() {
        String packageNames = pref.getString("PackageNames", "");
        String userName = pref.getString("UserName", "");
        String[] packageName;
        String regular = "#";
        User user = new User();

        user.setUserName(userName);
        user.setPassWord(pref.getString("Password", ""));

        /*editor = pref.edit();
        editor.putString("MemberDueDate","2019-02-28 10:00:00");
        editor.putString("Version", "1.0");
        editor.putString("Credits","20");
        editor.commit();*/
        //String c = pref.getString("Credits","");
        user.setDueDate(pref.getString("MemberDueDate", ""));
        String credits = pref.getString("Credits", "");
        if (credits.equals("")) {
            user.setCredits(0);
        } else {
            user.setCredits(Integer.parseInt(pref.getString("Credits", "")));
        }
        user.setVersion(pref.getString("Version", ""));
        user.setHeadPic(pref.getString("HeadPic", ""));
        NetApp.mInstance.setUser(user);

        //startService(new Intent(MainSpeedActivity.this, LifeService.class));
        //startService(new Intent(MainSpeedActivity.this, NetService.class));

        //startService(new Intent(MainSpeedActivity.this,MinaService.class));
        //startHeart();

        if (packageNames == "") {
            return;
        }
        packageNames = packageNames.substring(1);
        Log.d("showApp", "packageNames is " + packageNames);


        if (packageNames == null || packageNames == "") {
            return;
        }
        packageName = packageNames.split(regular);

        /*start APP*/
        App app = new App(this);
        app.setOnApp(new App.OnAppListen() {
            @Override
            public void getApp(ArrayList<AppInfo> lsApp) {
                if (lsApp == null) {
                    Log.d("MainSpeedActivity", "lsApp is null");
                    return;
                }
                //startService(new Intent(MainSpeedActivity.this, NetService.class));
                //startService(new Intent(MainSpeedActivity.this, LifeService.class));
            }
        });
        app.execute(packageName);
        //startService(new Intent(MainSpeedActivity.this, NetService.class));
        //startService(new Intent(MainSpeedActivity.this, LifeService.class));

        //setAppView();
    }

    private void setAppViewfromNetApp() {
        List<AppInfo> mlistAppInfo;

        mlistAppInfo = NetApp.mInstance.getApp();

        if (mlistAppInfo == null) {
            Log.d("setviewfromNet", "NetApp is null");

            return;
        }
        Log.d("setviewfromNet", "NetApp is " + mlistAppInfo.size());
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_viewApp);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        browseAppInfoAdapter = new BrowseAppInfoAdapter(this, mlistAppInfo);
        browseAppInfoAdapter.setVpnInterface(this, new BrowseAppInfoAdapter.VpnInterface() {
            @Override
            public void openVpn(Context context) {
                       /* Intent intent = LocalVpnService.prepare(context);
                        if (intent != null) {
                            startActivityForResult(intent, 1);
                        }else {
                            onActivityResult(1, RESULT_OK, null);
                        }*/
                VpnServiceHelper.changeVpnRunningStatus(context, true);
            }
        });
        recyclerView.setAdapter(browseAppInfoAdapter);
    }

    private void setAppViewfromShared() throws PackageManager.NameNotFoundException {
        String packageNames = pref.getString("PackageNames", "");
        String[] packageName;
        String regular = "#";

        if (packageNames == null || packageNames == "") {
            return;
        }
        packageNames = packageNames.substring(1);
        packageName = packageNames.split(regular);

        ArrayList<AppInfo> mlistAppInfo = new ArrayList<>();
        AppInfo appInfo;
        PackageManager pm = this.getApplicationContext().getPackageManager();
        PackageInfo info = null;

        for (String sTmp : packageName) {
            info = pm.getPackageInfo(sTmp, 0);
            appInfo = new AppInfo();

            appInfo.setAppName(info.applicationInfo.loadLabel(pm).toString());
            appInfo.setPackName(sTmp);
            appInfo.setUid(info.applicationInfo.uid);
            appInfo.setIcon(info.applicationInfo.loadIcon(pm));
            appInfo.setVersion(info.versionName);
            appInfo.setIsAccelerated(false);

            mlistAppInfo.add(appInfo);
        }

        if (mlistAppInfo == null) {
            return;
        }
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_viewApp);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        browseAppInfoAdapter = new BrowseAppInfoAdapter(this, mlistAppInfo);
        browseAppInfoAdapter.setVpnInterface(this, new BrowseAppInfoAdapter.VpnInterface() {
            @Override
            public void openVpn(Context context) {
                /*Intent intent = LocalVpnService.prepare(context);
                if (intent != null) {
                    startActivityForResult(intent, 1);
                }else {
                    onActivityResult(1, RESULT_OK, null);
                }*/
                VpnServiceHelper.changeVpnRunningStatus(context, true);
            }
        });
        recyclerView.setAdapter(browseAppInfoAdapter);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        int position = browseAppInfoAdapter.getPosition();
        AppInfo appInfo = browseAppInfoAdapter.getApp(position);

        ArrayList<AppInfo> mlist = NetApp.mInstance.getApp();
        Log.d("deletebrowse", "after delete browse NetApp is " + mlist.size());

        browseAppInfoAdapter.removeItem(position);
        Log.d("onContextItemSelected", "position and package is " + position + appInfo.getPackName());

        ArrayList<AppInfo> mlistafter = NetApp.mInstance.getApp();
        Log.d("deletebrowse", "after delete browse NetApp is " + mlistafter.size());
        deletefromsharedpreferences(appInfo);
        //detetefromNetApp(appInfo);

        return super.onContextItemSelected(item);
    }

    public void deletefromsharedpreferences(AppInfo appInfo) {
        String packageNames = pref.getString("PackageNames", "");

        String deletePackage = appInfo.getPackName();

        deletePackage = "#" + deletePackage;
        packageNames = packageNames.replace(deletePackage, "");
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("PackageNames", packageNames);
        if (packageNames == "") {
            editor.putBoolean("isSelectApps", false);
            Log.d("deleteshared", "shared is null");
        }
        editor.commit();
    }

    public void deleteSharedPreferencesFile(SharedPreferences pref) {
        SharedPreferences.Editor editor = pref.edit();
        editor.clear();
        editor.commit();
    }

    public void detetefromNetApp(AppInfo appInfo) {
        ArrayList<AppInfo> mlistAppInfo;
        mlistAppInfo = NetApp.mInstance.getApp();

        if (mlistAppInfo == null) {
            Log.d("deleteNet", "NetApp is null");
            return;
        }
        Log.d("deleteNet", "NetApp is " + mlistAppInfo.size());

        for (AppInfo sTmp : mlistAppInfo) {
            Log.d("deleteNet", "sTmp is " + sTmp.getPackName() + sTmp.getAppName() + sTmp.getUid());
            Log.d("deleteNet", "appInfo is " + appInfo.getPackName() + appInfo.getAppName() + appInfo.getUid());
            if (sTmp.getAppName().equals(appInfo.getAppName()) && sTmp.getPackName().equals(appInfo.getPackName())
                    && sTmp.getUid() == appInfo.getUid()) {
                mlistAppInfo.remove(sTmp);
                NetApp.mInstance.setApp(mlistAppInfo);
                Log.d("deleteNet", "NetApp is removed");
                break;
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("isFirst", isFirst);
        Log.d("MainSpeed", "isFirst is already saved");
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        isFirst = savedInstanceState.getBoolean("isFirst");
        Log.d("MainSpeed", "isFirst is " + isFirst);

    }

    @SuppressLint("WrongConstant")
    private void saveInfo(User user) {
        pref = getSharedPreferences("UserInfo", MODE_PRIVATE);
        editor = pref.edit();

        editor.putBoolean("isSaved", true);
        editor.putString("UserName", user.getUserName());
        editor.putString("MemberDueDate", user.getDueDate());
        editor.putString("Credits", String.valueOf(user.getCredits()));
        editor.putString("Version", user.getVersion());
        editor.putString("HeadPic", user.getHeadPic());
        editor.putBoolean("rememberpwd", true);
        editor.putString("Password", user.getPassWord());
        editor.commit();

        NetApp.mInstance.setUser(user);
    }

    private void startHeart() {
        Log.d("heart", "heart is started");
        AlarmManager manager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(MainSpeedActivity.this, MinaService.class);
        //startService(intent);
        PendingIntent pd = PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        //long trigger = SystemClock.elapsedRealtime();
        if (manager != null) {
            manager.setRepeating(AlarmManager.RTC_WAKEUP, 5, 60 * 1000, pd);
        }

    }

    /*@Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == RESULT_OK) {
            NatSessionManager.clearAllSession();
            Intent intent = new Intent(this, LocalVpnService.class);
            startService(intent);
        }
    }*/

    @Override
    protected void onDestroy() {
        //unbindService(connection);
        super.onDestroy();
    }

    /*private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, final IBinder service) {
            streamBinder = (LocalVpnService.StreamBinder) service;
            LocalVpnService service2 = streamBinder.getService();

            //重写实现回调，获取加速五元组，并向服务器发送加速请求
            service2.setCallback(new LocalVpnService.Callback() {
                //重写获取五元组函数，实现真正的回调
                @Override
                public void getSpeedStream() {
                    ArrayList<AppInfo> lsApp = NetApp.mInstance.getApp();
                    for (AppInfo sTmp : lsApp) {
                        if (sTmp.getIsAccelerated() == true) {
                            ArrayList<NetInfo> lsNet = new ArrayList<>();

                            Set<Map.Entry<Integer, NatSession>> entries = NatSessionManager.sessions.entrySet();
                            Iterator<Map.Entry<Integer, NatSession>> iterator = entries.iterator();
                            while (iterator.hasNext()) {
                                Map.Entry<Integer, NatSession> next = iterator.next();
                                NatSession session = next.getValue();
                                boolean isfound = false;
                                if (sTmp.lsNet == null) {
                                    sTmp.lsNet = lsNet;
                                }
                                for (NetInfo sNet : lsNet) {

                                    if (session.localIP == sNet.getLocalIp()
                                            && session.localPort == sNet.getLocalPort()
                                            && session.remotePort == sNet.getRemotePort()
                                            && session.remoteIP == sNet.getRemoteIp()
                                            && session.Protocol == sNet.getType()) {
                                        long dataLength = (long) (session.PacketRev + session.packetSent);
                                        long deltaLength = dataLength - sNet.getDataLength();
                                        sNet.setDataLength(dataLength);
                                        sNet.setDeltalength(deltaLength);
                                        sNet.setLocalAddress(CommonMethods.ipLongToString(sNet.getLocalIp()));
                                        sNet.setRemoteAddress(CommonMethods.ipLongToString(sNet.getRemoteIp()));
                                        isfound = true;
                                        break;
                                    }
                                }
                                if (!isfound) {
                                    NetInfo netinfo = new NetInfo();
                                    netinfo.setLocalIp(session.localIP);
                                    netinfo.setLocalPort(session.localPort);
                                    netinfo.setRemoteIp(session.remoteIP);
                                    netinfo.setRemotePort(session.remotePort);
                                    netinfo.setType(session.Protocol);
                                    netinfo.setDataLength((long) (session.packetSent + session.PacketRev));
                                    netinfo.setDeltalength((long) (session.packetSent + session.PacketRev));
                                    netinfo.setLocalAddress(CommonMethods.ipLongToString(netinfo.getLocalIp()));
                                    netinfo.setRemoteAddress(CommonMethods.ipLongToString(netinfo.getRemoteIp()));
                                    lsNet.add(netinfo);
                                }
                            }
                            break;

                        }
                    }
                    findSpeedItemandreport();
                }
            });
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    private void findSpeedItemandreport() {
        ArrayList<AppInfo> lsApp = NetApp.mInstance.getApp();
        String userName = null;
        int remotePort = 0;
        long remoteIp = 0;
        String appid = "";
        String token = "";
        String internetIP = "";

        if (NetApp.mInstance.getUser().getUserName() == null) {
            return;
        } else {
            userName = NetApp.mInstance.getUser().getUserName();
        }

        for (AppInfo info : lsApp) {
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
                if (token == null) {
                    token = "";
                }

                if (info.getIsAccelerated() == false) {
                    if (info.getState() == 1) {     // || (info.getIsBackground() == true && info.getState()==1) ) {
                        info.setState(0);
                        for (NetInfo net : lsNet) {
                            if (net.getIsspeed() == IS_SPEED) {
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
                                Log.d("delete link", "all cancel link is " + net.getRemoteAddress() + " " + net.getLocalPort());

                                HttpUtil.sendOkHttpRequest(request, new okhttp3.Callback() {
                                    @Override
                                    public void onResponse(Call call, okhttp3.Response response) throws IOException {
                                        Log.d("dopostrequest", "already send and receive response all");
                                        try {
                                            Log.d("delete link", "into try all");
                                            String responseData = response.body().string();
                                            Log.d("delete link", "all remsg is " + responseData);
                                            JSONObject jsonObject = (JSONObject) new JSONObject(responseData).get("params");
                                            Log.d("delete link", "Json is decode all");

                                            ArrayList<AppInfo> lApp = NetApp.mInstance.getApp();
                                            for (AppInfo info1 : lApp) {
                                                if (info1.getAppName().equals(jsonObject.getString("AppName"))
                                                        && info1.getPackName().equals(jsonObject.getString("PackageName"))) {
                                                    Log.d("delete link", "app is found all");
                                                    ArrayList<NetInfo> lNet = info1.lsNet;
                                                    for (NetInfo net : lNet) {
                                                        if (net.getType() == Integer.parseInt(jsonObject.getString("Protocol"))
                                                                && net.getLocalAddress().equals(jsonObject.getString("LocalAddress"))
                                                                && net.getLocalPort() == Integer.parseInt(jsonObject.getString("LocalPort"))
                                                                && net.getRemoteAddress().equals(jsonObject.getString("RemoteAddress"))
                                                                && net.getRemotePort() == Integer.parseInt(jsonObject.getString("RemotePort"))) {
                                                            net.setIsspeed(SERVER_NOSPEED);
                                                            Log.d("delete link", "have set nospeed all" + net.getRemoteAddress() + " " + String.valueOf(net.getLocalPort()));
                                                        }
                                                    }
                                                }
                                            }
                                        } catch (JSONException e) {
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
                for (i = lsNet.size() - 1; i >= 0; i--) {
                    tmp = lsNet.get(i);
                    if (tmp.getNetCount() != NetApp.mInstance.getAppCount()) {
                        continue;
                    }
                    tmp.setToken(token);
                    tmp.setInternetIP(internetIP);
                    tmp.setOperator(operator);

                    if (i < SPEED_STREAM && tmp.getDeltalength() != 0) {
                        if (tmp.getIsspeed() != IS_SPEED) {  //加速新的流
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
                            mFormBodyBuild.add("DataLength", String.valueOf(tmp.getDataLength()));

                            FormBody mFormBody = mFormBodyBuild.build();
                            Request request = new Request.Builder()
                                    .url("http://p22n940119.iok.la/AirSpeed/SpeedServlet")
                                    .post(mFormBody)
                                    .build();
                            Log.d("dopostrequest", "have built request");
                            Log.d("max", "max netinfo delta is " + i + " " + tmp.getDeltalength());
                            Log.d("max", "max remote is " + i + " " + tmp.getRemoteAddress() + " " + tmp.getRemotePort());
                            Log.d("max", "max local is " + i + " " + tmp.getLocalAddress() + " " + tmp.getLocalPort());

                            HttpUtil.sendOkHttpRequest(request, new okhttp3.Callback() {
                                @Override
                                public void onResponse(Call call, okhttp3.Response response) throws IOException {
                                    Log.d("dopostrequest", "already send and receive response speed");
                                    try {
                                        Log.d("delete link", "into try speed");
                                        String responseData = response.body().string();
                                        Log.d("delete link", "speed remsg is " + responseData);

                                        JSONObject jsonObject = (JSONObject) new JSONObject(responseData).get("params");
                                        Log.d("delete link", "Json is decode speed");

                                        ArrayList<AppInfo> lApp = NetApp.mInstance.getApp();
                                        for (AppInfo info1 : lApp) {
                                            if (info1.getAppName().equals(jsonObject.getString("AppName"))
                                                    && info1.getPackName().equals(jsonObject.getString("PackageName"))) {
                                                Log.d("delete link", "app is found speed");
                                                ArrayList<NetInfo> lNet = info1.lsNet;
                                                for (NetInfo net : lNet) {
                                                    if (net.getType() == Integer.parseInt(jsonObject.getString("Protocol"))
                                                            && net.getLocalAddress().equals(jsonObject.getString("LocalAddress"))
                                                            && net.getLocalPort() == Integer.parseInt(jsonObject.getString("LocalPort"))
                                                            && net.getRemoteAddress().equals(jsonObject.getString("RemoteAddress"))
                                                            && net.getRemotePort() == Integer.parseInt(jsonObject.getString("RemotePort"))) {

                                                        Log.d("delete link", "have set nospeed speed" + net.getRemoteAddress() + " " + String.valueOf(net.getLocalPort()));
                                                    }
                                                }
                                            }
                                        }
                                    } catch (JSONException e) {
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
                        if (tmp.getIsspeed() == IS_SPEED) {   //删除旧的流
                            tmp.setIsspeed(NOT_SPEED);

                            FormBody.Builder mFormBodyBuild1 = new FormBody.Builder();

                            mFormBodyBuild1.add("SpeedType", "0");
                            //mFormBodyBuild.add("Time",dateTime.getCurrentTimebyFormat("yyyyMMddHHmmss"));
                            if (userName != null) {
                                mFormBodyBuild1.add("UserName", userName);
                            }
                            mFormBodyBuild1.add("AppName", info.getAppName());
                            mFormBodyBuild1.add("PackageName", info.getPackName());
                            mFormBodyBuild1.add("Protocol", String.valueOf(tmp.getType()));
                            mFormBodyBuild1.add("LocalAddress", tmp.getLocalAddress());
                            mFormBodyBuild1.add("LocalPort", String.valueOf(tmp.getLocalPort()));
                            mFormBodyBuild1.add("RemoteAddress", tmp.getRemoteAddress());
                            mFormBodyBuild1.add("RemotePort", String.valueOf(tmp.getRemotePort()));
                            mFormBodyBuild1.add("Token", tmp.getToken());

                            mFormBodyBuild1.add("PublicIP", tmp.getInternetIP());
                            mFormBodyBuild1.add("Operator", String.valueOf(operator));
                            mFormBodyBuild1.add("DataLength", String.valueOf(tmp.getDataLength()));

                            FormBody mFormBody1 = mFormBodyBuild1.build();
                            Request request1 = new Request.Builder()
                                    .url("http://p22n940119.iok.la/AirSpeed/SpeedServlet")
                                    .post(mFormBody1)
                                    .build();
                            Log.d("dopostrequest", "have built request cancel");
                            Log.d("delete link", "cancel link is " + tmp.getRemoteAddress() + " " + tmp.getLocalPort());


                            HttpUtil.sendOkHttpRequest(request1, new okhttp3.Callback() {
                                @Override
                                public void onResponse(Call call, okhttp3.Response response) throws IOException {
                                    Log.d("dopostrequest", "already send and receive response cancel");
                                    try {
                                        Log.d("delete link", "into try");
                                        String responseData = response.body().string();
                                        Log.d("delete link", "remsg is " + responseData);

                                        JSONObject jsonObject = (JSONObject) new JSONObject(responseData).get("params");
                                        Log.d("delete link", "Json is decode");

                                        ArrayList<AppInfo> lApp = NetApp.mInstance.getApp();
                                        for (AppInfo info1 : lApp) {
                                            if (info1.getAppName().equals(jsonObject.getString("AppName"))
                                                    && info1.getPackName().equals(jsonObject.getString("PackageName"))) {
                                                Log.d("delete link", "app is found");
                                                ArrayList<NetInfo> lNet = info1.lsNet;
                                                for (NetInfo net : lNet) {
                                                    if (net.getType() == Integer.parseInt(jsonObject.getString("Protocol"))
                                                            && net.getLocalAddress().equals(jsonObject.getString("LocalAddress"))
                                                            && net.getLocalPort() == Integer.parseInt(jsonObject.getString("LocalPort"))
                                                            && net.getRemoteAddress().equals(jsonObject.getString("RemoteAddress"))
                                                            && net.getRemotePort() == Integer.parseInt(jsonObject.getString("RemotePort"))) {
                                                        Log.d("delete link", "have set nospeed " + net.getRemoteAddress() + " " + String.valueOf(net.getLocalPort()));
                                                        net.setIsspeed(SERVER_NOSPEED);
                                                    }
                                                }
                                            }
                                        }
                                    } catch (JSONException e) {
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

    private int getInternetOperator() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP_MR1) {
            return 0xff;
        }

        TelephonyManager teleM = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        if (teleM == null) {
            return 0xff;
        }

        String internetOperator = teleM.getSimOperator();
        if (internetOperator == null || "".equals(internetOperator)) {
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
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
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
            conn.setRequestProperty("user-agent", "Mozilla/5.0(Windows NT 6.3;WOW64)  " +
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
        } catch (Exception e) {
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
            conn.setRequestProperty("user-agent", "Mozilla/5.0(Windows NT 6.3;WOW64)  " +
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
                String json = reJSON.substring(start, end + 1);
                if (json != null) {
                    try {
                        JSONObject jsonObject = new JSONObject(json);
                        ip = jsonObject.getString("cip");
                        return ip;
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ip;
    }*/

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        NatSessionManager.clearAllSession();
        if (requestCode == START_VPN_SERVICE_REQUEST_CODE && resultCode == RESULT_OK) {
            VpnServiceHelper.startVpnService(getApplicationContext());
        }
    }
}
