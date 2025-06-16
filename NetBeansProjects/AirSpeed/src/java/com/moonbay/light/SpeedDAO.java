/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.moonbay.light;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author 你好
 */
public class SpeedDAO {
    
    public static void insertSpeedItem(Speed speed) {
        //获得数据库连接对象
        Connection connection = DBManager.getConnection();
        PreparedStatement preparedStatement = null;
        
        //生成SQL代码
        StringBuilder sqlStatement = new StringBuilder();
        sqlStatement.append("INSERT INTO speed(SpeedType,Time,UserName,AppName,PackageName,Protocol,"
                + "LocalAddress,LocalPort,RemoteAddress,RemotePort,Duration,Token,PublicIP,Operator,MSISDN,DataLength) "
                + "VALUE(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
        
        try{
            preparedStatement = connection.prepareStatement(sqlStatement.toString());
                        
            preparedStatement.setInt(1,speed.getSpeedType());
            preparedStatement.setString(2, speed.getDataTime());
            preparedStatement.setString(3, speed.getUserName());
            preparedStatement.setString(4, speed.getAppName());
            preparedStatement.setString(5,speed.getPackageName());
            preparedStatement.setString(6,speed.getProtocol());
            preparedStatement.setString(7, speed.getLocalAddress());
            preparedStatement.setInt(8, speed.getLocalPort());
            preparedStatement.setString(9, speed.getRemoteAddress());
            preparedStatement.setInt(10, speed.getRemotePort());
            preparedStatement.setInt(11,speed.getDuration());
            preparedStatement.setString(12,speed.getToken());
            preparedStatement.setString(13,speed.getPublicIP());
            preparedStatement.setInt(14,speed.getOperator());
            preparedStatement.setString(15, speed.getMSISDN());
            preparedStatement.setLong(16, speed.getDataLength());
            
            preparedStatement.executeUpdate();
            preparedStatement.close();
            connection.close();
        } catch(SQLException ex) {
            Logger.getLogger(SpeedDAO.class.getName()).log(Level.SEVERE,null,ex);
        }        
    }
    
    
}
