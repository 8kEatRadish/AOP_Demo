package com.shawn.krouter.layout

import android.content.Context
import android.view.View
import android.view.ViewGroup

/**
 * 文件： BaseViewGroup.kt
 * 描述： 自定义基础viewGroup
 * 作者： Suihongwei 2021/3/29
 **/
abstract class BaseViewGroup(context: Context) : ViewGroup(context) {
    //自动测量
    protected fun View.autoMeasure() {
        measure(
            defaultWidthMeasureSpec(this@BaseViewGroup),
            defaultHeightMeasureSpec(this@BaseViewGroup)
        )
    }

    //绘制
    protected fun View.layout(x: Int, y: Int, fromRight: Boolean = false) {
        if (!fromRight) {
            layout(x, y, x + measuredWidth, y + measuredHeight)
        } else {
            layout(this@BaseViewGroup.measuredWidth - x - measuredWidth, y)
        }
    }

    //默认测量宽度
    private fun View.defaultWidthMeasureSpec(parent: ViewGroup): Int {
        return when (layoutParams.width) {
            ViewGroup.LayoutParams.MATCH_PARENT -> {
                parent.measuredWidth.toExactlyMeasureSpec()
            }
            ViewGroup.LayoutParams.WRAP_CONTENT -> {
                ViewGroup.LayoutParams.WRAP_CONTENT.toAtMostMeasureSpec()
            }
            0 -> {
                throw IllegalAccessException("Need special treatment for $this")
            }
            else -> {
                layoutParams.width.toExactlyMeasureSpec()
            }
        }
    }

    //默认测量高度
    private fun View.defaultHeightMeasureSpec(parent: ViewGroup): Int {
        return when (layoutParams.height) {
            ViewGroup.LayoutParams.MATCH_PARENT -> {
                parent.measuredHeight.toExactlyMeasureSpec()
            }
            ViewGroup.LayoutParams.WRAP_CONTENT -> {
                ViewGroup.LayoutParams.WRAP_CONTENT.toAtMostMeasureSpec()
            }
            0 -> {
                throw IllegalAccessException("Need special treatment for $this")
            }
            else -> {
                layoutParams.height.toExactlyMeasureSpec()
            }
        }
    }

    private fun Int.toExactlyMeasureSpec(): Int {
        return MeasureSpec.makeMeasureSpec(this, MeasureSpec.EXACTLY)
    }

    private fun Int.toAtMostMeasureSpec(): Int {
        return MeasureSpec.makeMeasureSpec(this, MeasureSpec.AT_MOST)
    }

    protected val Int.dp: Int get() = (this * resources.displayMetrics.density + 0.5f).toInt()

    protected class LayoutParams(width: Int, height: Int) : MarginLayoutParams(width, height)
}