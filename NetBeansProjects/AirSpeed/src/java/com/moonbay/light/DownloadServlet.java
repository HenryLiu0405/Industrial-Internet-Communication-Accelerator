/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.moonbay.light;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URLEncoder;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author River
 */
public class DownloadServlet extends HttpServlet {

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    
    private String contentType = "application/x-msdownload";
    private String enc = "utf-8";
    private String fileRoot = "";
    
    //初始化fileRoot enc contentType
    public void init(ServletConfig config) throws ServletException {
        String temp = config.getInitParameter("contentType");
        if(temp != null && !temp.equals("")) {
            contentType = temp;
        }
        temp = config.getInitParameter("enc");
        if(temp != null && !temp.equals("")) {
            enc = temp;
        }
        temp = config.getInitParameter("fileRoot");
        if(temp != null && !temp.equals("")) {
            fileRoot = temp;
        }
        
    }    
    
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        response.setCharacterEncoding(enc);
        request.setCharacterEncoding(enc);
        
        String filepath = request.getParameter("filepath");
        String fullFilePath = fileRoot + filepath;
        //读文件
        File file = new File(fullFilePath);
        if(file.exists()) {
            String filename = URLEncoder.encode(file.getName(),enc);
            response.reset();
            response.setContentType(contentType);
            response.addHeader("Content-Disposition", "attachment; filename=\"" + filename +"\" ");
            int fileLength = (int) file.length();
            response.setContentLength(fileLength);
            //如果长度大于0
            if(fileLength != 0) {
                //创建文件输入流，读取文件
                InputStream in = new FileInputStream(file);
                byte[] buf = new byte[4096];
                //创建输出流，文件下载
                ServletOutputStream out = response.getOutputStream();
                int readLength;
                while((readLength = in.read(buf)) != -1) {
                    out.write(buf,0,readLength);
                }
                in.close();
                out.flush();
                out.close();
            }
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
