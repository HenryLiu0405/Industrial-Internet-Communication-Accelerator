/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.moonbay.light;

import java.io.IOException;
import java.io.PrintWriter;
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
public class ModifyPhoneServlet extends HttpServlet {

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    
    private User newuser = new User();
    private User olduser = new User();
    private OpRecord oprecord = new OpRecord();
    
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        response.setCharacterEncoding("utf-8");
        request.setCharacterEncoding("utf-8");
        int modresult = 0;
        int recordresult = 0;
        
        try(PrintWriter out = response.getWriter()) {
            Map<String,String> params = new HashMap<>();
            JSONObject jsonObject = new JSONObject();
            
            olduser = UserDAO.queryUser(request.getParameter("OldUserName"));
            if(olduser != null) {
                if(!request.getParameter("NewCode").equals("1234"))
                {
                    params.put("Result","errorcode");
                    jsonObject.put("params",params);
                    out.write(jsonObject.toString());
                    return;
                }
                Boolean verifyPass = olduser.getPassword().equals(request.getParameter("OldPassword"));
                if(verifyPass) {        
                    newuser.setId(olduser.getId());
                    newuser.setUserName(request.getParameter("NewUserName"));
                    newuser.setPassword(olduser.getPassword());
                    newuser.setDueDate(olduser.getDueDate());
                    newuser.setCredits(olduser.getCredits());
                    newuser.setVersion(olduser.getVersion());
        
                    modresult = UserDAO.updateUserName(newuser);
            
                    if(modresult > 0 ) {
                        oprecord.setUserName(olduser.getUserName());
        
                        java.util.Date systemtime = new java.util.Date();
                        java.text.SimpleDateFormat s = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");        
                        oprecord.setModTime(s.format(systemtime));
        
                        oprecord.setVersion(olduser.getVersion());
                        oprecord.setTableName("user");
                        oprecord.setTableKeyId(olduser.getId());
                        oprecord.setFieldName("UserName");
                        oprecord.setOldValue(olduser.getUserName());
                        oprecord.setNewValue(newuser.getUserName());
                         
                        recordresult = OpRecordDAO.insertOpRecord(oprecord);
                
                        if(recordresult > 0) {
                            params.put("Result","success");
                            params.put("UserName", newuser.getUserName());
                            params.put("Password",newuser.getPassword());
                            params.put("MemberDueDate",newuser.getDueDate());
                            params.put("Credits", String.valueOf(newuser.getCredits()));
                            params.put("Version", newuser.getVersion());
                            params.put("HeadPic", olduser.getHeadPic());
                        }else {
                            params.put("Result", "recordfailed");
                        }              
                    }else {
                        params.put("Result", "modfailed");
                    }       
                } else {
                    params.put("Result", "erroepass");
                }
            }else {
                params.put("Result", "notexist");
            }
            jsonObject.put("params",params);
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
