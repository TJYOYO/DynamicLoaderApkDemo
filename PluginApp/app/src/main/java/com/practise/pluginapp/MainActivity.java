package com.practise.pluginapp;

import android.os.Bundle;
import android.widget.TextView;

import com.ryg.dynamicload.DLBasePluginFragmentActivity;

public class MainActivity extends DLBasePluginFragmentActivity {

    TextView mTvContent;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTvContent = (TextView) findViewById(R.id.tv_content);

        mTvContent.setText("当前是插件页面MainActivity, \n 包名："+ this.getPackageName());

    }


}
