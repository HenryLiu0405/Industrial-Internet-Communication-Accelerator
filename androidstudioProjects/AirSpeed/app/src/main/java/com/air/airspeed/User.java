package com.air.airspeed;

import java.text.ParseException;

public class User {
    private String userName;
    private String passWord;
    private String dueDate;
    private int credits;
    private String version;
    private String headpic;
    private String netversion;

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserName() {
        return this.userName;
    }

    public void setPassWord(String passWord) {
        this.passWord = passWord;
    }

    public String getPassWord() {
        return this.passWord;
    }

    public void setDueDate(String dueDate) {
        this.dueDate = dueDate;
    }

    public String getDueDate() {
        return dueDate;
    }

    public void setCredits(int credits) {
        this.credits = credits;
    }

    public int getCredits() {
        return credits;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getVersion() {
        return version;
    }

    public void setHeadPic(String headpic) {
        this.headpic = headpic;
    }

    public String getHeadPic() {
        return headpic;
    }

    public void setNetVersion(String netversion) {
        this.netversion = netversion;
    }

    public String getNetVerion() {
        return netversion;
    }

    public boolean isoutofdate( ) {
        java.util.Date duetime = new java.util.Date();
        java.util.Date systemtime = new java.util.Date();
        java.text.SimpleDateFormat s = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try{
            duetime = s.parse(dueDate);
        }catch (ParseException e){
            e.printStackTrace();
        }
        if (systemtime.after(duetime)){
            return true;
        }else {
            return false;
        }
    }
}
