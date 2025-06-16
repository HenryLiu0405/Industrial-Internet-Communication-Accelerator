/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.moonbay.light;

import java.io.IOException;
import java.io.PrintWriter;
import static java.lang.String.format;
import static java.lang.String.format;
import static java.lang.String.format;
import static java.lang.System.out;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.sf.json.JSONObject;

/**
 *
 * @author River
 */
public class RegisterServlet extends HttpServlet {

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    public final static int NEW_REGISTER_CREDITS = 10;
    public final static int NEW_REGISTER_DAYS = 14;
    
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        response.setCharacterEncoding("utf-8");
        request.setCharacterEncoding("utf-8");
        User user = new User();
        int insertResult;
        
        try(PrintWriter out = response.getWriter())  {
            String username = request.getParameter("UserName").trim();
            String password = request.getParameter("Password").trim();
            String version = request.getParameter("Version").trim();
     
            user = UserDAO.queryUser(username);
            
            Map<String,String> params = new HashMap<>();
            JSONObject jsonObject = new JSONObject();
                
            if(user == null) {
                User newuser = new User();
                newuser.setUserName(username);
                newuser.setPassword(password);
                newuser.setVersion(version);
                
                Date systemTime = new Date();
                SimpleDateFormat s = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Calendar ca = Calendar.getInstance();
                ca.add(Calendar.DATE, NEW_REGISTER_DAYS);
                systemTime = ca.getTime();
                String dueDate = s.format(systemTime);
                newuser.setDueDate(dueDate);                
                
                newuser.setCredits(NEW_REGISTER_CREDITS);
                insertResult = UserDAO.insertUser(newuser);              
                
                if(insertResult > 0) {
                    params.put("Result","success");
                    params.put("UserName",username);
                    params.put("Password",password);
                    params.put("MemberDueDate",user.getDueDate());
                    params.put("Credits", String.valueOf(user.getCredits()));
                    params.put("Version", user.getVersion());
                    params.put("HeadPic", user.getHeadPic());
                }else {
                    params.put("Result","failed");
                }
            }else {
                params.put("Result", "existed");
            }
            
            jsonObject.put("params", params);
            out.write(jsonObject.toString());
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
    
    private Boolean verifyRegister(String userName, String password) {
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

}
