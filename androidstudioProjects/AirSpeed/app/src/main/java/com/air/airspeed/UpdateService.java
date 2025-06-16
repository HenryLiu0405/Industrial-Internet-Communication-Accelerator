package com.air.airspeed;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.RemoteViews;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;


import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static android.app.PendingIntent.getActivity;


public class UpdateService extends Service  {
    //标题
    private int titleId = 0;
    //文件存储
    private File updateDir = null;
    private File updateFile = null;
    //下载状态
    private final static int DOWNLOAD_COMPLETE = 0;
    private final static int DOWNLOAD_FAIL = 1;
    //通知栏
    private NotificationManager updateNotificationManager = null;
    private Notification updateNotification = null;
    //通知栏跳转Intent
    private Intent updateIntent = null;
    private PendingIntent updatePendingIntent = null;
    //创建通知栏
    int downloadCount = 0;
    int currentSize = 0;
    long totalSize = 0;
    int updateTotalSize = 0;
    //下载包安装路径
    private final static String savePpath = Environment.getExternalStorageDirectory().getAbsolutePath() +  "/AirSpeed/";
    private final static String saveFileName = savePpath + "AirSpeed.apk";

    public UpdateService() {
    }

    @SuppressWarnings("deprecation")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //获取传值
        titleId = intent.getIntExtra("titleId",0);
        //创建文件
        if (Environment.MEDIA_MOUNTED.equals(android.os.Environment.getExternalStorageState())) {
            updateDir = new File(Environment.getExternalStorageDirectory(),saveFileName);
            updateFile = new File(Environment.getExternalStorageDirectory(),getResources().getString(titleId)+".apk");
        }
        this.updateNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        //this.updateNotification = new Notification();
        //设置下载过程中，点击通知栏，回到主界面
        updateIntent = new Intent(UpdateService.this,ConfigActivity.class);
        updatePendingIntent = PendingIntent.getActivity(this,0,updateIntent,0);
        //设置通知栏显示内容
        //updateNotification.icon = R.drawable.ic_launcher_foreground;
        //updateNotification.tickerText = "开始下载";
        //updateNotification.setLatestEventInfo(UpdateService.this,"AirSpeed","0%",updatePendingIntent);
        this.updateNotification = new Notification.Builder(this)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("AirSpeed")
                .setTicker("开始下载")
                .setContentText("0%")
                .setContentIntent(updatePendingIntent).setNumber(0)
                .getNotification();
        updateNotification.flags|= Notification.FLAG_AUTO_CANCEL;
        //发出通知
        updateNotificationManager.notify(0,updateNotification);

        //开启新线程下载，如果使用service同步下载，会导致ANR，service本身也会阻塞
        new Thread(new updateRunnable()).start();//下载重点，是下载的过程

        return super.onStartCommand(intent,flags,startId);
    }


    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @SuppressWarnings("HandlerLeak")
    private Handler updateHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case DOWNLOAD_COMPLETE:
                    //点击安装PendingIntent
                    Intent installIntent = new Intent(Intent.ACTION_VIEW);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        installIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        Uri uri = FileProvider.getUriForFile(UpdateService.this,BuildConfig.APPLICATION_ID+".fileprovider",updateFile);
                        installIntent.setDataAndType(uri,"application/vnd.android.package-archive");
                    }else {
                        Uri uri = Uri.fromFile(updateFile);
                        installIntent.setDataAndType(uri,"application/vnd.android.package-archive");
                        installIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    }

                    updatePendingIntent = PendingIntent.getActivity(UpdateService.this,0,installIntent,0);
                    //updateNotification.setLatestEventInfo(UpdateService.this,
                    //						"QQ", "下载完成,点击安装。", updatePendingIntent);
                    NotificationCompat.Builder builder = new NotificationCompat.Builder(UpdateService.this)
                            .setSmallIcon(R.drawable.ic_launcher_foreground)
                            .setDefaults(Notification.DEFAULT_SOUND)
                            .setContentTitle("AirSpeed")
                            .setContentText("下载完成，点击安装")
                            .setContentIntent(updatePendingIntent).setNumber(0);
                    //updateNotification.flags|= Notification.FLAG_AUTO_CANCEL;
                    updateNotificationManager.notify(0,builder.build());
                    //停止服务
                    stopService(updateIntent);
                    break;
                case DOWNLOAD_FAIL:
                    //下载失败
                    NotificationCompat.Builder builder1 = new NotificationCompat.Builder(UpdateService.this)
                            .setSmallIcon(R.drawable.ic_launcher_foreground)
                            .setContentTitle("AirSpeed")
                            .setContentText("下载失败")
                            .setContentIntent(updatePendingIntent).setNumber(0);
                    //updateNotification.flags|= Notification.FLAG_AUTO_CANCEL;
                    updateNotificationManager.notify(0,builder1.build());
                    break;
                default:
                    stopService(updateIntent);
            }
        }
    };

    public long downloadUpdateFile(String downloadUrl, File saveFile) throws Exception {
        HttpURLConnection httpConnection = null;
        InputStream is = null;
        FileOutputStream fos = null;

        try {
            URL url = new URL(downloadUrl);
            httpConnection = (HttpURLConnection) url.openConnection();
            httpConnection.setRequestMethod("POST");
            httpConnection.setRequestProperty("User-Agent","PacificHttpClient");
            if (currentSize > 0) {
                httpConnection.setRequestProperty("RANGE","bytes=" + currentSize + "-");
            }
            httpConnection.setConnectTimeout(1000000);
            httpConnection.setReadTimeout(2000000);
            updateTotalSize = httpConnection.getContentLength();
            if (httpConnection.getResponseCode() == 404) {
                throw new Exception("fail!");
            }
            is = httpConnection.getInputStream();
            fos = new FileOutputStream(saveFile,false);
            byte buffer[] = new byte[4096];
            int readsize = 0;
            while ((readsize = is.read(buffer)) > 0) {
                fos.write(buffer,0,readsize);
                totalSize += readsize;
                //防止频繁的通知导致应用吃紧，百分比增加10才通知一次
                int len = (int)(totalSize*100/updateTotalSize)-1;
                if ((downloadCount == 0) || (len > downloadCount)) {
                    downloadCount +=1;
                    NotificationCompat.Builder builder2 = new NotificationCompat.Builder(UpdateService.this)
                            .setSmallIcon(R.drawable.ic_launcher_foreground)
                            .setContentTitle("正在下载")
                            .setContentText((int) (totalSize*100/updateTotalSize) + "%")
                            .setContentIntent(updatePendingIntent).setNumber(0);
                    //updateNotification.flags|= Notification.FLAG_AUTO_CANCEL;

                    //使用自定义view来显示Notification
                    updateNotification = builder2.build();
                    updateNotification.flags|= Notification.FLAG_AUTO_CANCEL;
                    updateNotification.contentView = new RemoteViews(getPackageName(),R.layout.notification_item);
                    updateNotification.contentView.setTextViewText(R.id.notificationTitle,"正在下载");
                    updateNotification.contentView.setProgressBar(R.id.notificationProgress,100,downloadCount,false);
                    updateNotificationManager.notify(0,updateNotification);
                }
            }
        }finally {
            if (httpConnection != null) {
                httpConnection.disconnect();
            }
            if (is != null) {
                is.close();
            }
            if (fos != null) {
                fos.close();
            }
        }
        return totalSize;
    }

    class updateRunnable implements Runnable {
        Message message = updateHandler.obtainMessage();

        public void run() {
            message.what = DOWNLOAD_COMPLETE;
            try {

                if (updateFile.exists()) {
                    updateFile.delete();
                }
                updateFile.createNewFile();
                long downloadSize = downloadUpdateFile("http://p22n940119.iok.la/AirSpeed/DownloadServlet?filepath=AirSpeed.apk", updateFile);
                if (downloadSize >0) {
                    //下载成功
                    updateHandler.sendMessage(message);
                }
            }catch (Exception ex) {
                ex.printStackTrace();
                message.what = DOWNLOAD_FAIL;
                //下载失败
                updateHandler.sendMessage(message);
            }
        }
    }
}
