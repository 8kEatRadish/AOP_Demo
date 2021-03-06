package com.shawn.krouter.layout

import android.content.Context
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.Button
import android.widget.TextView
import com.shawn.krouter.R

/**
 * 文件： SecondFragmentLayout.kt
 * 描述： 第二个fragment布局
 * 作者： Suihongwei 2021/4/13
 **/
class SecondFragmentLayout(context: Context) : BaseViewGroup(context) {

    val text = TextView(context).apply {
        text = resources.getString(R.string.hello_second_fragment)
        layoutParams = LayoutParams(WRAP_CONTENT, WRAP_CONTENT)
        addView(this)
    }

    val button = Button(context).apply {
        text = resources.getString(R.string.previous)
        layoutParams = LayoutParams(WRAP_CONTENT, WRAP_CONTENT)
        addView(this)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        text.autoMeasure()
        button.autoMeasure()
        setMeasuredDimension(widthMeasureSpec, heightMeasureSpec)
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        text.layout((measuredWidth - text.measuredWidth) / 2, measuredHeight / 2 - 32.dp)

        button.layout(
            (measuredWidth - button.measuredWidth) / 2,
            text.bottom + button.measuredHeight + 32.dp
        )
    }
}