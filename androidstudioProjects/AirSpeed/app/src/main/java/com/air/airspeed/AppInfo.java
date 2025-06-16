
package com.air.airspeed;

import android.graphics.drawable.Drawable;

import java.util.ArrayList;
import java.util.List;

public class AppInfo {
    private int uid;
    private String appName;
    private String packName;
    private Drawable icon;
    private String version;
    private Boolean isAccelerated;
    private boolean isBackground = false;  //加速应用是否在后台  true:后台  false:前台
    private int state=0;  //用户状态，0:停止加速中  1：加速中
    public ArrayList<NetInfo> lsNet;


    public String getAppName()
    {
        return appName;
    }

    public void setAppName(String appName)
    {
        this.appName = appName;
    }

    public String getPackName()
    {
        return packName;
    }

    public void setPackName(String packName)
    {
        this.packName = packName;
    }

    public Drawable getIcon()
    {
        return icon;
    }

    public void setIcon(Drawable icon)
    {
        this.icon = icon;
    }

    public String getVersion()
    {
        return  version;
    }

    public void setVersion(String version)
    {
        this.version = version;
    }

    public int getUid()
    {
        return uid;
    }

    public void setUid(int uid )
    {
        this.uid = uid;
    }

    public Boolean getIsAccelerated() {
        return isAccelerated;
    }

    public void setIsAccelerated(Boolean isAccelerated) {
        this.isAccelerated = isAccelerated;
    }
    public  boolean getIsBackground() {
        return isBackground;
    }
    public void setIsBackground(boolean isBackground) {
        this.isBackground = isBackground;
    }
    public  int getState() {
        return this.state;
    }
    public void setState(int state) {
        this.state = state;
    }

    //一下匹配新增
    public static AppInfo createFromUid(int uid){
        List<AppInfo> lsApp = NetApp.mInstance.getApp();
        AppInfo appInfo = new AppInfo();
        for (AppInfo sApp: lsApp){
            if (sApp.getUid() == uid){
                appInfo = sApp;
            }
        }
        return appInfo;
    }
}
