/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.moonbay.light;

import java.util.Timer;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
        

/**
 *
 * @author River
 */
public class TimerListener implements ServletContextListener {
    private Timer timer = null;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        timer = new Timer(true);
        sce.getServletContext().log("Timer is started");
        timer.scheduleAtFixedRate(new QosStateTask(), 0, 5*60*1000);
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        if(timer != null) {
            timer.cancel();
            sce.getServletContext().log("Timer is destoryed");
        }
    }
    
}
