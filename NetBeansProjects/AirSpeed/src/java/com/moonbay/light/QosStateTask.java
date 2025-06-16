/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.moonbay.light;

import java.io.IOException;
import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;
import static jdk.nashorn.internal.objects.NativeError.printStackTrace;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

/**
 *
 * @author River
 */
public class QosStateTask extends TimerTask{
    protected final Log log = LogFactory.getLog(getClass());
    
    private static boolean isRunning = false;
    
    @Override
    public void run() {
        if(!isRunning) {
            isRunning = true;
            log.info("Task started");
            
            //执行查询QOS操作
             //testapply();
             //inquireQos();
            //判断用户是否到期、离线，若是则删除该用户所有加速
             try {
                 checkusers();
             }catch(ParseException e) {
                 printStackTrace(e);
             }
            
            isRunning = false;
            log.info("Task closed");
        }else {
            log.info("Task is not excuted");
        }
    }
    
    private void inquireQos() {
        
        List<SpeedInfo> lsp = SpeedInfoManager.get();
        if(lsp.size() == 0) {
            return;
        }        

        for(SpeedInfo tmp:lsp) {
            if(tmp.getusername() == null || tmp.getusername() == ""){
                break;
            }
            JSONObject inquire = new JSONObject();            
            List<NetInfo> lsnet = tmp.getnetinfo();
            if(lsnet == null) {
                return;
            }
            for(NetInfo net:lsnet) {
                if(net.getcorrelationid() == null || net.getcorrelationid() == "" || net.getspeedstate() == 0) {
                    break;
                }
                inquire.put("correlationId", net.getcorrelationid());
                inquire.put("publicIP", net.getpublicIP());
                final CountDownLatch latch = new CountDownLatch(1); 
                Request request = new Request();
                request.setRequestType(3);  //inquire
                java.util.Date systemtime = new java.util.Date();
                java.text.SimpleDateFormat s = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");
                request.setRequestTime(s.format(systemtime));
                request.setOperator(net.getoperator());
                request.setCorrelationID(net.getcorrelationid());
                request.setUserName(tmp.getusername());
                request.setPackageName(net.getpackagename());
                request.setPublicIP(net.getpublicIP());
                //写入数据库
                RequestDAO.insertRequestItem(request);
                                
                HttpRequest.sendAsynGet("http://4gqos.h2comm.com.cn:8090/ivsp/services/AACAPIV1/getQosState", inquire, new FutureCallback<HttpResponse>()
                {
                    @Override
                    public void completed(HttpResponse response) {
                        latch.countDown();
                        java.util.Date systemtime = new java.util.Date();
                        java.text.SimpleDateFormat s= new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");
                        request.setAnswerTime(s.format(systemtime));
                        String recode = "";
                        try {
                            recode = EntityUtils.toString(response.getEntity());
                        }catch(IOException e) {
                            e.printStackTrace();
                        }
                        JSONObject result = JSONObject.fromObject(recode);
                        request.setResult(Integer.parseInt(result.getString("ResultCode")));
                        //更新数据库
                        RequestDAO.updateDIInfo(request);
                        
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
                }catch(InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    private void checkusers() throws ParseException {
        List<SpeedInfo> lsp = SpeedInfoManager.get();
        if(lsp.size() == 0){
            return;
        }        
        for(SpeedInfo tmp:lsp) {
            if(isoutofdate(tmp))  {                                  
               deleteUserQos(tmp);
               //if(tmp.getnetinfo().size() ==0) {
                 //  lsp.remove(tmp);
               //}
            }
        }
    }
    
    private boolean isoutofdate(SpeedInfo speedinfo) throws ParseException {
        java.util.Date systemtime = new java.util.Date();
        java.text.SimpleDateFormat s = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        java.util.Date duetime = s.parse(speedinfo.getduedate());
        long currenttime = System.currentTimeMillis();
        if(speedinfo.gethearttime()=="" || speedinfo.gethearttime() == null) {
            return false;
        }
        long hearttime = s.parse(speedinfo.gethearttime()).getTime();        
        long lost = (currenttime - hearttime)/1000/60;
        if(systemtime.after(duetime) || lost > 5) {   //过期或者5分钟没有心跳
            return true;
        }else {
            return false;
        }        
    }
    
    private void deleteUserQos(SpeedInfo si) {
        if(si.getnetinfo() == null || si.getusername() == null) {
            return;
        }
        for(NetInfo tmp:si.getnetinfo()) {
            if(tmp.getcorrelationid() == null || tmp.getcorrelationid() == ""){
                break;
            }
            JSONObject delete = new JSONObject();
            Request request = new Request();
        
            delete.put("correlationId",tmp.getcorrelationid());
            delete.put("publicIP", tmp.getpublicIP());
                       
            request.setRequestType(2);  //deletetype
            request.setCorrelationID(tmp.getcorrelationid());
            java.util.Date systemtime = new java.util.Date();
            java.text.SimpleDateFormat s = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");
            request.setRequestTime(s.format(systemtime));                       
            request.setOperator(tmp.getoperator());        
            request.setUserName(si.getusername());
            request.setPackageName(tmp.getpackagename());                      
            request.setPublicIP(tmp.getpublicIP());
                       
            //写入数据库
            RequestDAO.insertRequestItem(request);
            //申请删除QOS
            final CountDownLatch latch = new CountDownLatch(1); 
                            HttpRequest.sendAsynGet("http://4gqos.h2comm.com.cn:8090/ivsp/services/AACAPIV1/getRemoveTecentGamesQos",delete, new FutureCallback<HttpResponse>()
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
                                           request.setResult(999);
                                       }
                                       }catch(JSONException e){ 
                                         Logger.getLogger(SpeedServlet.class.getName()).info("netinfo delete response is not jsonobject");
                                       }    
                                       
                                       //更新数据库
                                       RequestDAO.updateDIInfo(request);
                                       
                                       if(result.getString("ResultCode").equals("0")) {
                                           si.getnetinfo().remove(tmp);                                           
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
        }
              
       
    }
    
    private void testapply() {
        String params = "{\"Partner_ID\":\"moonbay\",\"security_token\":\"\",\"ResourceFeatureProperties\":[{\"FlowProperties\":[{\"MaximumUpStreamSpeedRate\":\"512000\",\"MaximumDownStreamSpeedRate\":\"2048000\",\"Protocol\":\"ip\",\"Direction\":\"2\",\"DestinationPort\":\"80\",\"DestinationIpAddress\":\"42.236.19.211\"}],\"Type\":\"1\",\"MinimumDownStreamSpeedRate\":\"1024000\",\"Priority\":\"15\",\"MinimumUpStreamSpeedRate\":\"204800\"}],\"Duration\":\"3600\",\"UserIdentifier\":{\"PublicIP\":\"112.65.48.253\",\"IP\":\"10.192.182.170\"},\"ServiceId\":\"Game-Tencent-DS\"}";
        
        final CountDownLatch latch = new CountDownLatch(1);
       
         HttpRequest.sendAsynPost("http://4gqos.h2comm.com.cn:8090/ivsp/services/AACAPIV1/applyTecentGamesQoS",params, new FutureCallback<HttpResponse>()
            {
            @Override
            public void completed(HttpResponse t) {
                //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
                latch.countDown();
                int result = 1;
            }

            @Override
            public void failed(Exception excptn) {
                //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
                latch.countDown();
                int result = 0;
            }

            @Override
            public void cancelled() {
                //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
                latch.countDown();
                int result = 2;
            }                
            });
         try {
                latch.await();
            }catch(InterruptedException e){
                e.printStackTrace();
            } 
    }
    
}
