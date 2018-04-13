package com.ryg.dynamicload.internal;

import android.content.Context;
import android.os.Bundle;

/**
 * Created by 15032065 on 17/3/5.
 */

public interface Bridge
{
    void startHostActivity(Context context, String className);
    void startHostActivity(Context context, String className, Bundle bundle);
    void startHostActivityForResult(Context context, String className, int requestCode);
    void startHostActivityForResult(Context context, String className, Bundle bundle, int requestCode);
}
