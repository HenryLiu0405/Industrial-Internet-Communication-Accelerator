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
public class OpRecordDAO {
    
    public static int insertOpRecord(OpRecord oprecord) {
        //获得数据库连接对象
        Connection connection = DBManager.getConnection();
        PreparedStatement preparedStatement = null;
        int result = 0;
        
        //生成SQL代码
        StringBuilder sqlStatement = new StringBuilder();
        sqlStatement.append("INSERT INTO oprecord(UserName,ModTime,Version,"
                + "TableName,TableKeyId,FieldName,OldValue,NewValue) "
                + "VALUE(?,?,?,?,?,?,?,?)");
        
        try{
            preparedStatement = connection.prepareStatement(sqlStatement.toString());
            
            preparedStatement.setString(1, oprecord.getUserName());
            preparedStatement.setString(2, oprecord.getModTime());
            preparedStatement.setString(3, oprecord.getVersion());
            preparedStatement.setString(4, oprecord.getTableName());
            preparedStatement.setInt(5, oprecord.getTableKyeId());
            preparedStatement.setString(6, oprecord.getFieldName());
            preparedStatement.setString(7, oprecord.getOldValue());
            preparedStatement.setString(8, oprecord.getNewValue());
            
            result = preparedStatement.executeUpdate();
            preparedStatement.close();
            connection.close();
        } catch(SQLException ex){
            Logger.getLogger(OpRecordDAO.class.getName()).log(Level.SEVERE,null,ex);
        }
        return result;
    }
    
}
