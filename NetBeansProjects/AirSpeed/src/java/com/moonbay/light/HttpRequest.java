/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.moonbay.light;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import net.sf.json.JSONObject;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.impl.nio.conn.PoolingNHttpClientConnectionManager;
import org.apache.http.impl.nio.reactor.DefaultConnectingIOReactor;
import org.apache.http.impl.nio.reactor.IOReactorConfig;
import org.apache.http.impl.nio.client.DefaultHttpAsyncClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.nio.client.HttpAsyncClient;
import org.apache.http.nio.reactor.ConnectingIOReactor;
import org.apache.http.nio.reactor.IOReactorException;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpParams;
import org.apache.log4j.Logger;


/**
 *
 * @author River
 */
public class HttpRequest {
    
    public static String sendGet(String url,String param) {
        String result = "";
        BufferedReader in = null;
        try {
            String urlName = url + "?" + param;
            URL realurl = new URL(urlName);
            URLConnection conn = realurl.openConnection();
            conn.setUseCaches(false);
            conn.setRequestProperty("accept","*/*");
            conn.setRequestProperty("connection","Keep-Alive");
            conn.setRequestProperty("user-agent",
                    "Mozilla/4.0 （compatible; MSIE 6.0; Windows NT 5.1;SV1）" );
            conn.connect();
            Map<String, List<String>> map = conn.getHeaderFields();
            //遍历所有的响应头字段
            for(String key:map.keySet()) {
                System.out.println(key + "-->" + map.get(key));
            }
            in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;
            while((line = in.readLine()) != null) {
                result += line;
            }            
        }catch(Exception e) {
            System.out.println("GET is failure" + e);
            e.printStackTrace();
        }
        finally {
            try {
                if(in != null) {
                    in.close();
                }
            }catch(Exception ex) {
                ex.printStackTrace();
            }
        }
        return result;
    }
    
    public static String sendPost(String url,String param) {
        PrintWriter out = null;
        BufferedReader in = null;
        String result = "";
        try {
            URL realurl = new URL(url);
            URLConnection conn = realurl.openConnection();
            conn.setUseCaches(false);
            conn.setRequestProperty("accept", "*/*");
            conn.setRequestProperty("connection","Keep-Alive");
            conn.setRequestProperty("user-agent", 
                    "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
            //POST一定要设置
            conn.setDoOutput(true);
            conn.setDoInput(true);
            
            out = new PrintWriter(conn.getOutputStream());
            out.print(param); //发送请求
            out.flush();  //flush输出流的缓冲
            
            in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;
            while((line = in.readLine()) != null) {
                result += line;
            }            
        }catch(Exception e) {
            System.out.println("POST is failure" + e);
            e.printStackTrace();
        }
        finally {
            try{
                if(out != null){
                    out.close();
                }
                if(in != null){
                    in.close();
                }
            }catch(Exception ex) {
                ex.printStackTrace();
            }
        }
        System.out.println("POST result: " + result);
        return result;
    }
    
    public static void sendAsynPost(String url, String params, FutureCallback<HttpResponse> callback ) {
        
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(50000)
                .setSocketTimeout(50000)
                .setConnectionRequestTimeout(1000)
                .build();
        
        //io线程
        IOReactorConfig ioReactorConfig = IOReactorConfig.custom()
                .setIoThreadCount(Runtime.getRuntime().availableProcessors())
                .setSoKeepAlive(true)
                .build();
        //连接池大小
        ConnectingIOReactor ioReactor = null;
        try {
            ioReactor = new DefaultConnectingIOReactor(ioReactorConfig);
        }catch (IOReactorException e) {
            e.printStackTrace();
        }
        PoolingNHttpClientConnectionManager connM = new PoolingNHttpClientConnectionManager(ioReactor);
        connM.setMaxTotal(100);
        connM.setDefaultMaxPerRoute(100);
        
        final CloseableHttpAsyncClient  client = HttpAsyncClients.custom()
                .setConnectionManager(connM)
                .setDefaultRequestConfig(requestConfig)
                .build();
        
        final HttpPost post = new HttpPost(url);   
        post.addHeader("Content-Type", "application/json;charset=utf-8");
        post.setHeader("Accept", "application/json");        
        //post.setHeader("Content-Length", url);
        //post.setHeader("Cache-Control", "no-cache");
        //post.setHeader("Pragma","no-cache");
        
        StringEntity entity = null;
        
        try{
            entity = new StringEntity(params);
        }catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } 
        post.setEntity(entity);
        client.start();        
        client.execute(post, callback);     
        Logger.getLogger(HttpRequest.class.getName()).info("AsynPost has sent "+params);
        //try {
          //  client.close();
        //}catch(IOException ignore){
            
        //}
    }
    
    public static void sendAsynGet(String url, JSONObject args, FutureCallback<HttpResponse> callback) {
        String uri;
        uri = jointuri(url,args);
        
        final CloseableHttpAsyncClient client = HttpAsyncClients.createDefault();       
        final HttpGet get = new HttpGet(uri);
        get.setHeader("Content-Type", "application/json;charset=utf-8");
        get.setHeader("Accept","application/json");
        //get.setHeader("Content-Length",uri);
        //get.setHeader("Cache-Control","no-cache");
        //get.setHeader("Pragma","no-cache");
        client.start();
        client.execute(get, callback);
        Logger.getLogger(HttpRequest.class.getName()).info("AsynGet has sent "+ args.toString());
        //try {
          //  client.close();
        //}catch(IOException ignore){
            
        //}
    }
    
    private static String jointuri(String url, JSONObject args) {
        String tmp = args.toString();
        String uri = url + "?" + tmp.substring(1,tmp.length()-1).replace(",", "&").replace(":", "=").replace("\"","");
        return uri;
    }
    
}

























