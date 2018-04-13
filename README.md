
导读：学习了任玉刚的动态加载框架技术，进行插件化的开发，实现host项目+plugin项目的独立开发，和动态加载，这里通过demo详细梳理一下流程

#### 一：介绍代码结构
DynamicLoaderApkdemo中包括了HostApp,PluginApp ，先介绍一下他们：

#### 1：HostApp
com.ryg.dynamicload包中的文件是动态插件框架提供的,其中包括了DL相关的代理类。
![image.png](https://upload-images.jianshu.io/upload_images/909565-166c39f3730b816d.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)
com.ryg.sample包中的MainActivity是一个启动页面如下：

![image.png](https://upload-images.jianshu.io/upload_images/909565-d8452a09a21ace48.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)


#### 2：PluginApp

###### 插件需要加入动态加载库
其中libs中加入动态加载库dl-apk.jar包，它只参与编译，不打包到apk中，这个包就是host中的com.ryg.dynamic下面的代码打包而来!
```
dependencies {
//    compile fileTree(dir: 'libs', include: ['*.jar'])
    compile 'com.android.support:appcompat-v7:24.2.1'
    compile 'com.google.code.gson:gson:2.8.0'
    provided files('libs/dl-lib.jar')
}
```
![image.png](https://upload-images.jianshu.io/upload_images/909565-0baa40a17e1a6d3e.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)


###### 页面继承动态库页面
com.practise.pluginapp.MainActivity，启动页面，**需要继承了动态加载库提供的DLBasePluginFragmentActivity类（当然可以是DLBasePluginActivity，或者对于service等）**，方便实现host加载时代理该类Activity的责任和生命周期。




#### 二：动态加载分析流程
###### 1：加载插件PluginApp.apk到内存中的过程
HostApp->MainActivity->loadPlugin, 加载手机sd卡中的PluginApp.apk, 其中PluginApp.apk是PluginApp项目打包而来
```
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
```
>DLPluginManager.getInstance(this).loadApk(apkAllPath);

上面的这个逻辑是主要的加载apk到内存的过程

```
 public DLPluginPackage loadApk(String dexPath) {
        // when loadApk is called by host apk, we assume that plugin is invoked
        // by host.
        return loadApk(dexPath, true);
    }

    /**
     * @param dexPath
     *            plugin path
     * @param hasSoLib
     *            whether exist so lib in plugin
     * @return
     */
    public DLPluginPackage loadApk(final String dexPath, boolean hasSoLib) {
        mFrom = DLConstants.FROM_EXTERNAL;

        PackageInfo packageInfo = mContext.getPackageManager().getPackageArchiveInfo(dexPath,
                PackageManager.GET_ACTIVITIES | PackageManager.GET_SERVICES);
        if (packageInfo == null) {
            return null;
        }
        //加载插件apk，获取到apk的资源
        DLPluginPackage pluginPackage = preparePluginEnv(packageInfo, dexPath);
        if (hasSoLib) {
            copySoLib(dexPath);
        }

        return pluginPackage;
    }
```
loadApk方法中，主要看
>DLPluginPackage pluginPackage = preparePluginEnv(packageInfo, dexPath);
```
private DLPluginPackage preparePluginEnv(PackageInfo packageInfo, String dexPath) {

        DLPluginPackage pluginPackage = mPackagesHolder.get(packageInfo.packageName);
        if (pluginPackage != null) {
            return pluginPackage;
        }
        //下面的3个主要方法，完成了类加载，和资源的获取！！
        DexClassLoader dexClassLoader = createDexClassLoader(dexPath);
        AssetManager assetManager = createAssetManager(dexPath);
        Resources resources = createResources(assetManager);
        // create pluginPackage
        pluginPackage = new DLPluginPackage(dexClassLoader, resources, packageInfo);
        mPackagesHolder.put(packageInfo.packageName, pluginPackage);
        return pluginPackage;
    }
```
**其中准备好了DexClassLoader的类加载，并加载完成插件apk，还有插件apk的资源获取到了！**

**mPackagesHolders是一个Map文件，存储了对于插件apk的信息（类加载器，资源等）, 方便了下一步启动插件apk中的页面需要的资源信息等**

```
private DexClassLoader createDexClassLoader(String dexPath) {
        File dexOutputDir = mContext.getDir("dex", Context.MODE_PRIVATE);
        dexOutputPath = dexOutputDir.getAbsolutePath();
        DexClassLoader loader = new DexClassLoader(dexPath, dexOutputPath, mNativeLibDir, mContext.getClassLoader());
        return loader;
    }
```
**android提供的类加载器DexClassLoader，传入了apk的路径，指定class.dex的路径，本地方法库，父类加载器等，完成了apk中class.dex文件的加载到内存，到这里插件Apk的动态加载就完成了！**

#### 2：启动插件页面，实现插件的功能

```
tv_start_plugin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                Toast.makeText(MainActivity.this, "开始启动插件页面....",Toast.LENGTH_SHORT).show();
                startPluginActivity(MainActivity.this, new DLIntent("com.practise.pluginapp"
                        , "com.practise.pluginapp.TestActivity"));
            }
        });
```

```
//传人的DLIntent中包括了需要启动插件的包名和页面
private void startPluginActivity(Context context, DLIntent intent) {
        DLPluginManager dlPluginManager = DLPluginManager.getInstance(context);
        if (!dlPluginManager.isHostPackageSet()){
            //当前host的包名
            dlPluginManager.setHostPackageName("com.ryg");
        }
        dlPluginManager.startPluginActivity(this, intent);
    }
```
```
@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    public int startPluginActivityForResult(Context context, DLIntent dlIntent, int requestCode) {
        //如果已经执行了动态加载apk，mfrom为1跳过if
        if (mFrom == DLConstants.FROM_INTERNAL) {
            if (isHostPackageSet() && dlIntent.getPluginPackage() != null
                    && !hostPackageName.equals(dlIntent.getPluginPackage())){
                return DLPluginManager.START_RESULT_NO_PKG;
            }
            dlIntent.setClassName(context, dlIntent.getPluginClass());
            performStartActivityForResult(context, dlIntent, requestCode);
            return DLPluginManager.START_RESULT_SUCCESS;
        }
        //得到插件的包名
        String packageName = dlIntent.getPluginPackage();
        if (TextUtils.isEmpty(packageName)) {
            throw new NullPointerException("disallow null packageName.");
        }
        //这里看到mPackagesHolder这个map获取之前保存的信息
        DLPluginPackage pluginPackage = mPackagesHolder.get(packageName);
        if (pluginPackage == null) {
            return START_RESULT_NO_PKG;
        }

        final String className = getPluginActivityFullPath(dlIntent, pluginPackage);
        //关键点来了：通过DexClassLoader类加载器将插件页面（已经在内存中的）生成对于的class对象
        Class<?> clazz = loadPluginClass(pluginPackage.classLoader, className);
        if (clazz == null) {
            return START_RESULT_NO_CLASS;
        }

        // get the proxy activity class, the proxy activity will launch the
        // plugin activity.
       //根据生成的对象，判断它的代理页面，就是我们插件页面继承的DLBasePluginFragmentActivity，这样就实现了启动代理页面，然后再调用生成对象中的方法，代理了插件页面的生命周期
        Class<? extends Activity> activityClass = getProxyActivityClass(clazz);
        if (activityClass == null) {
            return START_RESULT_TYPE_ERROR;
        }

        // put extra data
        dlIntent.putExtra(DLConstants.EXTRA_CLASS, className);
        dlIntent.putExtra(DLConstants.EXTRA_PACKAGE, packageName);
        dlIntent.setClass(mContext, activityClass);
        performStartActivityForResult(context, dlIntent, requestCode);
        return START_RESULT_SUCCESS;
    }
```
下面的方法是DexClassLoader来加载生成对象，不太明白类加载的可以看看：[虚拟机的类加载器理解和实践](https://www.jianshu.com/p/021e0d428209)
```
private Class<?> loadPluginClass(ClassLoader classLoader, String className) {
        Class<?> clazz = null;
        try {
            clazz = Class.forName(className, true, classLoader);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        return clazz;
    }

```

判断class对象是继承的哪个页面
```
private Class<? extends Activity> getProxyActivityClass(Class<?> clazz) {
        Class<? extends Activity> activityClass = null;
        if (DLBasePluginActivity.class.isAssignableFrom(clazz)) {
            activityClass = DLProxyActivity.class;
        } else if (DLBasePluginFragmentActivity.class.isAssignableFrom(clazz)) {
            activityClass = DLProxyFragmentActivity.class;
        }

        return activityClass;
    }
```
这样根据生成的对象，判断它的代理页面，就是我们插件页面继承的DLBasePluginFragmentActivity，这样就实现了启动代理页面，然后再调用生成对象中的方法，代理了插件页面的生命周期， 然后就启动了插件页面，如下:
![image.png](https://upload-images.jianshu.io/upload_images/909565-eaf0a27ded988a64.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

需要看动态加载插件开发的参考：

[https://github.com/singwhatiwanna/dynamic-load-apk](https://github.com/singwhatiwanna/dynamic-load-apk)  开源代码dlapk-lib

[http://blog.csdn.net/singwhatiwanna/article/details/39937639](http://blog.csdn.net/singwhatiwanna/article/details/39937639)  任玉刚的分析

[http://blog.csdn.net/fanpeihua123/article/details/51364521](http://blog.csdn.net/fanpeihua123/article/details/51364521)  范陪华的分析  ,  很棒, 重点看 ! !  

[http://blog.csdn.net/u012124438/article/details/53241755](http://blog.csdn.net/u012124438/article/details/53241755)  在范陪华文章，加入了更多的代码解读

[http://blog.csdn.net/u012124438/article/details/53242838](http://blog.csdn.net/u012124438/article/details/53242838) 在范陪华文章，加入了更多的代码解读























