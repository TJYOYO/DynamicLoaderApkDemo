package com.ryg.utils;

/**
 * Created by 15032065 on 17/2/15.
 */

public class LoadResult {
    public PluginFile plugin;
    public boolean isLoadSuccess;
    public int errorCode;
    public String message;


    //1:直接下载 安装 2:直接copy 安装 3:无需下载 无需安装
    public int installCode;
    //版本
    public String version;
    public LoadResult(PluginFile plugin, boolean isLoadSuccess, int errorCode, String message)
    {
        this.plugin = plugin;
        this.isLoadSuccess = isLoadSuccess;
        this.errorCode = errorCode;
        this.message = message;
    }
}
