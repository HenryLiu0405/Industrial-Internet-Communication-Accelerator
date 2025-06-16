package com.air.airspeed;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Point;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.air.airspeed.bottombar.BottomBar;
import com.air.airspeed.bottombar.fragment.Fragment1;
import com.air.airspeed.bottombar.fragment.Fragment2;
import com.air.airspeed.bottombar.fragment.Fragment3;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private RecyclerView mRv;
    private List<ResolveInfo> mlsResolveInfo;
    private RvAdapter mAdapter;
    private TextView appscount;
    private int appnumber = 0;

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;


    @TargetApi(Build.VERSION_CODES.KITKAT)
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomBar bottomBar = (BottomBar) findViewById(R.id.bottom_bar);
        bottomBar.setContainer(R.id.f1_container);
        bottomBar.setTitleBeforeAndAfterColor("#999999", "#ff5d5e");
        bottomBar.setFirstChecked(0);
        bottomBar.setTitleSize(12);
        bottomBar.setTitleIconMargin(1);
        bottomBar.setIconHeight(40);
        bottomBar.setIconWidth(40);


        bottomBar.addItem(Fragment1.class, "应用", R.drawable.item1_before, R.drawable.item1_after);
        bottomBar.addItem(Fragment2.class, "加速", R.drawable.item2_before, R.drawable.item2_after);
        bottomBar.addItem(Fragment3.class, "我的", R.drawable.item3_before, R.drawable.item3_after);
        bottomBar.build();
        bottomBar.setOnTouchListener(new BottomTouchListener());

        appscount = (TextView) findViewById(R.id.appscount);
        mRv = (RecyclerView) findViewById(R.id.rv);
        mlsResolveInfo = new ArrayList<>();

        mRv.setLayoutManager(new GridLayoutManager(this, 4));

        mAdapter = new RvAdapter(MainActivity.this, mlsResolveInfo, new RvAdapter.OnItemClickListener() {
            @Override
            public void onClick(View view, int position) {
                ResolveInfo resolveInfo = mlsResolveInfo.get(position);
                //startAppByResloveInfo(resolveInfo);
                saveAppByResloveInfo(MainActivity.this, resolveInfo);
                //MainActivity.this.finish();
                appnumber += 1;
                callBack(view);
            }
        });

        mRv.setAdapter(mAdapter);

        //bottomBar.getIconPosition(1, endPoint);

        List<ResolveInfo> resolveInfos = queryMainActivitiesInfo();
        mlsResolveInfo.addAll(resolveInfos);
        mAdapter.notifyDataSetChanged();
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();


    }

    class BottomTouchListener implements View.OnTouchListener  {
        int target = -1;
        @Override
        public boolean onTouch(View view, MotionEvent event) {
            int viewWidth = view.getWidth()/3;
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    target = (int) event.getX()/viewWidth;
                    break;
                case MotionEvent.ACTION_UP:
                    if (event.getY() < 0) {
                        break;
                    }
                    if (target == ((int) event.getX()/viewWidth)) {
                        switch (target) {
                            case 1:
                                Intent intent = new Intent(MainActivity.this, MainSpeedActivity.class);
                                startActivity(intent);
                                break;
                            case 2:
                                Intent intent1 = new Intent(MainActivity.this, LoginSuccessActivity.class);
                                startActivity(intent1);
                                break;
                        }
                    }
                    break;
            }
            return false;
        }
    }

    private List<ResolveInfo> queryMainActivitiesInfo() {
        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> resolveInfos = getPackageManager().queryIntentActivities(mainIntent, 0);
        // remove slef app
        Iterator<ResolveInfo> iterator = resolveInfos.iterator();
        while (iterator.hasNext()) {
            ResolveInfo resolveInfo = iterator.next();
            String packageName = resolveInfo.activityInfo.packageName;

            if (packageName.equals(getApplication().getPackageName())) {
                iterator.remove();
                break;
            }

        }
        return resolveInfos;
    }

    private void startAppByResloveInfo(ResolveInfo resolveInfo) {
        String pkg = resolveInfo.activityInfo.packageName;   //app packagename
        String cls = resolveInfo.activityInfo.name;   //app name
        ComponentName component = new ComponentName(pkg, cls);

        //将包名、应用名存入App
        //NetApp.mInstance.setPackageName(pkg);
        //NetApp.mInstance.setAppName(cls);
        String[] strings;
        strings =  new String[2];
        strings[0] = pkg;
        strings[1] = cls;

        App app = new App(this);
        app.setOnApp(new App.OnAppListen() {
            @Override
            public void getApp(ArrayList<AppInfo> lsApp) {
                if (lsApp == null)
                {
                    Log.d("MainActivity", "lsApp is null");
                    return;
                }
                startService(new Intent(MainActivity.this,NetService.class));
            }
        });
        app.execute(strings);

        //打开选中加速应用的主activity，不能在这里打开，要到支付完成后打开,先手动打开测试
        //Intent intent = new Intent();
        //intent.setComponent(component);
        //startActivity(intent);

        //启动购买时间的activity
        Intent intent1 = new Intent(MainActivity.this, SelectBuyTime.class);
        Bundle bd1 = new Bundle();
        bd1.putString("SpeedAPPpkg", pkg);
        bd1.putString("SpeedAPPcls", cls);
        intent1.putExtras(bd1);
        startActivity(intent1);

        //启动服务，查询端口
       //Intent intent4 = new Intent(MainActivity.this,NetService.class);
       //intent4.putExtras(bd1);
       //startService(intent4);
    }

    private void saveAppByResloveInfo(Context context, ResolveInfo resolveInfo) {

        ArrayList<AppInfo> lsApp = NetApp.mInstance.getApp();
        AppInfo appInfo = new AppInfo();
        PackageManager pm = context.getApplicationContext().getPackageManager();
        PackageInfo info = null;

        try
        {
            info = pm.getPackageInfo(resolveInfo.activityInfo.packageName, 0);
            Log.d("MainActivity","info.uid = " + info.applicationInfo.uid);
        }catch (PackageManager.NameNotFoundException e)
        {
            e.printStackTrace();
        }

        appInfo.setUid(info.applicationInfo.uid);
        appInfo.setAppName(resolveInfo.activityInfo.name);
        appInfo.setPackName(resolveInfo.activityInfo.packageName);
        appInfo.setIcon(info.applicationInfo.loadIcon(pm));
        appInfo.setVersion(info.versionName);
        appInfo.setIsAccelerated(false);
        Log.d("MainActivity", "have set false " + appInfo.getAppName());

        if (lsApp == null)
        {
            ArrayList<AppInfo> lsTmpApp = new ArrayList<>();
            lsTmpApp.add(appInfo);
            lsApp = lsTmpApp;
            NetApp.mInstance.setApp(lsApp);
            Log.d("saveNet","now null NetApp is "+lsApp.size());
            savetosharedpreferences(appInfo.getPackName());

            return;
        }
        boolean found = false;
        for (AppInfo sTmp:lsApp)
        {
            if (appInfo.getPackName().equals(sTmp.getPackName()))
            {
                found = true;
                break;
            }
        }
        if (!found)
        {
            lsApp.add(appInfo);
            Log.d("saveNet","now NetApp is increased " +lsApp.size());
            savetosharedpreferences(appInfo.getPackName());
        }
    }

    private void savetosharedpreferences(String packageName)
    {
        SharedPreferences pref = getSharedPreferences("UserInfo", MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();

        String regular = "#";
        String newpackage = regular + packageName;
        String oldpackages = pref.getString("PackageNames", "");

        if (oldpackages.contains(newpackage)) {
            return;
        }
        newpackage = oldpackages + regular + packageName;

        editor.putString("PackageNames", newpackage);
        Log.d("saveShared", "packages are " + newpackage);
        if (!pref.getBoolean("isSelectApps", false))
        {
            editor.putBoolean("isSelectApps", true);
        }
        editor.commit();
    }

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    public Action getIndexApiAction() {
        Thing object = new Thing.Builder()
                .setName("Main Page") // TODO: Define a title for the content shown.
                // TODO: Make sure this auto-generated URL is correct.
                .setUrl(Uri.parse("http://[ENTER-YOUR-URL-HERE]"))
                .build();
        return new Action.Builder(Action.TYPE_VIEW)
                .setObject(object)
                .setActionStatus(Action.STATUS_TYPE_COMPLETED)
                .build();
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        AppIndex.AppIndexApi.start(client, getIndexApiAction());
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        AppIndex.AppIndexApi.end(client, getIndexApiAction());
        client.disconnect();
    }

    public void callBack(View view) {
        BallView ballView = new BallView(this,appnumber);
        int position[] = new int[2];
        view.getLocationInWindow(position);
        ballView.setStartPosition(new Point((position[0] + view.getRight())/2, position[1]));

        ViewGroup rootView = (ViewGroup) this.getWindow().getDecorView();
        rootView.addView(ballView);

        int endPositon[] = new  int[2];
        appscount.getLocationInWindow(endPositon);
        ballView.setEndPosition(new Point(endPositon[0]+30, endPositon[1]));
        ballView.startBeizerAnimation();
        //appscount.setTextColor(Color.RED);
        //appscount.setText(String.valueOf(appnumber));
    }
}
