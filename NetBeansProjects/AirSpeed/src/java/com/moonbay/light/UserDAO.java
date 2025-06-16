/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.moonbay.light;

/**
 *
 * @author 你好
 */
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
        
public class UserDAO {
    
    public static User queryUser(String userName) {
        Connection connection = DBManager.getConnection();
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        
        StringBuilder sqlStatement = new StringBuilder();
        sqlStatement.append("SELECT * FROM user WHERE UserName=?");
        
        try{
            preparedStatement = connection.prepareStatement(sqlStatement.toString());
            preparedStatement.setString(1, userName);
            resultSet = preparedStatement.executeQuery();
            User user = new User();
            if(resultSet.next()) {
                user.setUserName(resultSet.getString("UserName"));
                user.setPassword(resultSet.getString("Password"));
                user.setId(resultSet.getInt("Id"));
                user.setDueDate(resultSet.getString("MemberDueDate"));
                user.setCredits(resultSet.getInt("Credits"));
                user.setVersion(resultSet.getString("Version"));
                user.setHeadPic(resultSet.getString("HeadPic"));
                return user;
            }else{
                return null;
            }            
        }catch(SQLException ex) {
            Logger.getLogger(UserDAO.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        } finally {
            DBManager.closeAll(connection, preparedStatement, resultSet);
        }        
    }

    public static int updateUserPass(User user) {
        //获得数据库连接对象
        Connection connection = DBManager.getConnection();
        PreparedStatement preparedStatement = null;
        int result = 0;
        
        //生成SQL代码
        StringBuilder sqlStatement = new StringBuilder();
        sqlStatement.append("UPDATE user SET Password='" + user.getPassword() + "' WHERE UserName='" + user.getUserName() + "'");
        
        try{
            preparedStatement = connection.prepareStatement(sqlStatement.toString());   
            result = preparedStatement.executeUpdate();
            preparedStatement.close();
            connection.close();            
        } catch(SQLException ex) {
            Logger.getLogger(UserDAO.class.getName()).log(Level.SEVERE,null,ex);
        }
        return result;
    }
    
    public static int updateUserHead(User user) {
        //获得数据库连接对象
        Connection connection = DBManager.getConnection();
        PreparedStatement preparedStatement = null;
        int result = 0;
        
        //生成SQL代码
        StringBuilder sqlStatement = new StringBuilder();
        sqlStatement.append("UPDATE user SET HeadPic='" + user.getHeadPic() + "' WHERE UserName='" + user.getUserName() + "'");
        
        try{
            preparedStatement = connection.prepareStatement(sqlStatement.toString());   
            result = preparedStatement.executeUpdate();
            preparedStatement.close();
            connection.close();            
        } catch(SQLException ex) {
            Logger.getLogger(UserDAO.class.getName()).log(Level.SEVERE,null,ex);
        }
        return result;
    }
    
    public static int updateUserName(User user) {
        Connection connection = DBManager.getConnection();
        PreparedStatement preparedStatement = null;
        int result = 0;
        
        StringBuilder sqlStatement = new StringBuilder();
        sqlStatement.append("UPDATE user SET UserName='" + user.getUserName() +"' WHERE Id='" + user.getId() +"'");
        try{
            preparedStatement = connection.prepareStatement(sqlStatement.toString());
            result = preparedStatement.executeUpdate();
            preparedStatement.close();
            connection.close();
        }catch(SQLException ex) {
            Logger.getLogger(UserDAO.class.getName()).log(Level.SEVERE,null,ex);
        }
        return result;
    }

    public static int insertUser(User user) {
        //获得数据库连接对象
        Connection connection = DBManager.getConnection();
        PreparedStatement preparedStatement = null;
        int result = 0;
        
        //生成SQL代码
        StringBuilder sqlStatement = new StringBuilder();
        sqlStatement.append("INSERT INTO user(UserName,Password,MemberDueDate,Credits) VALUE(?,?,?,?)");
        
        try{
            preparedStatement = connection.prepareStatement(sqlStatement.toString());
            
            preparedStatement.setString(1, user.getUserName());
            preparedStatement.setString(2, user.getPassword());
            preparedStatement.setString(3, user.getDueDate());
            preparedStatement.setInt(4, user.getCredits());
            
            result = preparedStatement.executeUpdate();
            preparedStatement.close();
            connection.close();
        }catch(SQLException ex) {
            Logger.getLogger(UserDAO.class.getName()).log(Level.SEVERE,null,ex);
        }   
        return result;
    }
    
    public static void deleteUser(User user) {
        
    }
}
