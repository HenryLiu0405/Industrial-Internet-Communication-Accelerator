

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.moonbay.light;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author River
 */
public class RequestDAO {
    public static void insertRequestItem(Request request) {
        //获得数据库连接对象
        Connection connection = DBManager.getConnection();
        PreparedStatement preparedStatement = null;
        
        //生成SQL代码
        StringBuilder sqlStatement = new StringBuilder();
        sqlStatement.append("INSERT INTO request(RequestType,CorrelationID,RequestTime,Operator,Duration,"
                + "DestinationIP,DestinationPort,MediaType,QoSPriority,Direction,UpMaxSpeed,DownMaxSpeed,UpMinSpeed,DownMinSpeed,"
                + "AnswerTime,Result,UserName,PackageName,PrivateIP,PrivatePort,PublicIP,PublicPort,MSISDN,DataLength) "
                + "VALUE(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
        
        try{
            preparedStatement = connection.prepareStatement(sqlStatement.toString());
                        
            preparedStatement.setInt(1, request.getRequestType());
            preparedStatement.setString(2, request.getCorrelationID());
            preparedStatement.setString(3, request.getRequestTime());
            preparedStatement.setInt(4,request.getOperator());
            preparedStatement.setInt(5,request.getDuration());
            preparedStatement.setString(6, request.getDestinationIP());
            preparedStatement.setInt(7, request.getDestinationPort());
            preparedStatement.setInt(8, request.getMediaType());
            preparedStatement.setInt(9, request.getQoSPriority());
            preparedStatement.setInt(10,request.getDirection());
            preparedStatement.setInt(11,request.getUpMaxSpeed());
            preparedStatement.setInt(12,request.getDownMaxSpeed());
            preparedStatement.setInt(13, request.getUpMinSpeed());
            preparedStatement.setInt(14, request.getDownMinSpeed());
            preparedStatement.setString(15, request.getAnswerTime());
            preparedStatement.setInt(16, request.getResult());
            preparedStatement.setString(17, request.getUserName());
            preparedStatement.setString(18, request.getPackageName());
            preparedStatement.setString(19, request.getPrivateIP());
            preparedStatement.setInt(20, request.getPrivatePort());
            preparedStatement.setString(21, request.getPublicIP());
            preparedStatement.setInt(22, request.getPublicPort());
            preparedStatement.setString(23,request.getMSISDN());
            preparedStatement.setLong(24,request.getDataLength());
            
            preparedStatement.executeUpdate();
            preparedStatement.close();
            connection.close();
        } catch(SQLException ex) {
            Logger.getLogger(Request.class.getName()).log(Level.SEVERE,null,ex);
        }        
    }
    
    public static int updateDIInfo(Request request) {
        int result = 0;
        //获得数据库连接
        Connection conn = DBManager.getConnection();
        PreparedStatement ps = null;
        //生产SQL代码
        StringBuilder sb = new StringBuilder();
        sb.append("UPDATE request SET AnswerTime='"+request.getAnswerTime()+"', Result='"+request.getResult()+"' WHERE CorrelationID='"+request.getCorrelationID()+"' AND RequestType='"+request.getRequestType()+"'");
        try {
            ps = conn.prepareStatement(sb.toString());
            result = ps.executeUpdate();
            ps.close();
            conn.close();
        }catch(SQLException ex) {
            Logger.getLogger(RequestDAO.class.getName()).log(Level.SEVERE,null,ex);
        }        
        return result;
    }
        
    
    public static int updateApplyInfo(Request request) {
        int result = 0;
        Connection conn = DBManager.getConnection();
        PreparedStatement ps = null;
        StringBuilder sb = new StringBuilder();
        sb.append("UPDATE request SET AnswerTime='"+request.getAnswerTime()+"', Result='"+request.getResult()+"', CorrelationID='"+request.getCorrelationID()+"' WHERE DestinationIP='"+request.getDestinationIP()+"' AND DestinationPort='"+request.getDestinationPort()+"' AND RequestType='"+request.getRequestType()+"'");
        try {
            ps = conn.prepareStatement(sb.toString());
            result = ps.executeUpdate();
            ps.close();
            conn.close();
        }catch(SQLException ex) {
            Logger.getLogger(RequestDAO.class.getName()).log(Level.SEVERE,null,ex);            
        }
        return result;
    } 
    

    
}
