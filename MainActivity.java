package com.example.foredownload1.activity;


//import androidx.activity.result.ActivityResultCallback;
//import androidx.activity.result.ActivityResultLauncher;
//import androidx.activity.result.contract.ActivityResultContract;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.Toast;

import com.example.foredownload1.adpter.LAdapter;
import com.example.foredownload1.thread.DownloadThread;
import com.example.foredownload1.R;
import com.example.foredownload1.models.AppModle;
import com.example.foredownload1.services.MyService;
import com.example.foredownload1.utils.DLutil;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    Handler handler;
    //String apkname;
    List<String> apklist ;
    Integer id = 1;
    private Button Downbutton,DownConfirmButton;
    private EditText editText;
    private List<AppModle> appModleList;
    private boolean FirstIN = false;
    private final String DATA_RESOURCES= "appDataSources.json";
    private List<String> applist = new ArrayList<>();
    private RecyclerView mRv;
    private LAdapter adapter;
   // private DownloadThread downloadThread;
    /**
     * MODE用来区别不同的下载模式，1是爬虫，2是预设
     */
    private Integer MODE_JSOUP = 1;
    private Integer MODE_CONFIRM = 2;

//myservice
    private MyService.DownloadBinder mDownloadBinder;
    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            mDownloadBinder = (MyService.DownloadBinder) iBinder;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

        }
    };
    //end

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case 0:
               // System.out.println("进入安装了");
               // System.out.println(apkname);
                //System.out.println(apklist.get(1));


                break;
            case 1:
                String path = data.getStringExtra("path");
                System.out.println(path);
                installApk(path);
                break;
            case 2:
                break;

        }


    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();

        //服务
         Intent intent = new Intent(this, MyService.class);
        startService(intent);
        bindService(intent, mConnection, BIND_AUTO_CREATE);

         //end
        //apklist = Arrays.asList(editText.getText().toString().split(","));
        handler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 0:
                        Toast.makeText(MainActivity.this,"请输入正确的应用名称",Toast.LENGTH_LONG);
                        break;
                    case 1:
                        System.out.println("handler传递了消息");
                        //installApk((String) msg.obj);
                        install((String) msg.obj);
                        break;
                    case 2:
                        Toast.makeText(MainActivity.this,"找不到当前apk下载包",Toast.LENGTH_LONG);
                        break;

                }
            }
        };
        //downloadThread = new DownloadThread(handler);

        Downbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               // apkname = editText.getText().toString();
                apklist = new ArrayList<>();
                String[] lists = editText.getText().toString().split("1");
                for(String list:lists){
                    System.out.println(list);
                }
                apklist.addAll(Arrays.asList(lists));
                //apklist.add(apkname);
                OpenAutoDown(0);
               DownloadThread downloadThread = new DownloadThread(handler);
                //MyService myService = new MyService(downloadThread,1);
                for(String apkname:apklist) {
                    Log.d("ww", "onClick: " +apkname);
                    adapter.add(apkname);
                    mDownloadBinder.startDownload(apkname,downloadThread,id++,1,null,adapter);
                }

            }
        });
        DownConfirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initData();
                if(appModleList == null){
                    Toast.makeText(MainActivity.this,"读取数据出现问题",Toast.LENGTH_LONG).show();
                    return;
                }
                OpenAutoDown(2);

                DownloadThread downloadThread = new DownloadThread(handler);
                for(AppModle appModle:appModleList) {
                    adapter.add(appModle.getAppname());
                    mDownloadBinder.startDownload(appModle.getAppname(), downloadThread, id++, 2,appModle.getApppath(),adapter);
                }




            }
        });
    }

    private void initData() {
        appModleList = DLutil.getJsonFromAssets(this,DATA_RESOURCES);

    }

    private void initView() {
        Downbutton = findViewById(R.id.down_button);
         editText = findViewById(R.id.editText);
        DownConfirmButton = findViewById(R.id.down_button_confirm);
        mRv = findViewById(R.id.recycleview);
        mRv.setLayoutManager(new LinearLayoutManager(this));
        mRv.addItemDecoration(new DividerItemDecoration(this,DividerItemDecoration.VERTICAL));
        adapter = new LAdapter();
        adapter.setApplist(applist);
        mRv.setAdapter(adapter);


    }

    /*
    private void installApkO(String downloadApkPath) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //是否有安装位置来源的权限
            boolean haveInstallPermission = getPackageManager().canRequestPackageInstalls();
            if (haveInstallPermission) {
                installApk( downloadApkPath);
            } else {
                launcher.launch("downloadApkPath");
            }
        } else {
            installApk(downloadApkPath);
        }
    }


     */
    public void OpenAutoDown(int requestCode){
        if(FirstIN){
            return;
        }
        FirstIN = true;
        Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
        startActivityForResult(intent,requestCode);

    }
    public void install(String path) {
        Boolean haveInstallPermission = getPackageManager().canRequestPackageInstalls();
        System.out.println("进入了");
        if (!haveInstallPermission) {
            Uri packageUri = Uri.parse("package:" + getPackageName());
            System.out.println("获取权限");
            Intent intent = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, packageUri);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("path",path);
            //startActivity(intent);
            startActivityForResult(intent, 1);
            //startActivity(intent);
            //installApk(path);
        }
        System.out.println("有权限了");
        installApk(path);

    }


    protected void installApk(String apkpath) {
        File apkFile = new File(apkpath);
        if (!apkFile.exists()) {
            return;
        }
        System.out.println(11);
        Intent intent = new Intent(Intent.ACTION_VIEW);
//      安装完成后，启动app（源码中少了这句话）
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        Uri uri = FileProvider.getUriForFile(this, "com.example.downloadsdk.provider", apkFile);
        intent.setDataAndType(uri, "application/vnd.android.package-archive");
        startActivity(intent);
    }
}
    /*
    ActivityResultLauncher launcher = registerForActivityResult(new ResultContract(), new ActivityResultCallback<String>() {
        @Override
        public void onActivityResult(String result) {
            Intent intent = getIntent();

            installApk(intent.getStringExtra("apkpath"));
        }
    });

    class ResultContract extends ActivityResultContract<String, String> {
        @NonNull
        @Override
        public Intent createIntent(@NonNull Context context, String input) {
            Uri packageUri = Uri.parse("package:"+ getPackageName());
            Intent intent = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,packageUri);
            intent.putExtra("apkpath",input);
            return intent;
        }

        @Override
        public String parseResult(int resultCode, @Nullable Intent intent) {
            return null;
        }
    }

}

 */