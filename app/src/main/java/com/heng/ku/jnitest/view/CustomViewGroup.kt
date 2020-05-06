package com.heng.ku.jnitest.view

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.util.Log
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup

private const val TAG = "CustomViewGroup"

class CustomViewGroup  @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = -1
) : ViewGroup(context, attrs, defStyleAttr) {
    override fun dispatchDraw(canvas: Canvas?) {
        Log.d(TAG, "dispatchDraw")
        super.dispatchDraw(canvas)
        Log.d(TAG, "dispatchDraw->over")
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        Log.d(TAG, "dispatchTouchEvent")
        val event = super.dispatchTouchEvent(ev)
        Log.d(TAG, "dispatchTouchEvent->over")
        return event
    }

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        val onInterceptTouchEvent = super.onInterceptTouchEvent(ev)
        Log.d(TAG, "onInterceptTouchEvent:$onInterceptTouchEvent")
        return onInterceptTouchEvent
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        Log.d(TAG, "onMeasure")
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        for (i in 0 until childCount) {
            val child = getChildAt(i)
            if (child.visibility != View.GONE) {
                measureChild(child,widthMeasureSpec,heightMeasureSpec)
            }
        }
        Log.d(TAG, "onMeasure->over")
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        layoutChildren(l, t, r, b)
        Log.d(TAG, "onLayout")
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        Log.d(TAG, "onTouchEvent")
        return super.onTouchEvent(event)
    }
    override fun onDraw(canvas: Canvas?) {
        Log.d(TAG, "onDraw")
        super.onDraw(canvas)
        Log.d(TAG, "onDraw->over")
    }
    private fun layoutChildren(
        left: Int,
        top: Int,
        right: Int,
        bottom: Int
    ) {
        val count = childCount


        for (i in 0 until count) {
            val child = getChildAt(i)
            if (child.visibility != View.GONE) {
                val width = child.measuredWidth
                val height = child.measuredHeight

                child.layout(left, top, left + width, top + height)
            }
        }
    }
}