package com.heng.ku.jnitest.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.util.Log
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup

private const val TAG = "CustomView"

class CustomView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = -1
) : View(context, attrs, defStyleAttr) {
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


    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        Log.d(TAG, "onMeasure")
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        Log.d(TAG, "onMeasure->over")
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        Log.d(TAG, "onTouchEvent")
        return super.onTouchEvent(event)
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        Log.d(TAG, "onLayout")
        super.onLayout(changed, l, t, r, b)
        Log.d(TAG, "onLayout->over")
    }

    private val rect = Rect(0, 0, 50, 50)
    private val paint = Paint().apply {
        this.color = Color.RED
        this.isAntiAlias = true
    }

    override fun onDraw(canvas: Canvas?) {
        Log.d(TAG, "onDraw")
        super.onDraw(canvas)
        canvas?.drawRect(rect, paint)
        Log.d(TAG, "onDraw->over")
    }

}