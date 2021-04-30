package com.shawn.krouter.uitl;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;

import androidx.annotation.NonNull;

import java.lang.reflect.Field;

public class HookCallBack implements Handler.Callback {

    private static final String TAG = HookCallBack.class.getSimpleName();
    private Handler mHandler;

    public HookCallBack(Handler handler){
        this.mHandler = handler;
    }

    @Override
    public boolean handleMessage(@NonNull Message msg) {

        if (msg.what == 100){
            handleHookMsg(msg);
        }

        mHandler.handleMessage(msg);

        return true;
    }

    private void handleHookMsg(Message msg) {
        Object object = msg.obj;
        try {
            Field intent = object.getClass().getDeclaredField("intent");

            //这时候拿出来之前存进来真正的intent

            intent.setAccessible(true);

            Intent proxyIntent = (Intent) intent.get(object);
            Intent realIntent = proxyIntent.getParcelableExtra("realObj");

            proxyIntent.setComponent(realIntent.getComponent());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
