package com.example.foredownload1.thread;

import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.foredownload1.listener.DownloadListener;
import com.example.foredownload1.listener.ProgressResponseBody;
import com.example.foredownload1.models.AppModle;
import com.example.foredownload1.utils.DLutil;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.ConnectionPool;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.BufferedSink;
import okio.Okio;
import okio.Sink;

public class DownloadThread {
    Handler handler;
    ExecutorService executorService;
    HashMap<String, DownloadListener> map = new HashMap<>();
   // HashMap<Integer,Call> Callmap = new HashMap<>();




    private static final int minCONNECT_TIMEOUT = 3000;
    /**
     * Http 写入超时时间
     */
    private static final int minWRITE_TIMEOUT = 3000;
    /**
     * Http Read超时时间
     */
    private static final int minREAD_TIMEOUT = 3000;
    /**
     * Http Async Call Timeout
     */
    private static final int minCall_TIMEOUT = 3000;
    /**
     * Http连接池
     */
    private static final int connectionPoolSize = 1000;
//    /**
//     * 静态Http请求池
//     */
//    private static OkHttpClient client=null;
    /**
     * 静态连接池对象
     */
    private static ConnectionPool mConnectionPool=new ConnectionPool(connectionPoolSize, 30, TimeUnit.MINUTES);
    //executor = new ThreadPoolExecutor(0, 2147483647, ...这个是静态的，21亿连接数量
//    p

   // DownloadListener downloadListener;
   // private DownloadListener downloadListener;


    public DownloadThread(Handler handler) {
        this.handler = handler;

        executorService = Executors.newFixedThreadPool(10);
    }

    public void setDownloadListener(DownloadListener downloadListener,String apkname) {
        System.out.println(apkname);
        if(apkname != null && downloadListener!= null) {
            map.put(apkname, downloadListener);
        }
    }

    public void getDownload(String apkname, Context context) {
        if(apkname == null){
            Message message = new Message();
            message.what = 1;
            handler.sendMessage(message);
            return;
        }
        String url = DLutil.downLoadUrl + apkname;
        Log.d("ww", "getDownload: "+url);
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    Document document = Jsoup.connect(url)
                            .userAgent("Opera/9.25 (Windows NT 5.1; U; en)")
                            .execute()
                            .parse();
                    Element element = document.selectFirst("body > div.warp > div.main > div > ul > li:nth-child(1) > div > div.download.comdown > a");
                    if(element == null){
                        Message message = new Message();
                        message.what = 2;
                        handler.sendMessage(message);
                        return;
                    }
                    Log.d("ww", "run: " + url);
                    String durl = element.attr("href");
                    durl = durl.replaceAll("&amp;","&");
                    Log.d("ww", "run: "+ durl);
                    /*
                    durl = durl + "/download";
                    Document document1 = Jsoup.connect(url)
                            .userAgent("Opera/9.25 (Windows NT 5.1; U; en)")
                            .execute()
                            .parse();
                    //Document document1 = Jsoup.parse(durl);
                    Element element1 = document1.selectFirst("#detail-download-button");
                    durl = element1.attr("href");

                     */

                    Downn(apkname, durl, context);
                    Log.d("ww", "run: " +durl);

                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        });
    }
/*
    public OkHttpClient getProgressClient() {
        // 拦截器，用上ProgressResponseBody
        Interceptor interceptor = new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Response originalResponse = chain.proceed(chain.request());
                return originalResponse.newBuilder()
                        .body(new ProgressResponseBody(originalResponse.body(), progressListener))
                        .build();
            }
        };
        return new OkHttpClient.Builder()
                .addNetworkInterceptor(interceptor)
                .build();
    }

 */
    public void Downn(String apkname, String apkUrl,Context context) {
        ProgressResponseBody.ProgressListener progressListener = new ProgressResponseBody.ProgressListener() {
            long total;
            @Override
            public void onPreExecute(long contentLength) {
                total = contentLength;
            }

            @Override
            public void update(long totalBytes, boolean done) {
               int  mProgress = (int) (totalBytes * 1.0f / total * 100);
               map.get(apkname).onProgress(mProgress);
            }
        };
        Interceptor interceptor = new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Response originalResponse = chain.proceed(chain.request());
                return originalResponse.newBuilder()
                        .body(new ProgressResponseBody(originalResponse.body(), progressListener))
                        .build();
            }
        };
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .addNetworkInterceptor(interceptor)
                .build();
           // OkHttpClient okHttpClient = new OkHttpClient();
        final long startTime = System.currentTimeMillis();
                /*
                .newBuilder()
                .connectTimeout(minCONNECT_TIMEOUT, TimeUnit.MILLISECONDS) //连接超时
                .readTimeout(minREAD_TIMEOUT, TimeUnit.MILLISECONDS) //读取超时
                .writeTimeout(minWRITE_TIMEOUT, TimeUnit.MILLISECONDS) //写超时
                .callTimeout(minCall_TIMEOUT, TimeUnit.MILLISECONDS)
                // okhttp默认使用的RealConnectionPool初始化线程数==2147483647，在服务端会导致大量线程TIMED_WAITING
                //ThreadPoolExecutor(0, 2147483647, 60L, TimeUnit.SECONDS, new SynchronousQueue(), Util.threadFactory("OkHttp ConnectionPool", true));
                //.connectionPool(mConnectionPool)
                .build();

                 */

        Request request = new Request.Builder()
                .url(apkUrl)
                .addHeader("Connection", "close")
                //.header("RANGE", "bytes=" + startPoints + "-")
                .build();
        //异步加载不需要开启新线程
        //Call call = okHttpClient.newCall(request);
       // Callmap.put(id,call);
        //okHttpClient.newCall(request).enqueue();
       okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {

            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                Sink sink = null;
                BufferedSink bufferedSink = null;
                try {
                    // String mSDCardPath= Environment.getExternalStorageDirectory().getAbsolutePath();//SD卡路径
                    //String appPath= getApplicationContext().getFilesDir().getAbsolutePath();//此APP的files路径
                    String path = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) + "/" + apkname + ".apk";
                    Log.i("DOWNLOAD",path);
                    File dest = new File(path);
                    sink = Okio.sink(dest);
                    bufferedSink = Okio.buffer(sink);
                    //真正写入的文件
                    bufferedSink.writeAll(response.body().source());
                    Log.i("DOWNLOAD",response.body().source().toString());
                    bufferedSink.close();
                    Log.i("DOWNLOAD","download success");
                    Message message = new Message();
                    message.obj = path;
                    message.what = 1;
                    handler.sendMessage(message);
                    Log.i("DOWNLOAD","totalTime="+ (System.currentTimeMillis() - startTime));
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.i("DOWNLOAD","download failed");
                } finally {
                    if(bufferedSink != null){
                        bufferedSink.close();
                    }
                }
            }

            /*
            直接放进哦那Response

             */
 /*
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                System.out.println("respnse");

                    InputStream is = response.body().byteStream();
                    long total = response.body().contentLength();
                    byte[] buf = new byte[2048];
                    String path = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) + "/" + apkname + ".apk";
                    File file = new File(path);
                    FileOutputStream fos = new FileOutputStream(file);
                    int len = 0;
                    long sum = 0;
                    int mProgress;
                    while ((len = is.read(buf)) != -1) {
                        fos.write(buf, 0, len);
                        sum += len;
                        mProgress = (int) (sum * 1.0f / total * 100);
                        map.get(apkname).onProgress(mProgress);
                        // downloadListener.onProgress(mProgress);
                        System.out.println(mProgress);
                    }
                    System.out.println("推出了循环");
                    fos.flush();
                    fos.close();
                    is.close();
                    Message message = new Message();
                    message.obj = path;
                    message.what = 1;
                    handler.sendMessage(message);
                    System.out.println("发送了信息");
            }

             */

        });
    }
    private void save(Response response, Context context,String apkname,long startsPoint) {
        String path = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) + "/" + apkname + ".apk";
        File destination = new File(path);
        ResponseBody body = response.body();
        InputStream in = body.byteStream();
        FileChannel channelOut = null;
        // 随机访问文件，可以指定断点续传的起始位置
        RandomAccessFile randomAccessFile = null;
        try {
            randomAccessFile = new RandomAccessFile(destination, "rwd");
            //Chanel NIO中的用法，由于RandomAccessFile没有使用缓存策略，直接使用会使得下载速度变慢，亲测缓存下载3.3秒的文件，用普通的RandomAccessFile需要20多秒。
            channelOut = randomAccessFile.getChannel();
            // 内存映射，直接使用RandomAccessFile，是用其seek方法指定下载的起始位置，使用缓存下载，在这里指定下载位置。
            MappedByteBuffer mappedBuffer = channelOut.map(FileChannel.MapMode.READ_WRITE, startsPoint, body.contentLength());
            byte[] buffer = new byte[1024];
            int len;
            while ((len = in.read(buffer)) != -1) {
                mappedBuffer.put(buffer, 0, len);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                in.close();
                if (channelOut != null) {
                    channelOut.close();
                }
                if (randomAccessFile != null) {
                    randomAccessFile.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }



}
