package com.heng.ku.jnitest

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
private const val TAG="ViewTestActivity"
class ViewTestActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_test)
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        Log.d(TAG,"dispatchTouchEvent")
        val touchEvent = super.dispatchTouchEvent(ev)
        Log.d(TAG,"dispatchTouchEvent->over")
        return touchEvent
    }
}
