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
public class OpRecord {
    private String username;
    private String modtime;
    private String version;
    private String tablename;
    private int tablekeyid;
    private String fieldname;
    private String oldvalue;
    private String newvalue;
    
    public void setUserName(String username) {
        this.username = username;
    }
    
    public String getUserName() {
        return username;
    }
    
    public void setModTime(String modtime) {
        this.modtime = modtime;
    }
    
    public String getModTime() {
        return modtime;
    }
    
    public void setVersion(String version) {
        this.version = version;
    }
    
    public String getVersion() {
        return version;
    }
    
    public void setTableName(String tablename) {
        this.tablename = tablename;
    }
    
    public String getTableName() {
        return tablename;
    }
    
    public void setTableKeyId(int tablekeyid) {
        this.tablekeyid = tablekeyid;
    }
    
    public int getTableKyeId() {
        return tablekeyid;
    }
    
    public void setFieldName(String fieldname) {
        this.fieldname = fieldname;
    }
    
    public String getFieldName() {
        return fieldname;
    }
    
    public void setOldValue(String oldvalue) {
        this.oldvalue = oldvalue;
    }
    
    public String getOldValue() {
        return oldvalue;
    }
    
    public void setNewValue(String newvalue) {
        this.newvalue = newvalue;
    }
    
    public String getNewValue() {
        return newvalue;
    }
    
}
