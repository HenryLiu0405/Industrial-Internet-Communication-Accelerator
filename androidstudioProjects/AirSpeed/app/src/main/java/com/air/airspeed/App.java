package com.air.airspeed;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.PackageInfo;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class App extends AsyncTask<String, Void, ArrayList<AppInfo>>
{
    private PackageManager pManager;
    private OnAppListen onApp;

    public App(Context context)
    {
        pManager = context.getApplicationContext().getPackageManager();
    }

    @Override
    protected void onPreExecute()
    {
        Log.d("App", "preexecute");
    }

    @Override
    protected void onPostExecute(ArrayList<AppInfo> lsInfo)
    {
        NetApp.mInstance.setApp(lsInfo);

        //Log.d("App","NetApp is " + NetApp.mInstance.getApp().getAppName() + NetApp.mInstance.getApp().getPackName());

        if (onApp != null)
        {
            onApp.getApp(lsInfo);
        }
    }

    @Override
    protected ArrayList<AppInfo> doInBackground(String... params)
    {
        String[] packageName;

        packageName = params;

        //Log.d("app", "apppackage = " + appName + packName );

        ArrayList<AppInfo> lsApp = new ArrayList<>();
        AppInfo appInfo;
        PackageInfo info = null;

        for (String packName:packageName)
        {
            try
            {
                Log.d("APP", "info packName is " + packName);
                info = pManager.getPackageInfo(packName, PackageManager.GET_PERMISSIONS);
                Log.d("App"," info.uid= " + info.applicationInfo.uid);
            }catch (PackageManager.NameNotFoundException e)
            {
                e.printStackTrace();
            }


            String[] permissions = info.requestedPermissions;
            if (permissions != null && permissions.length >0 )
            {
                Log.d("App","permission = " + permissions.length);
                for(String permission : permissions)
                {
                    if ("android.permission.INTERNET".equals(permission))
                    {
                        Log.d("App","internet = " + permission);
                        appInfo = new AppInfo();
                        appInfo.setUid(info.applicationInfo.uid);
                        //appInfo.setAppName(appName);
                        appInfo.setAppName(info.applicationInfo.loadLabel(pManager).toString());
                        appInfo.setPackName(packName);
                        //appInfo.setPackName(info.packageName);
                        appInfo.setIcon(info.applicationInfo.loadIcon(pManager));
                        appInfo.setVersion(info.versionName);
                        appInfo.setIsAccelerated(false);
                        Log.d("APP","have set false " + appInfo.getAppName());

                        lsApp.add(appInfo);
                        //Log.d("App","lsApp.uid = " + lsApp.getUid() + lsApp.getPackName() + lsApp.getAppName());

                        break;
                    }
                }
            }
        }
        return lsApp;
    }

    public void setOnApp(OnAppListen onApp)
    {
        this.onApp = onApp;
    }

    public interface OnAppListen
    {
        public void getApp(ArrayList<AppInfo> lsApp);
    }


}
