package com.example.foredownload1.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.example.foredownload1.adpter.LAdapter;
import com.example.foredownload1.listener.DownloadListener;
import com.example.foredownload1.thread.DownloadThread;
import com.example.foredownload1.activity.MainActivity;
import com.example.foredownload1.R;

public class MyService extends Service {
    //private DownloadThread downloadThread;
   // private Integer id = 1;
    private DownloadBinder mBinder = new DownloadBinder();
    //设置下载监听
    /*

private DownloadListener mDownloadListener = new DownloadListener() {

        @Override
        public void onProgress(int progress) {
            getNotificationManager().notify(id, getNotification("Downloading...", progress));
        }
    };

     */
    public MyService() {

    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return mBinder;
    }
   public class DownloadBinder extends Binder{
        //开始下载
        public void startDownload(String appname, DownloadThread downloadThread, Integer id, Integer mode, String url,LAdapter adapter){
            if(downloadThread != null){
                //服务和activity
                 DownloadListener mDownloadListener = new DownloadListener() {

                    @Override
                    public void onProgress(int progress) {
                        getNotificationManager().notify(id, getNotification(appname, progress,adapter,id));
                    }
                };
                downloadThread.setDownloadListener(mDownloadListener, appname);
                 if(mode == 1) {
                     Log.d("ww", "startDownload: "+appname);
                     downloadThread.getDownload(appname,getApplicationContext());
                 }
                 if(mode == 2){
                     downloadThread.Downn(appname,url,getApplicationContext());
                 }

                //开启前台任务
                startForeground(id, getNotification(appname , 0,adapter,id));
            }

        }
    }
    private NotificationManager getNotificationManager() {
        return (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    }
    private Notification getNotification(String title, int progress, LAdapter adapter,int id) {
        //点击通知以后跳转到主页面
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pi = PendingIntent.getActivity(this, 0, intent, 0);
        //创建channel
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        //android 8以后需要引进通道
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("001", "my_channel", NotificationManager.IMPORTANCE_DEFAULT);
            channel.enableLights(true); //是否在桌面icon右上角展示小红点
            channel.setLightColor(Color.GREEN); //小红点颜色
            channel.setShowBadge(true); //是否在久按桌面图标时显示此渠道的通知

            manager.createNotificationChannel(channel);
        }
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "001");
        builder.setSmallIcon(R.drawable.logo);
        builder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher));
        builder.setContentIntent(pi);
        builder.setContentTitle(title);
        if (progress > 0) {
            builder.setContentText(progress + "%");
            builder.setProgress(100, progress, false);
            adapter.setProgrss(progress,id);

        }
        /*
        if(progress != 0 && progress != 100 && progress % 10 == 0){
            adapter.setProgress(progress,title);
        }

        if(progress == 100){
            stopSelf();
        }

         */

        return builder.build();
    }

}