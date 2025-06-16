package com.air.airspeed;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateTime {
    private Date currentTime;
    private SimpleDateFormat formatter;
    private String curTimebyformat;

    public String getCurrentTimebyFormat(String formatter){
        this.formatter = new SimpleDateFormat(formatter);
        this.currentTime = new Date(System.currentTimeMillis());
        curTimebyformat = formatter.format(String.valueOf(currentTime));
        return this.curTimebyformat;
    }
}
