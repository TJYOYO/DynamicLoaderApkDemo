package com.ryg.dynamicload.internal;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

/**
 * Created by 15032065 on 17/3/6.
 * 插件独立运行支持跳转其它moduleactivity
 */

public class PluginBridge implements Bridge {

    @Override
    public void startHostActivity(Context context, String className, Bundle bundle){
        try {
            Intent intent = new Intent();
            intent.setClass(context, Class.forName(className));
            if (bundle != null)
                intent.putExtra("Bundle",bundle);
            context.startActivity(intent);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void startHostActivity(Context context, String className) {
        startHostActivity(context,className,null);
    }

    @Override
    public void startHostActivityForResult(Context context, String className, Bundle bundle,int requestCode)
    {
        try {
            Intent intent = new Intent();
            intent.setClass(context, Class.forName(className));
            if (bundle != null)
                intent.putExtra("Bundle",bundle);
            if (context instanceof Activity)
            {
                ((Activity)(context)).startActivityForResult(intent,requestCode);
            }
            else
            {
                context.startActivity(intent);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void startHostActivityForResult(Context context, String className, int requestCode) {

        startHostActivityForResult(context,className,null,requestCode);
    }
}
