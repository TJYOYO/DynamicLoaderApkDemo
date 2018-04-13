package com.ryg.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.ryg.dynamicload.internal.DLPluginManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by 15032065 on 17/2/15.
 */

public class PluginTools {

    //插件的目录
    private static final String PATH = "";

    /**
     * 检查版本 安装方式
     * @param context
     * @param plugins
     */
    public static List<LoadResult> checkPlugins(Context context, PluginFile plugins[])
    {
        if (plugins == null || plugins.length == 0)return null;
        List<LoadResult> res = new ArrayList<LoadResult>();
        for (PluginFile plugin : plugins){
            //当前最新的版本
            Version version = getPluginVersion(plugin);
            if (version == null || TextUtils.isEmpty(version.getVersion()))
            {
                res.add(new LoadResult(plugin,false,-1,"插件获取版本失败!"));

                continue;
            }

            LoadResult loadResult = new LoadResult(plugin,true,1,"success!");
            //匹配版本
            //如果是－1 表示没有安装过
            String currentVersion = getVersion(context,plugin.fileName);

            int installCode = -1;
            //安装过 且 最新版本当前版本不一样 直接下载 安装
            if (!currentVersion.equals("-1") && !version.getVersion().equals(currentVersion))
            {
                installCode = 1;
            }
            //没安装 且 最新的版本和当前版本不一样 直接下载 安装
            else if (currentVersion.equals("-1") && !version.getVersion().equals(plugin.version))
            {
                installCode = 1;
            }
            //没安装 且 最新的版本和当前版本一样 直接copy 安装
            else if (currentVersion.equals("-1")&&version.getVersion().equals(plugin.version))
            {
                installCode = 2;
            }
            //不用安装
            else if (currentVersion.equals(version.getVersion()))
            {
                //比较当前目录下的文件是否存在
                String to = context.getApplicationContext().getFilesDir()
                        .getAbsolutePath() + PATH;
                File file = new File(to + "/" +plugin.fileName);
                if (file.exists())
                    installCode = 3;
                else
                    installCode = 2;
            }
            else
            {
                installCode = 1;
            }
            loadResult.installCode = installCode;
            res.add(loadResult);
            loadResult.version = version.getVersion();
            loadResult.plugin.pluginDownloadPath = version.getDownloadAddr();
        }
        return res;
    }
    /**
     * 灰度版本的检查插件方法
     * 检查版本 安装方式
     * @param context
     * @param plugins
     */
    public static List<LoadResult> checkPluginsForGray(Context context, PluginFile plugins[])
    {
        if (plugins == null || plugins.length == 0)return null;
        List<LoadResult> res = new ArrayList<LoadResult>();
        for (PluginFile plugin : plugins){
            //当前最新的版本
            Version version = getPluginVersion(plugin);
            if (version == null || TextUtils.isEmpty(version.getVersion()))
            {
                res.add(new LoadResult(plugin,false,-1,"插件获取版本失败!"));
                continue;
            }

            LoadResult loadResult = new LoadResult(plugin,true,1,"success!");
            //匹配版本
            //如果是－1 表示没有安装过
            String currentVersion = getVersion(context,plugin.fileName);

            int installCode = -1;
            //更新标识不为1，表示不需要更新
            if ("0".equals(version.getUpdateFlag())){
                installCode = 3;
            }
            //安装过 且 最新版本当前版本不一样 直接下载 安装
            else if (!currentVersion.equals("-1") && !version.getVersion().equals(currentVersion))
            {
                installCode = 1;
            }
            //没安装 且 最新的版本和当前版本不一样 直接下载 安装
            else if (currentVersion.equals("-1") && !version.getVersion().equals(plugin.version))
            {
                installCode = 1;
            }
            //没安装 且 最新的版本和当前版本一样 直接copy 安装
            else if (currentVersion.equals("-1")&&version.getVersion().equals(plugin.version))
            {
                installCode = 2;
            }
            //不用安装
            else if (currentVersion.equals(version.getVersion()))
            {
                //比较当前目录下的文件是否存在
                String to = context.getApplicationContext().getFilesDir()
                        .getAbsolutePath() + PATH;
                File file = new File(to + "/" +plugin.fileName);
                if (file.exists())
                    installCode = 3;
                else
                    installCode = 2;
            }
            else
            {
                installCode = 1;
            }
            loadResult.installCode = installCode;
            res.add(loadResult);
            loadResult.version = version.getVersion();
            loadResult.plugin.pluginDownloadPath = version.getDownloadAddr();
        }
        return res;
    }

    /**
     * v2.0 针对之前checkPlugins的版本做了优化，去除考虑本地copy的逻辑，要求全部在线更新
     * @param context
     * @param plugins
     * @return
     */
    public static List<LoadResult> checkPlugins_V2(Context context, PluginFile plugins[])
    {
        if (plugins == null || plugins.length == 0)return null;
        List<LoadResult> res = new ArrayList<LoadResult>();
        for (PluginFile plugin : plugins){
            //当前最新的版本
            Version version = getPluginVersion(plugin);
            if (version == null || TextUtils.isEmpty(version.getVersion()))
            {
                res.add(new LoadResult(plugin,false,-1,"插件获取版本失败!"));
                continue;
            }

            LoadResult loadResult = new LoadResult(plugin,true,1,"success!");
            //匹配版本
            //如果是－1 表示没有安装过
            String currentVersion = getVersion(context,plugin.fileName);

            int installCode = -1;
            //没安装过 或 最新版本当前版本不一样 直接下载 安装
            if (currentVersion.equals("-1") || !version.getVersion().equals(currentVersion))
            {
                installCode = 1;
            }
            //不用安装
            else if (currentVersion.equals(version.getVersion()))
            {
                //比较当前目录下的文件是否存在
                String to = context.getApplicationContext().getFilesDir()
                        .getAbsolutePath() + PATH;
                File file = new File(to + "/" +plugin.fileName);
                if (file.exists())
                    installCode = 3;
                else
                    //文件不存在直接下载
                    installCode = 1;
            }
            else
            {
                installCode = 1;
            }
            loadResult.installCode = installCode;
            res.add(loadResult);
            loadResult.version = version.getVersion();
            loadResult.plugin.pluginDownloadPath = version.getDownloadAddr();
        }
        return res;
    }


    /**
     * 加载plugin
     * @param context 上下文
     * @param loadResults plugin数组
     */
    public static List<LoadResult> initPlugins(Context context, List<LoadResult> loadResults, Handler handler) {

        if (loadResults == null || loadResults.size() == 0)return null;
        List<LoadResult> res = new ArrayList<LoadResult>();
        for (LoadResult loadResult : loadResults){
            if (loadResult.installCode == 1)//下载新版本 & copy
            {
                //文件下载
                if (downLoadFile(context,loadResult.plugin.pluginDownloadPath,loadResult.plugin.fileName,handler))
                {
                    loadResult.message = "安装成功";
                    loadResult.errorCode = 1;
                    loadResult.isLoadSuccess = true;
                    res.add(loadResult);
                    setVersion(context,loadResult.plugin.fileName,loadResult.version);
                }
                else
                {
                    loadResult.message = "安装失败!";
                    loadResult.errorCode = -1;
                    loadResult.isLoadSuccess = false;
                    res.add(loadResult);
                }
            }
            else if (loadResult.installCode == 2)//直接copy
            {
                if (copyPlugin(context,loadResult.plugin.fileName))
                {
                    loadResult.message = "安装成功";
                    loadResult.errorCode = 1;
                    loadResult.isLoadSuccess = true;
                    res.add(loadResult);
                    setVersion(context,loadResult.plugin.fileName,loadResult.version);
                }
                else
                {
                    loadResult.message = "安装失败!";
                    loadResult.errorCode = -1;
                    loadResult.isLoadSuccess = false;
                    res.add(loadResult);
                }
            }
            else if(loadResult.installCode == 3)
            {
                loadResult.message = "当前插件不需要安装";
                loadResult.errorCode = 1;
                loadResult.isLoadSuccess = true;
                res.add(loadResult);
            }
            else
            {
                //错误
                loadResult.message = "当前插件安装状态未知！";
                loadResult.errorCode = -1;
                loadResult.isLoadSuccess = false;
                res.add(loadResult);
            }
        }
        return res;
    }

    /**
     * 加载plugin
     * @param context 上下文
     * @param loadResults plugin数组
     */
    public static List<LoadResult> initPlugins_V2(Context context, List<LoadResult> loadResults, PluginManagerLinstener pluginListener)
    {

        if (loadResults == null || loadResults.size() == 0)return null;
        List<LoadResult> res = new ArrayList<LoadResult>();
        for (LoadResult loadResult : loadResults){
            if (loadResult.installCode == 1)//下载新版本 & copy
            {
                //文件下载
                if (downLoadFile_V2(context,loadResult.plugin.pluginDownloadPath,loadResult.plugin.fileName,pluginListener))
                {
                    loadResult.message = "安装成功";
                    loadResult.errorCode = 1;
                    loadResult.isLoadSuccess = true;
                    res.add(loadResult);
                    setVersion(context,loadResult.plugin.fileName,loadResult.version);
                }
                else
                {
                    loadResult.message = "安装失败!";
                    loadResult.errorCode = -1;
                    loadResult.isLoadSuccess = false;
                    res.add(loadResult);
                }
            }
            else if (loadResult.installCode == 2)//直接copy
            {
                if (copyPlugin(context,loadResult.plugin.fileName))
                {
                    loadResult.message = "安装成功";
                    loadResult.errorCode = 1;
                    loadResult.isLoadSuccess = true;
                    res.add(loadResult);
                    setVersion(context,loadResult.plugin.fileName,loadResult.version);
                }
                else
                {
                    loadResult.message = "安装失败!";
                    loadResult.errorCode = -1;
                    loadResult.isLoadSuccess = false;
                    res.add(loadResult);
                }
            }
            else if(loadResult.installCode == 3)
            {
                loadResult.message = "当前插件不需要安装";
                loadResult.errorCode = 1;
                loadResult.isLoadSuccess = true;
                res.add(loadResult);
            }
            else
            {
                //错误
                loadResult.message = "当前插件安装状态未知！";
                loadResult.errorCode = -1;
                loadResult.isLoadSuccess = false;
                res.add(loadResult);
            }
        }
        return res;
    }

    /**
     * 加载插件包内容 注意检查最后的包的size,防止异常加载失败
     * @param context
     * @param pluginFiles
     * @return
     */
    public static List<PluginItem> loadPluginTest(Context context, List<PluginFile> pluginFiles)
    {
        List<PluginItem> pluginItems = new ArrayList<PluginItem>();
        //判断有没有插件
        if (pluginFiles == null || pluginFiles.size() == 0) {
            return pluginItems;
        }
        for (PluginFile pluginFile : pluginFiles)
        {
//            File plugin = new File("./app/warehouse.apk");
//            File plugin = new File(pluginFile.fileAllPath);
            String exPath = Environment.getExternalStorageDirectory().getAbsolutePath();
            String allPath = exPath + "/"+pluginFile.fileName;

            File plugin = new File(allPath);
            boolean isExists = plugin.exists();
            if (!isExists){
                continue;
            }
            PluginItem item = new PluginItem();
            item.fileName = pluginFile.fileName;
            item.pluginPath = plugin.getAbsolutePath();
            item.packageInfo = DLUtils.getPackageInfo(context, item.pluginPath);
            //获取插件的启动Activity的名称
            if (item.packageInfo.activities != null && item.packageInfo.activities.length > 0) {
                item.launcherActivityName = item.packageInfo.activities[0].name;
            }
            //获取插件启动Service的名称
            if (item.packageInfo.services != null && item.packageInfo.services.length > 0) {
                item.launcherServiceName = item.packageInfo.services[0].name;
            }
            pluginItems.add(item);
            //加载插件
            DLPluginManager.getInstance(context).loadApk(item.pluginPath);
        }

        return pluginItems;
    }


    /**
     * 加载插件包内容 注意检查最后的包的size,防止异常加载失败
     * @param context
     * @param pluginFiles
     * @return
     */
    public static List<PluginItem> loadPlugin(Context context, List<PluginFile> pluginFiles)
    {
        List<PluginItem> pluginItems = new ArrayList<PluginItem>();
        //判断有没有插件
        if (pluginFiles == null || pluginFiles.size() == 0) {
            return pluginItems;
        }
        String to = context.getApplicationContext().getFilesDir()
                .getAbsolutePath() + PATH;
        for (PluginFile pluginFile : pluginFiles)
        {
            File plugin = new File(to+"/"+pluginFile.fileName);
            if (!plugin.exists())continue;
            PluginItem item = new PluginItem();
            item.fileName = pluginFile.fileName;
            item.pluginPath = plugin.getAbsolutePath();
            item.packageInfo = DLUtils.getPackageInfo(context, item.pluginPath);
            //获取插件的启动Activity的名称
            if (item.packageInfo.activities != null && item.packageInfo.activities.length > 0) {
                item.launcherActivityName = item.packageInfo.activities[0].name;
            }
            //获取插件启动Service的名称
            if (item.packageInfo.services != null && item.packageInfo.services.length > 0) {
                item.launcherServiceName = item.packageInfo.services[0].name;
            }
            pluginItems.add(item);
            //加载插件
            DLPluginManager.getInstance(context).loadApk(item.pluginPath);
        }

        return pluginItems;
    }


    /**
     * 拷贝assets下的文件到指定目录
     *
     * @param context
     * @param fileName assets 下的pugin名 如:pluginA.so
     */
    private static boolean copyPlugin(Context context, String fileName) {
        if (context == null || fileName == null) return false;
        String to = context.getApplicationContext().getFilesDir()
                .getAbsolutePath() + PATH;
        //检查文件夹是否存在
        File fileDirs = new File(to);
        if (!fileDirs.exists()) {
            //文件夹不存在，创建
            fileDirs.mkdirs();
        }
        //文件覆盖
        String tmpFilePath = to + "/" + fileName.split("\\.")[0]+".tmp";
        File file = new File(tmpFilePath);
        InputStream in = null;
        FileOutputStream out = null;
        try {
            in = context.getAssets().open(fileName);
            out = new FileOutputStream(file);
            boolean e1 = true;
            byte[] buf = new byte[1024];

            int e11;
            while ((e11 = in.read(buf)) != -1) {
                out.write(buf, 0, e11);
            }

            out.flush();
            //判断之前的文件是否存在，存在直接删除
            File of = new File(to + "/" + fileName);
            if (of.exists()){
                of.delete();
            }
            return file.renameTo(new File(to + "/" + fileName));
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException var19) {
                    var19.printStackTrace();
                }
            }

            if (out != null) {
                try {
                    out.close();
                } catch (IOException var18) {
                    var18.printStackTrace();
                }
            }
        }
    }


    /**
     * 下载插件 并且重命名
     * @param context
     * @param urlStr url
     * @param fileName 文件名
     * @param handler 异步抛出消息
     * @return
     */
    private static boolean downLoadFile(Context context, String urlStr, String fileName, Handler handler)
    {
        if (context == null || fileName == null) return false;
        String to = context.getApplicationContext().getFilesDir()
                .getAbsolutePath() + PATH;
        //检查文件夹是否存在
        File fileDirs = new File(to);
        if (!fileDirs.exists()) {
            //文件夹不存在，创建
            fileDirs.mkdirs();
        }

        OutputStream output=null;
        String tmpFilePath = to + "/" + fileName.split("\\.")[0]+".tmp";
        File file = new File(tmpFilePath);
        try {

            URL url=new URL(urlStr);
            HttpURLConnection conn=(HttpURLConnection)url.openConnection();
            conn.setRequestProperty("Accept-Encoding", "identity");
            //文件覆盖
            InputStream input=conn.getInputStream();

            if(handler!= null)
            {
                Message msg = new Message();
                msg.obj = conn.getContentLength()+"";//总长度
                msg.what = 0;//开始下载
                handler.sendMessage(msg);
            }

            output=new FileOutputStream(file);
            byte[] buf = new byte[1024];
            int e11;
            while ((e11 = input.read(buf)) != -1) {
                output.write(buf, 0, e11);
                if(handler!= null)
                {
                    Message msg = new Message();
                    msg.arg1 = e11;
                    msg.what = 1;//进度
                    handler.sendMessage(msg);
                }
            }
            output.flush();

            if(handler!= null)
            {
                Message msg = new Message();
                msg.what = 2;//完成，正在重命名
                handler.sendMessage(msg);
            }
            File of = new File(to + "/" + fileName);
            if (of.exists()){
                of.delete();
            }
            //文件重命名
            return file.renameTo(new File(to + "/" + fileName));
        }catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }
        finally {
            try {
                output.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 下载插件 并且重命名
     * @param context
     * @param urlStr url
     * @param fileName 文件名
     * @param linstener 回调
     * @return
     */
    private static boolean downLoadFile_V2(Context context, String urlStr, String fileName, PluginManagerLinstener linstener)
    {
        if (context == null || fileName == null) return false;
        String to = context.getApplicationContext().getFilesDir()
                .getAbsolutePath() + PATH;
        //检查文件夹是否存在
        File fileDirs = new File(to);
        if (!fileDirs.exists()) {
            //文件夹不存在，创建
            fileDirs.mkdirs();
        }

        OutputStream output=null;
        String tmpFilePath = to + "/" + fileName.split("\\.")[0]+".tmp";
        File file = new File(tmpFilePath);
        try {

            URL url=new URL(urlStr);
            HttpURLConnection conn=(HttpURLConnection)url.openConnection();
            conn.setRequestProperty("Accept-Encoding", "identity");
            //文件覆盖
            InputStream input=conn.getInputStream();

            if(linstener!= null)
            {
                linstener.onAllLength(conn.getContentLength());
            }

            output=new FileOutputStream(file);
            byte[] buf = new byte[1024];
            int e11;
            while ((e11 = input.read(buf)) != -1) {
                output.write(buf, 0, e11);
                if(linstener!= null)
                {
                    linstener.onProgress(e11);
                }
            }
            output.flush();

            if(linstener!= null)
            {
                linstener.onDownLoadFinish();
            }
            File of = new File(to + "/" + fileName);
            if (of.exists()){
                of.delete();
            }
            //文件重命名
            return file.renameTo(new File(to + "/" + fileName));
        }catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }
        finally {
            try {
                output.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 设置版本号
     * @param context
     * @param key
     * @param value
     */
    private static void setVersion(Context context,String key ,String value)
    {
        SharedPreferences sp = context.getSharedPreferences("Plugin_Version", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor= sp.edit();
        editor.putString(key,value);
        editor.commit();
    }

    /**
     * 获取版本
     * @param context
     * @param key
     * @return
     */
    private static String getVersion(Context context,String key)
    {
        SharedPreferences sp = context.getSharedPreferences("Plugin_Version", Context.MODE_PRIVATE);
        return sp == null ? "-1" : sp.getString(key, "-1");
    }

    /**
     * 获取plugin当前服务器版本
     *
     * @param pluginFile
     * @return
     */
    private static Version getPluginVersion(PluginFile pluginFile) {

        Version result = null;
        String json = "";
        HttpURLConnection urlConnection = null;
        InputStreamReader in = null;
        try {
            urlConnection = (HttpURLConnection) new URL(pluginFile.getVersionUrl).openConnection();
            in = new InputStreamReader(urlConnection.getInputStream());
            BufferedReader bufferedReader = new BufferedReader(in);
            String readLine = null;
            while ((readLine = bufferedReader.readLine()) != null) {
                json += readLine;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                in.close();
                urlConnection.disconnect();
            } catch (Exception e1) {
                e1.printStackTrace();
            }
            try {

                if (json != null)
                {
                    Gson gson = new Gson();
                    result = gson.fromJson(json,Version.class);
                }
            }
            catch (Exception e2)
            {
                e2.printStackTrace();
            }

            return result;
        }

    }

    class Version{
        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }

        public String getDownloadAddr() {
            return downloadAddr;
        }

        public void setDownloadAddr(String downloadAddr) {
            this.downloadAddr = downloadAddr;
        }

        public String getAppCode() {
            return appCode;
        }

        public void setAppCode(String appCode) {
            this.appCode = appCode;
        }

        public String getUpdateFlag() {
            return updateFlag;
        }

        public void setUpdateFlag(String updateFlag) {
            this.updateFlag = updateFlag;
        }

        private String version;
        private String downloadAddr;
        private String appCode;
        private String updateFlag;

    }

//    public static void main(String[]args) throws Exception{
//
//        PluginFile pluginFile = new PluginFile();
//        pluginFile.getVersionUrl = "http://tumspre.cnsuning.com/tums-web/upgrade/queryVersion.action?devicetype=RF0";
//        PluginTools.getPluginVersion(pluginFile);
//    }
}
