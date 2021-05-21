package com.shawn.krouter;

import android.util.Log;

import com.shawn.krouter.uitl.UtilsKt;

public class Test {

    String name = "testFun";
    public void TestFun(){

        UtilsKt.MyLogD("suihw","into " + new Exception().getStackTrace()[0].getMethodName());
        Log.d("suihw","lalala");
    }
}
