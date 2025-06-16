/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.moonbay.light;

import java.util.List;

/**
 *
 * @author River
 */
public class SpeedInfo {
    String username;
    int userstate;  //0:离线  1：在线
    String duedate;   
    String hearttime;  //last heart time
    List<NetInfo> netinfo;
    
    public String getusername() {
        return username;
    }
    public void setusername(String username) {
        this.username = username;
    }
    public int getuserstate() {
        return userstate;
    }
    public void setuserstate(int userstate) {
        this.userstate = userstate;
    }
    public String getduedate() {
        return duedate;
    }
    public void setduedate(String duedate) {
        this.duedate = duedate;
    }
    public String gethearttime() {
        return hearttime;
    }
    public void sethearttime(String hearttime) {
        this.hearttime = hearttime;
    }
    
    public List<NetInfo> getnetinfo(){
        return netinfo;
    }
    public void setnetinfo(List<NetInfo> netinfo) {
        this.netinfo = netinfo;
    }
}
