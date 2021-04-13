package com.shawn.krouter.layout

import android.content.Context
import android.view.ViewGroup
import android.widget.Button

class SecondActivityLayout constructor(context: Context) : BaseViewGroup(context) {

    val button = Button(context).apply {
        layoutParams = LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            100.dp
        )
        text = "我是第二个activity"
        this@SecondActivityLayout.addView(this)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        button.autoMeasure()
        setMeasuredDimension(widthMeasureSpec, heightMeasureSpec)
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        button.layout(0, (measuredHeight - button.measuredHeight) / 2)
    }
}