/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.moonbay.light;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.sf.json.JSON;
import net.sf.json.JSONArray;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;
import org.apache.http.HttpResponse;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

/**
 *
 * @author 你好
 */
public class SpeedServlet extends HttpServlet {

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
        
        try(PrintWriter out = response.getWriter()) {
        
        Speed speed = new Speed();
        
        java.util.Date systemtime = new java.util.Date();
        java.text.SimpleDateFormat s = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");        
        speed.setDataTime(s.format(systemtime));
        
        speed.setUserName(request.getParameter("UserName"));
        speed.setAppName(request.getParameter("AppName"));
        speed.setPackageName(request.getParameter("PackageName"));
        //speed.setPublicIP(Long.parseLong(request.getParameter("PublicIP")));
        speed.setOperator(Integer.parseInt(request.getParameter("Operator")));
        speed.setSpeedType(Integer.parseInt(request.getParameter("SpeedType")));
        speed.setMSISDN(request.getParameter("MSISDN"));
        speed.setDataLength(Long.parseLong(request.getParameter("DataLength")));

        speed.setPublicIP(request.getParameter("PublicIP"));
        
            speed.setProtocol(request.getParameter("Protocol"));
            speed.setLocalAddress(request.getParameter("LocalAddress"));
            speed.setLocalPort(Integer.parseInt(request.getParameter("LocalPort")));
            speed.setRemoteAddress(request.getParameter("RemoteAddress"));
            speed.setRemotePort(Integer.parseInt(request.getParameter("RemotePort")));
            speed.setToken(request.getParameter("Token"));
            
            Map<String,String> params = new HashMap<>();
            JSONObject jsonobject = new JSONObject();
            
            params.put("Result", "success");
            params.put("AppName", speed.getAppName());
            params.put("PackageName", speed.getPackageName());
            params.put("Protocol", speed.getProtocol());
            params.put("LocalAddress",speed.getLocalAddress());
            params.put("LocalPort", request.getParameter("LocalPort"));
            params.put("RemoteAddress", speed.getRemoteAddress());
            params.put("RemotePort", request.getParameter("RemotePort"));
            jsonobject.put("params", params);
            out.write(jsonobject.toString());
            Logger.getLogger(SpeedServlet.class.getName()).info("response is "+ jsonobject.toString()+" "+String.valueOf(speed.getSpeedType()));
            
             SpeedDAO.insertSpeedItem(speed); 
            
        Logger.getLogger(SpeedServlet.class.getName()).info("received speed item "+speed.getRemoteAddress()+" "+speed.getLocalPort()
                +" "+String.valueOf(speed.getDataLength()));
        if(speed.getSpeedType() != 0){
            applyQos(speed); 
        }
        else {
            deleteQos(speed);
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
    
    private void applyQos(Speed speed)  {
        JSONObject apply = new JSONObject();
        JSONObject userinfo = new JSONObject();
        JSONObject resourceitem = new JSONObject();
        JSONObject flowitem = new JSONObject();
        JSONArray resource = new JSONArray();
        JSONArray flow = new JSONArray();
        
        userinfo.put("IP", speed.getLocalAddress());
        userinfo.put("PublicIP", speed.getPublicIP());        
                
        String packagename = speed.getPackageName();
        String type = getSpeedType(packagename);
        String priority = "15";
        
        flowitem.put("Direction", "2");
        flowitem.put("DestinationIpAddress", speed.getRemoteAddress());
        flowitem.put("DestinationPort", Integer.toString(speed.getRemotePort()));
        flowitem.put("Protocol", speed.getProtocol());
        switch (type) {
            case "0":
                flowitem.put("MaximumUpStreamSpeedRate",   "204800");
                flowitem.put("MaximumDownStreamSpeedRate", "204800");
                resourceitem.put("MinimumUpStreamSpeedRate","51200");
                resourceitem.put("MinimumDownStreamSpeedRate","51200");
                break;
            case "1":
                flowitem.put("MaximumUpStreamSpeedRate",   "512000");
                flowitem.put("MaximumDownStreamSpeedRate", "2048000");
                resourceitem.put("MinimumUpStreamSpeedRate","204800");
                resourceitem.put("MinimumDownStreamSpeedRate","1024000");
                break;
            case "2":
                flowitem.put("MaximumUpStreamSpeedRate",   "512000");
                flowitem.put("MaximumDownStreamSpeedRate", "512000");
                resourceitem.put("MinimumUpStreamSpeedRate","102400");
                resourceitem.put("MinimumDownStreamSpeedRate","102400");
                break;
            case "3":
                flowitem.put("MaximumUpStreamSpeedRate",   "512000");
                flowitem.put("MaximumDownStreamSpeedRate", "512000");
                resourceitem.put("MinimumUpStreamSpeedRate","307200");
                resourceitem.put("MinimumDownStreamSpeedRate","307200");
                break;
            default:
                flowitem.put("MaximumUpStreamSpeedRate",   "512000");
                flowitem.put("MaximumDownStreamSpeedRate", "512000");
                resourceitem.put("MinimumUpStreamSpeedRate","102400");
                resourceitem.put("MinimumDownStreamSpeedRate","102400");
                break;              
        }      
        
        flow.add(flowitem);        
                
        JSONObject reitem = new JSONObject();
        reitem.put("Type", type);
        reitem.put("Priority", priority);
        reitem.put("FlowProperties", flow);
        resource.add(reitem);
        resource.add(resourceitem);
        
        apply.put("Partner_ID", "moonbay");
        apply.put("security_token", speed.getToken());
        apply.put("UserIdentifier", userinfo);
        apply.put("ServiceId", "Games100K");  //Game-Tencent-DS
        apply.put("ResourceFeatureProperties", resource);
        apply.put("Duration", "300");     
        String params = apply.toString();    
        
        speed.setDuration(300);
            
        Request request = new Request();
        request.setRequestType(0);  //applytype
        //request.setCorrelationID(result.getString("CorrelationId"));
        request.setRequestTime(speed.getDataTime());
        request.setOperator(speed.getOperator());
        request.setDuration(Integer.parseInt(apply.getString("Duration")));
        request.setDestinationIP(speed.getRemoteAddress());
        request.setDestinationPort(speed.getRemotePort());
        request.setMediaType(Integer.parseInt(type));
        request.setQoSPriority(Integer.parseInt(priority));
        request.setDirection(Integer.parseInt(flowitem.getString("Direction")));
        request.setUpMaxSpeed(Integer.parseInt(flowitem.getString("MaximumUpStreamSpeedRate")));
        request.setDownMaxSpeed(Integer.parseInt(flowitem.getString("MaximumDownStreamSpeedRate")));
        request.setUpMinSpeed(Integer.parseInt(resourceitem.getString("MinimumUpStreamSpeedRate")));
        request.setDownMinSpeed(Integer.parseInt(resourceitem.getString("MinimumDownStreamSpeedRate")));
    
        request.setUserName(speed.getUserName());
        request.setPackageName(speed.getPackageName());
        request.setPrivateIP(speed.getLocalAddress());
        request.setPrivatePort(speed.getLocalPort());
        request.setPublicIP(speed.getPublicIP());
        request.setDataLength(speed.getDataLength());
        //写入数据库
        RequestDAO.insertRequestItem(request);
        Logger.getLogger(SpeedServlet.class.getName()).info("RequestDAO is "+request.getDestinationIP()+" "
                +request.getPrivatePort()+" "+String.valueOf(request.getDataLength()));
        
        //写入全局
        List<SpeedInfo> lsp = SpeedInfoManager.get();
        Logger logger = Logger.getLogger(SpeedServlet.class.getName());
        if(lsp == null) {            
            logger.info("SpeedInfoManager is null");
            return;
        }                
        boolean found = false;
        for(SpeedInfo tmp:lsp) {
        if(tmp.getusername().equals(speed.getUserName())
             && tmp.getuserstate() != 0) { 
            found = true; 
            tmp.sethearttime(speed.getDataTime());
            if(tmp.getnetinfo() == null) {
                ArrayList<NetInfo> nitmp = new ArrayList<NetInfo>();
                tmp.setnetinfo(nitmp);                
                NetInfo net = new NetInfo();
                net.setpackagename(speed.getPackageName());
                net.setpublicIP(speed.getPublicIP());
                net.setoperator(speed.getOperator());
                net.setmsisdn(speed.getMSISDN());
                net.setlocalIP(speed.getLocalAddress());
                net.setlocalport(speed.getLocalPort());
                net.setremoteIP(speed.getRemoteAddress());
                net.setremoteport(speed.getRemotePort());
                net.setprotocol(speed.getProtocol());
                //net.setcorrelationid(request.getCorrelationID());
                net.settoken(speed.getToken());
                net.setqospriority(request.getQoSPriority());
                //net.setspeedstate(1);
                nitmp.add(net);
                logger.info("netinfo has added " +net.getremoteIP()+" "+ String.valueOf(net.getlocalport())
                +" "+String.valueOf(speed.getDataLength()));
                break;
            }
            boolean netfound = false;
            List<NetInfo> ni = tmp.getnetinfo();
            for(NetInfo nt:ni) {
                if(nt.getpackagename().equals(speed.getPackageName())
                        && nt.getlocalIP().equals(speed.getLocalAddress())
                           && nt.getlocalport()==speed.getLocalPort()
                           && nt.getremoteIP().equals(speed.getRemoteAddress())
                           && nt.getremoteport()==speed.getRemotePort()
                           && nt.getprotocol().equals(speed.getProtocol())
                           && nt.gettoken().equals(speed.getToken())) {
                    netfound = true;
                    nt.setpublicIP(speed.getPublicIP());
                    nt.setoperator(speed.getOperator());
                    nt.setmsisdn(speed.getMSISDN());
                    nt.setlocalIP(speed.getLocalAddress());
                    nt.setlocalport(speed.getLocalPort());
                    nt.setremoteIP(speed.getRemoteAddress());
                    nt.setremoteport(speed.getRemotePort());
                    nt.setprotocol(speed.getProtocol());
                    //nt.setcorrelationid(request.getCorrelationID());
                    nt.settoken(speed.getToken());
                    nt.setqospriority(request.getQoSPriority());
                    logger.info("netinfo has modified "+nt.getremoteIP()+" "+ String.valueOf(nt.getlocalport())
                +" "+String.valueOf(speed.getDataLength()));
                    //nt.setspeedstate(1);                    
                    break;
                }
            }
            if(!netfound) {
                NetInfo net = new NetInfo();
                net.setpackagename(speed.getPackageName());
                net.setpublicIP(speed.getPublicIP());
                net.setoperator(speed.getOperator());
                net.setmsisdn(speed.getMSISDN());
                net.setlocalIP(speed.getLocalAddress());
                net.setlocalport(speed.getLocalPort());
                net.setremoteIP(speed.getRemoteAddress());
                net.setremoteport(speed.getRemotePort());
                net.setprotocol(speed.getProtocol());
                //net.setcorrelationid(request.getCorrelationID());
                net.settoken(speed.getToken());
                net.setqospriority(request.getQoSPriority());
                //net.setspeedstate(1);
                ni.add(net);
                logger.info("netinfo has added into the queue "+net.getremoteIP()+" "+ String.valueOf(net.getlocalport())
                +" "+String.valueOf(speed.getDataLength()));
            }
            break;
          }
        }
        if(!found) {
            logger.info("invalid speeditem");
            return;
        }
                            
        MyInt mycount = new MyInt(); 
        mycount.count = 3;             
        //for(int i=0;i<3;i++) {  
            final CountDownLatch latch = new CountDownLatch(1); 
           // http://p22n940119.iok.la/AirSpeed/TestServlet
            //http://4gqos.h2comm.com.cn:8090/ivsp/services/AACAPIV1/applyTecentGamesQoS
            HttpRequest.sendAsynPost("http://4gqos.h2comm.com.cn:8090/ivsp/services/AACAPIV1/applyTecentGamesQoS",params, new FutureCallback<HttpResponse>()
            {
                @Override
                public void completed(HttpResponse response) {
                    latch.countDown();
                    java.util.Date systemtime = new java.util.Date();
                    java.text.SimpleDateFormat s = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");
                    request.setAnswerTime(s.format(systemtime));
                    String recode = "";
                    JSONObject result = new JSONObject();
                    try {
                        recode = EntityUtils.toString(response.getEntity());
                    }catch(IOException e) {
                        e.printStackTrace();
                    }
                    logger.info("netinfo speed response"+ recode);
                    //测试先隐含
                    try{
                    result = JSONObject.fromObject(recode);  
                        try{
                        request.setResult(Integer.parseInt(result.getString("ResultCode")));
                        }catch(JSONException e){
                            //接收到不正确的json结果，测试自己制造结果 
                            result.put("ResultCode","0");
                            request.setResult(998);
                        }
                        try{
                            request.setCorrelationID(result.getString("CorrelationId"));
                        }catch(JSONException e){
                            //接收到不正确的json结果，测试自己制造结果 
                            request.setCorrelationID(recode);
                        }                    
                    }catch(JSONException e) {                                     
                        logger.info("netinfo speed response is not jsonobject");
                        //自己构建jsonobject
                        request.setResult(999);
                        request.setCorrelationID(recode);
                        result.put("ResultCode","0");
                    }
                   
                    
                    //更新数据库
                    RequestDAO.updateApplyInfo(request);
                    //更新全局
                    boolean refound = false;
                    for(SpeedInfo tmp:lsp) {
                    if(tmp.getusername().equals(speed.getUserName())
                        && tmp.getuserstate() != 0) { 
                        refound = true;        
                        if(tmp.getnetinfo() == null) {
                            break;
                        }
                    boolean renetfound = false;
                    List<NetInfo> reni = tmp.getnetinfo();
                    for(NetInfo nt:reni) {
                        if(nt.getpackagename().equals(speed.getPackageName())
                           && nt.getlocalIP().equals(speed.getLocalAddress())
                           && nt.getlocalport()==speed.getLocalPort()
                           && nt.getremoteIP().equals(speed.getRemoteAddress())
                           && nt.getremoteport()==speed.getRemotePort()
                           && nt.getprotocol().equals(speed.getProtocol())
                           ) {
                        renetfound = true;                    
                        nt.setcorrelationid(request.getCorrelationID());                    
                        nt.setspeedstate(1);                    
                        break;
                        }
                    }
                    if(!renetfound) {
                        Logger.getLogger(SpeedServlet.class.getName()).info("invalid netinfoitem");
                        break;
                    }
                    
                    }
                }
                if(!refound) {
                    Logger.getLogger(SpeedServlet.class.getName()).info("invalid speeditem delete");
                    return;
                }
                    
                    
                    if(result.getString("ResultCode").equals("0")) {    
                        mycount.count=0;
                                             
                    }else {         
                        //mycount.count--;                                   
                    }
                }

                @Override
                public void failed(Exception e) {
                    latch.countDown();
                    e.printStackTrace();
                }

                @Override
                public void cancelled() {  
                    latch.countDown();
                }        
            });  
            
            try {
                latch.await();
            }catch(InterruptedException e){
                e.printStackTrace();
            }            
            //if(mycount.count == 0){
              //  break;                
            //}
        //}  
    }
    
    private void deleteQos(Speed speed) {
        JSONObject delete = new JSONObject();
        List<SpeedInfo> lsp = SpeedInfoManager.get();
        if(lsp == null) {
            Logger.getLogger(SpeedServlet.class.getName()).info("SpeedInfoManager is null in delete");
            return;
        }
        boolean found = false;
        for(SpeedInfo tmp:lsp) {
           if(tmp.getusername().equals(speed.getUserName())
                   && tmp.getuserstate() != 0) {
               tmp.sethearttime(speed.getDataTime());
               List<NetInfo> ni = tmp.getnetinfo();
               if(ni == null) {
                   Logger.getLogger(SpeedServlet.class.getName()).info("netinfo is null in delete");
                   break;
               }
               for(NetInfo nt:ni) {
                   if(nt.getpackagename().equals(speed.getPackageName()) 
                           && nt.getlocalIP().equals(speed.getLocalAddress())
                           && nt.getlocalport()==speed.getLocalPort()
                           && nt.getremoteIP().equals(speed.getRemoteAddress())
                           && nt.getremoteport()==speed.getRemotePort()
                           && nt.getprotocol().equals(speed.getProtocol())
                           && nt.gettoken().equals(speed.getToken())
                          ) {
                       delete.put("correlationId",nt.getcorrelationid());
                       delete.put("publicIP", nt.getpublicIP());
                       //delete.put("PartnerID", "moonbay");
                       
                       Request request = new Request();
                       request.setRequestType(2);  //deletetype
                       request.setCorrelationID(nt.getcorrelationid());
                       request.setRequestTime(speed.getDataTime());                       
                       request.setOperator(speed.getOperator());  
                       request.setDestinationIP(nt.getremoteIP());
                       request.setDestinationPort(nt.getremoteport());
                       request.setUserName(speed.getUserName());
                       request.setPackageName(speed.getPackageName());
                       request.setPrivateIP(nt.getlocalIP());
                       request.setPrivatePort(nt.getlocalport());
                       request.setPublicIP(speed.getPublicIP());
                       request.setDataLength(speed.getDataLength());
                       
                       //写入数据库
                       RequestDAO.insertRequestItem(request);
                       Logger.getLogger(SpeedServlet.class.getName()).info("netinfo has found in delete "
                       +request.getDestinationIP()+" "+request.getPrivatePort()+" "+String.valueOf(request.getDataLength()));
                       
                       MyInt mydelcount = new MyInt(); 
                       mydelcount.count = 3;    
                        
                       //for(int i=0;i<3;i++) {
                           final CountDownLatch latch = new CountDownLatch(1); 
                            HttpRequest.sendAsynGet("http://4gqos.h2comm.com.cn:8090/ivsp/services/AACAPIV1/getRemoveTecentGamesQoS",delete, new FutureCallback<HttpResponse>()
                            {
                                @Override
                                 public void completed(HttpResponse response) {                                     
                                     latch.countDown();
                                     java.util.Date systemtime = new java.util.Date();
                                     java.text.SimpleDateFormat s = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");
                                     request.setAnswerTime(s.format(systemtime));
                                     String recode = "";
                                     JSONObject result = new JSONObject();
                                       try {
                                            recode = EntityUtils.toString(response.getEntity());
                                        }catch(IOException e) {                            
                                            e.printStackTrace();
                                        }
                                       
                                       Logger.getLogger(SpeedServlet.class.getName()).info("netinfo delete response" + recode);
                                       try {
                                       result = JSONObject.fromObject(recode); 
                                       try {
                                           request.setResult(Integer.parseInt(result.getString("ResultCode")));
                                       }catch(JSONException e){
                                           //接收到不正确的结果，测试自己制造结果 
                                           result.put("ResultCode", "0");
                                           request.setResult(998);
                                       }
                                       }catch(JSONException e){ 
                                            Logger.getLogger(SpeedServlet.class.getName()).info("netinfo delete response is not jsonobject");
                                            //自己构造jsonobject
                                            request.setResult(999);
                                            result.put("ResultCode", "0");
                                       }                                       
                                                          
                                       //更新数据库
                                       RequestDAO.updateDIInfo(request);
                                       //更新全局
                                       nt.setspeedstate(0);                                       
                                       
                                       if(result.getString("ResultCode").equals("0")) {
                                           mydelcount.count = 0;
                                           //ni.remove(nt);                                           
                                       }else {
                                           //mydelcount.count --;
                                       }                                    
                                 }
                                 
                                 @Override
                                 public void failed(Exception e) {
                                     latch.countDown();
                                     e.printStackTrace();
                                 }
                                 
                                @Override
                                public void cancelled() {
                                    latch.countDown();
                                }
                            });
                            
                            try {
                                latch.await();
                            }catch(InterruptedException e){
                                e.printStackTrace();
                            }            
                            //if(mydelcount.count == 0){
                             //break;                
                            //}
                       //}    
                   }
               }
           } 
        }
    }    
    
    class MyInt {
        public int count;  
    }
    
    private String getSpeedType(String packagename) {
        switch (packagename) {
            case "com.tencent.qqmusic":
            case "com.kugou.android":
            case "com.tencent.karaoke":
            case "cn.kuwo.player":
            case "fm.xiami.main":
            case "com.changba":
            case "com.netease.cloudmusic":
            case "com.ximalaya.ting.android":
                return "0";
            case "com.ss.android.ugc.aweme":
            case "com.smile.gifmaker":
            case "com.ss.android.ugc.live":
            case "com.yixia.videoeditor":
            case "com.youku.phone":
            case "com.qiyi.video":
            case "com.tencent.qqlive":
            case "air.tv.douyu.android":
            case "com.panda.videoliveplatform":
            case "com.taobao.taobao":
            case "com.jingdong.app.mall":
            case "com.xuemeng.pingduoduo":
            case "com.sankuai.meituan":
            case "com.suning.mobile.ebuy":
            case "com.taobao.idlefish":
            case "cn.missfresh.application":
            case "ctrip.android.view":
            case "com.Qunar":
            case "com.taobao.trip":
            case "com.dp.android.elong":
            case "com.tuniu.app.ui":  
            case "com.sankuai.meituan.takeoutnew":
            case "me.ele":
            case "com.dianping.v1":
                return "1";
            case "com.eg.android.AlipayGphone":
            case "com.baidu.wallet":
            case "com.wangyin.payment":
            case "com.tencent.tmgp.sgame":
            case "com.tencent.cldts":
            case "com.netease.zjz":
            case "com.netease.hyxd":
            case "com.tencent.tmgp.pubgmhd":
            case "com.tencent.tmgp.pubgm":    
                return "3";
            default:
                return "2";
        }
    }

}











