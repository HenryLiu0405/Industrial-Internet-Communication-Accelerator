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
public class NetInfo {
        String packagename;
        String publicIP;
        int operator;
        String msisdn;
        String localIP;
        int localport;
        String remoteIP;
        int remoteport;
        String protocol;
        String correlationid;
        String token;
        int qospriority;
        int speedstate;    //0:停止加速  1：开始加速
        int opspdstate;    //0:服务存在  1：内部错误  201：服务不存在
        
        public String getpackagename() {
            return packagename;
        }
        public void setpackagename(String packagename) {
            this.packagename = packagename;
        }
        public String getlocalIP() {
            return localIP ;
        }
        public void setlocalIP(String localIP) {
            this.localIP = localIP;
        }
        public int getlocalport() {
            return localport;
        }
        public void setlocalport(int localport) {
            this.localport = localport;
        }
        public String getremoteIP() {
            return remoteIP;
        }
        public void setremoteIP(String remoteIP) {
            this.remoteIP = remoteIP;
        }
        public int getremoteport() {
            return remoteport;
        }
        public void setremoteport(int remoteport) {
            this.remoteport = remoteport;
        }
        public String getprotocol() {
            return protocol;
        }
        public void setprotocol(String protocol) {
            this.protocol = protocol;
        }
        public String getcorrelationid() {
            return correlationid;
        }
        public void setcorrelationid(String correlationid) {
            this.correlationid = correlationid;
        }
        public String gettoken() {
            return token;
        }
        public void settoken(String token) {
            this.token = token;
        }
        public int getqospriority() {
            return qospriority;
        }
        public void setqospriority(int qospriority) {
            this.qospriority = qospriority;
        }
        public int getspeedstate() {
            return speedstate;
        }
        public void setspeedstate(int speedstate) {
            this.speedstate = speedstate;
        }
        public int getopspdstate() {
            return opspdstate;
        }
        public void setopspdstate(int opspdstate) {
            this.opspdstate = opspdstate;
        }
        public String getpublicIP() {
        return publicIP;
    }
    public void setpublicIP(String publicIP) {
       this.publicIP = publicIP; 
    }
    public int getoperator(){
        return operator;
    }
    public void setoperator(int operator) {
        this.operator = operator;
    }
    public String getmsisdn(){
        return msisdn;
    }
    public void setmsisdn(String msisdn) {
        this.msisdn = msisdn;
    }
}
