package com.heng.ku.jnitest.view

import android.content.Context
import android.opengl.GLSurfaceView
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.SurfaceView
import com.heng.ku.jnitest.opengl.drawer.VideoDrawer

class DragGLSurfaceView @JvmOverloads constructor(context: Context, attributes: AttributeSet? = null) :
    SurfaceView(context, attributes) {
    var videoDrawer: VideoDrawer? = null
    private var lastX = 0f
    private var lastY = 0f
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event?.action == MotionEvent.ACTION_DOWN) {
            lastX = event.x
            lastY = event.y
        } else if (event?.action == MotionEvent.ACTION_MOVE) {
            videoDrawer?.translate((event.x - lastX) / width, (event.y - lastY) / height)
            lastX = event.x
            lastY = event.y
        }

        return true
    }
}