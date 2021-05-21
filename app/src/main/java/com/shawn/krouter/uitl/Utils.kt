@file:Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")

package com.shawn.krouter.uitl

import android.app.Activity
import android.util.Log
import android.view.View
import androidx.annotation.IdRes
import java.lang.reflect.Proxy

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
annotation class InjectClick(@IdRes val ids: IntArray)

fun Activity.injectClicks() {
    javaClass.methods.asSequence().filter {
        it.isAnnotationPresent(InjectClick::class.java)
    }.forEach flag@{
        it.isAccessible = true
        if (it.parameterTypes.isEmpty() || it.parameterTypes[0] != View::class.java) {
            Log.e("suihw", "${it.name} : method parameter error");
            return@flag
        }
        it.getAnnotation(InjectClick::class.java).ids.forEach { id ->
            findViewById<View>(id).apply {
                val clickProxy = Proxy.newProxyInstance(
                    javaClass.classLoader, arrayOf(View.OnClickListener::class.java)
                ) { _, _, _ ->
                    Log.d("suihw", "执行方法前插入")
                    it.invoke(this@injectClicks, this)
                    Log.d("suihw", "执行方法后插入")
                } as View.OnClickListener
                setOnClickListener(clickProxy)
            }
        }
    }
}

fun MyLogD(tag: String, message: String) {
    Log.d(tag, message)
}
