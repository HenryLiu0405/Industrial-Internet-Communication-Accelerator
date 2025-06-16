/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.moonbay.light;

import java.text.SimpleDateFormat;

/**
 *
 * @author 你好
 */
public class Speed {
    private int id;
    private int speedtype;  //0:停止加速   1:1级加速  2:2级加速
    private String dateTime;
    private String userName;
    private String appName;
    private String packageName;
    private String protocol;
    private String localAddress;
    private int localPort;
    private String remoteAddress;
    private int remotePort;
    private int duration;
    private String token;
    private String publicIP;
    private int operator;
    private String msisdn;
    private long datalength;
    
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public int getSpeedType() {
        return speedtype;
    }
    public void setSpeedType(int speedtype) {
        this.speedtype = speedtype;
    }
    
    public String getDataTime() {
        return dateTime;
    }
    
    public void setDataTime(String dataTime) {
        this.dateTime = dataTime;
    }
    
    public String getUserName() {
        return userName;
    }
    
    public void setUserName(String userName) {
        this.userName = userName;
    }
    
    public String getAppName(){
         return appName;
    }

    public void setAppName(String appName){
        this.appName = appName;
    }   
    
    public String getPackageName(){
        return packageName;
    }
    public void setPackageName(String packageName){
        this.packageName = packageName;
    }
    
    public String getProtocol(){
        return protocol;
    }
    public void setProtocol(String protocol){
        this.protocol = protocol;
    }
    
    public String getLocalAddress() {
        return localAddress;
    }
    public void setLocalAddress(String localAddress){
        this.localAddress = localAddress;
    }
    
    public int getLocalPort() {
        return localPort;
    }
    public void setLocalPort(int localPort){
        this.localPort = localPort;
    }
    
    public String getRemoteAddress(){
        return remoteAddress;
    }
    public void setRemoteAddress(String remoteAddress) {
        this.remoteAddress = remoteAddress;
    } 
    
    public int getRemotePort() {
        return remotePort;
    }
    public void setRemotePort(int remotePort) {
        this.remotePort = remotePort;
    }
    public int getDuration() {
        return duration;
    }
    public void setDuration(int duration) {
        this.duration = duration;
    }
    public String getToken() {
        return token;
    }
    public void setToken(String token) {
        this.token = token;
    }
    public String getPublicIP() {
        return publicIP;
    }
    public void setPublicIP(String publicIP) {
        this.publicIP = publicIP;
    }
    public int getOperator() {
        return operator;
    }
    public void setOperator(int operator) {
        this.operator = operator;
    }
    public String getMSISDN(){
        return msisdn;        
    }
    public void setMSISDN(String msisdn) {
        this.msisdn = msisdn;
    }
    public long getDataLength(){
        return datalength;
    }
    public void setDataLength(long datalength) {
        this.datalength = datalength;
    }
}
