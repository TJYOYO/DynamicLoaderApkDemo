package com.ryg.utils;

/**
 * 接口回调
 * Created by 15032065 on 17/2/21.
 */

public interface PluginManagerLinstener {

    //开始检查服务器版本
    void onStartCheckVersionOnServer();
    //开始初始化插件
    void onInitPlugin();
    //总长度
    void onAllLength(double length);
    //每次下载量
    void onProgress(double length);
    //下载完成
    void onDownLoadFinish();
    //开始加载插件
    void onLoadPlugin();
    //加载完成
    void onFinish();
    //错误
    void onError(int errorCode, String message);


}
