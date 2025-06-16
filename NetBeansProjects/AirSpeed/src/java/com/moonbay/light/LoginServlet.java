/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.moonbay.light;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.sf.json.JSONObject;
import org.apache.log4j.Logger;

/**
 *
 * @author 你好
 */
public class LoginServlet extends HttpServlet {

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        request.setCharacterEncoding("utf-8");
        response.setCharacterEncoding("utf-8");      
        
        
        try (PrintWriter out = response.getWriter()) {
                        
            /* 以下为正式程序代码*/
            String username = request.getParameter("UserName").trim();
            String password = request.getParameter("Password").trim();
            
            Map<String,String> params = new HashMap<>();
            JSONObject jsonObject = new JSONObject();            
            
            User user = UserDAO.queryUser(username);
            if(user != null) {
                Boolean verify = user.getPassword().equals(password);
                if (verify) {
                        params.put("UserName", user.getUserName());
                        params.put("Password", user.getPassword());
                        params.put("MemberDueDate",user.getDueDate());
                        params.put("Credits", String.valueOf(user.getCredits()));
                        params.put("Version", user.getVersion());
                        params.put("HeadPic", user.getHeadPic());
                    if(!isoutofdate(user)) {
                        params.put("Result", "success");                        
                        List<SpeedInfo> lsp = SpeedInfoManager.get();
                        boolean found = false;
                        if(lsp != null) {
                            for(SpeedInfo tmp:lsp) {
                                if(tmp.getusername().equals(user.getUserName())) {
                                    tmp.setduedate(user.getDueDate());
                                    tmp.setuserstate(1);
                                    java.util.Date systemtime = new java.util.Date();
                                    java.text.SimpleDateFormat s = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");
                                    tmp.sethearttime(s.format(systemtime));
                                    found = true;
                                    break;
                                }
                            }
                        }
                    
                        if(!found) {
                            SpeedInfo si = new SpeedInfo();
                            si.setusername(user.getUserName());
                            si.setduedate(user.getDueDate());
                            si.setuserstate(1);
                            lsp.add(si);
                        //SpeedInfoManager.get().add(si);
                        //List<SpeedInfo> ll = SpeedInfoManager.get();                        
                        }
                    }else {
                        params.put("Result", "outofdate");
                    }
                } else {
                    
                    params.put("Result", "passerror");
                }
            }else {
                params.put("Result", "notexist");
            }            
            jsonObject.put("params", params);
            out.write(jsonObject.toString());
            /*以上为正式程序代码*/
        }
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }
    
    private Boolean verifyLogin(String userName, String password) {
        User user = UserDAO.queryUser(userName);
        
        return null != user && password.equals(user.getPassword());
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>
    
    private boolean isoutofdate(User user)  {
        java.util.Date duetime = new java.util.Date();
        java.util.Date systemtime = new java.util.Date();
        java.text.SimpleDateFormat s = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try{
        duetime = s.parse(user.getDueDate());
        }catch(ParseException e){
            e.printStackTrace();
        }
        //long currenttime = System.currentTimeMillis();        
        if(systemtime.after(duetime)) {   //过期
            return true;
        }else {
            return false;
        }        
    }    

}
