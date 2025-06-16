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
public class User {
   private int id; 
   private String userName;
   private String password;
   private String dueDate;
   private int credits;
   private String version;
   private String headpic;
   
   public int getId() {
       return id;
   }
   
   public void setId(int id)  {
       this.id = id;
   }
   public String getUserName(){
       return userName;
   }
   
   public void setUserName(String userName){
       this.userName = userName;
   }
   
   public String getPassword(){
       return password;
   }
   
   public void setPassword(String password){
       this.password = password;
   }
   
   public String getDueDate() {
       return dueDate;
   }
   
   public void setDueDate(String dueDate) {
       this.dueDate = dueDate;
   }
   
   public int getCredits() {
       return credits;
   }
   
   public void setCredits(int credits) {
       this.credits = credits;
   }
   
   public String getVersion() {
       return version;
   }
   
   public void setVersion(String version) {
       this.version = version;
   }
   public String getHeadPic() {
       return headpic;
   }
   
   public void setHeadPic(String headpic) {
       this.headpic = headpic;
   }
}
