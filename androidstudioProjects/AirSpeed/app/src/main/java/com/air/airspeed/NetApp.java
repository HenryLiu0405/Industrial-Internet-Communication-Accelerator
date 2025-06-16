package com.air.airspeed;

import android.app.Application;

import java.util.ArrayList;



public class NetApp extends Application
{
    public static NetApp mInstance;
    public boolean isFirstTime;
    private ArrayList<AppInfo> lsApp;
    public User user;
    private int appcount;

        //private String packageName;
    //private String appName;

    @Override
    public void onCreate()
    {
        super.onCreate();
        mInstance = this;
        isFirstTime = true;

    }

    //public static NetApp getmInstance() {
        //return mInstance;
    //}

    public ArrayList<AppInfo> getApp() {
        return lsApp;
    }

    public void setApp(ArrayList<AppInfo> app) {
        lsApp = app;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public User getUser() {
        return this.user;
    }

    public void setAppCount(int appcount) {
        this.appcount = appcount;
    }
    public int getAppCount() {
        return appcount;
    }

    //public void setPackageName(String pkg){
    //    packageName = pkg;
    //}

    //public String getPackageName() {
   //     return packageName;
   // }

    //public void setAppName(String cls) {
     //   appName = cls;
   // }

    //public String getAppName() {
     //   return appName;
   // }
}
