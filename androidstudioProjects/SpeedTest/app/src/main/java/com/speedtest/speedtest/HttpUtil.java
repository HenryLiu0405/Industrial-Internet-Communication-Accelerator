package com.speedtest.speedtest;

import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

import okhttp3.OkHttpClient;
import okhttp3.Request;

public class HttpUtil {


    private static final int TIME_OUT = 30;
    private static OkHttpClient mOkHttpClient;

    //config client
    static{
        OkHttpClient.Builder okHttpBuilder = new OkHttpClient().newBuilder();
        okHttpBuilder.connectTimeout(TIME_OUT, TimeUnit.SECONDS);
        okHttpBuilder.readTimeout(TIME_OUT, TimeUnit.SECONDS);
        okHttpBuilder.writeTimeout(TIME_OUT, TimeUnit.SECONDS);

        //support redirect
        okHttpBuilder.followRedirects(true);

        //support https, later
        okHttpBuilder.hostnameVerifier(new HostnameVerifier() {
            @Override
            public boolean verify(String hostname, SSLSession session)  {
                return true;
            }
        });
        //okHttpBuilder.sslSocketFactory(sslSocketFactory, X509ExtendedTrustManager);

        //make client
        mOkHttpClient = okHttpBuilder.build();
    }

    public static void sendOkHttpRequest(Request request, okhttp3.Callback callback) {
        mOkHttpClient.newCall(request).enqueue(callback);
    }
}
