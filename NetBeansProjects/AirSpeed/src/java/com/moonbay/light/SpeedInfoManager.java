/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.moonbay.light;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author River
 */
public class SpeedInfoManager {
    private static List<SpeedInfo> lsSpeedInfo = new ArrayList<SpeedInfo>();
    
    public static List<SpeedInfo> get() {
        return lsSpeedInfo;
    }
   
    
    
}
