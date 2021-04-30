package com.shawn.krouter.uitl;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.shawn.krouter.ProxyActivity;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class ActivityManagerDelegate implements InvocationHandler {

    private static final String TAG = "ActivityManagerDelegate";
    private final Object mObject;
    private final Context mContext;

    public ActivityManagerDelegate(Object mObject, Context mContext) {
        this.mObject = mObject;
        this.mContext = mContext;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {


        if (method.getName().equals("startActivity")) {
            //拦截方法
            Log.e(TAG, "yo, i got you");

            Intent intent = null;
            for (int i = 0; i < args.length; i++) {
                if (args[i] instanceof Intent) {
                    intent = (Intent) args[i];
                    //找到intent参数
                    Intent mIntent = new Intent();
                    ComponentName componentName = new ComponentName(mContext, ProxyActivity.class);
                    // 将真正的intent带上，后续替换
                    mIntent.setComponent(componentName);
                    mIntent.putExtra("realObj", intent);
                    //修改为已注册的Activity的intent，先让AMS检查通过
                    args[i] = mIntent;
                }
            }

        }
        return method.invoke(mObject, args);
    }
}
