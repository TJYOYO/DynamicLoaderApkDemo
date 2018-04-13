package com.ryg.sample;

import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.ryg.R;
import com.ryg.dynamicload.internal.DLIntent;
import com.ryg.dynamicload.internal.DLPluginManager;
import com.ryg.dynamicload.internal.DLPluginPackage;
import com.ryg.utils.PluginFile;
import com.ryg.utils.PluginTools;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    TextView tv_load_plugin,tv_start_plugin;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tv_load_plugin = (TextView) findViewById(R.id.tv_load_plugin);
        tv_start_plugin = (TextView) findViewById(R.id.tv_start_plugin);

        tv_load_plugin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "开始加载插件apk....",Toast.LENGTH_SHORT).show();
                loadPlugin();
            }
        });

        tv_start_plugin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                Toast.makeText(MainActivity.this, "开始启动插件页面....",Toast.LENGTH_SHORT).show();
                startPluginActivity(MainActivity.this, new DLIntent("com.practise.pluginapp"
                        , "com.practise.pluginapp.TestActivity"));
            }
        });
    }


    private void loadPlugin() {

        //apk名称
        String apkName = "PluginApp.apk";

        //插件apk的位置
        String apkAllPath = Environment.getExternalStorageDirectory().getAbsolutePath()
                + "/" + apkName;

        //加载插件apk到内存中
        DLPluginPackage dlPluginPackage = DLPluginManager.getInstance(this).loadApk(apkAllPath);

        if(dlPluginPackage.classLoader != null){
            Toast.makeText(this, "加载插件apk到内存中成功!",Toast.LENGTH_SHORT).show();
        }

    }

//        PluginFile pluginFile = new PluginFile();
//        pluginFile.fileName = apkName;
//        pluginFile.fileAllPath = apkAllPath;

//        List<PluginFile> listPlugin = new ArrayList<>();
//        listPlugin.add(pluginFile);

//        PluginTools.loadPluginTest(this, listPlugin);


    private void startPluginActivity(Context context, DLIntent intent) {
        DLPluginManager dlPluginManager = DLPluginManager.getInstance(context);
        if (!dlPluginManager.isHostPackageSet()){
            dlPluginManager.setHostPackageName("com.ryg");
        }
        dlPluginManager.startPluginActivity(this, intent);
    }



}
