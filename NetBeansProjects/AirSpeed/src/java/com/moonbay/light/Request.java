/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.moonbay.light;

/**
 *
 * @author River
 */
public class Request {
    private int id;
    private int requesttype;
    private String correlationid;
    private String requesttime;
    private int operator;
    private int duration;
    private String destinationIP;
    private int destinationPort;
    private int mediatype;
    private int qospriority;
    private int direction;
    private int upmaxspeed;
    private int downmaxspeed;
    private int upminspeed;
    private int downminspeed;
    private String answertime;
    private int result = 1000;
    private String username;
    private String packagename;
    private String privateIP;
    private int privatePort;
    private String publicIP;
    private int publicPort;
    private String msisdn;
    private long datalength;
    
    public int getRequestType() {
        return requesttype;
    }
    public void setRequestType(int requesttype) {
        this.requesttype = requesttype;
    }
    public String getCorrelationID() {
        return correlationid;
    }
    public void setCorrelationID(String correlationid) {
        this.correlationid = correlationid;
    }
    public String getRequestTime() {
        return requesttime;
    }
    public void setRequestTime(String requesttime) {
        this.requesttime = requesttime;
    }
    public int getOperator() {
        return operator;
    }
    public void setOperator(int operator) {
        this.operator = operator;
    }
    public int getDuration() {
        return duration;
    }
    public void setDuration(int duration) {
        this.duration = duration;
    }
    public String getDestinationIP() {
        return destinationIP;
    }
    public void setDestinationIP(String destinationIP) {
        this.destinationIP = destinationIP;
    }
    public int getDestinationPort() {
        return destinationPort;
    }
    public void setDestinationPort(int destinationPort) {
        this.destinationPort = destinationPort;
    }
    public int getMediaType() {
        return mediatype;
    }
    public void setMediaType(int medidatype) {
        this.mediatype = mediatype;
    }
    public int getQoSPriority() {
        return qospriority;
    }
    public void setQoSPriority(int qospriority) {
        this.qospriority = qospriority;
    }
    public int getDirection() {
        return direction;
    }
    public void setDirection(int direction) {
        this.direction = direction;
    }
    public int getUpMaxSpeed() {
        return upmaxspeed;
    }
    public void setUpMaxSpeed(int upmaxspeed) {
        this.upmaxspeed = upmaxspeed;
    }
    public int getDownMaxSpeed() {
        return downmaxspeed;
    }
    public void setDownMaxSpeed(int downmaxspeed) {
        this.downmaxspeed = downmaxspeed;
    }
    public int getUpMinSpeed() {
        return upminspeed;
    }
    public void setUpMinSpeed(int upminspeed) {
        this.upminspeed = upminspeed;
    }
    public int getDownMinSpeed() {
        return downminspeed;
    }
    public void setDownMinSpeed(int downminspeed) {
        this.downminspeed = downminspeed;
    }
    public String getAnswerTime() {
        return answertime;
    }
    public void setAnswerTime(String answertime) {
        this.answertime = answertime;
    }
    public int getResult() {
        return result;
    }
    public void setResult(int result) {
        this.result = result;
    }
    public String getUserName() {
        return username;
    }
    public void setUserName(String username) {
        this.username = username;
    }
    public String getPackageName() {
        return packagename;
    }
    public void setPackageName(String packagename) {
        this.packagename = packagename;
    }
    public String getPrivateIP() {
        return privateIP;
    }
    public void setPrivateIP(String privateIP) {
        this.privateIP = privateIP;
    }
    public int getPrivatePort() {
        return privatePort;
    }
    public void setPrivatePort(int privatePort) {
        this.privatePort = privatePort;
    }
    public String getPublicIP() {
        return publicIP;
    }
    public void setPublicIP(String publicIP) {
        this.publicIP = publicIP;
    }
    public int getPublicPort() {
        return publicPort;
    }
    public void setPublicPort(int publicPort) {
        this.publicPort = publicPort;
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
