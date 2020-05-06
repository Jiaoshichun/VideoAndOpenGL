package com.heng.ku.jnitest.opengl.drawer

import android.graphics.SurfaceTexture


interface IDrawer {
    fun draw()
    fun setTextureID(id: Int)
    fun release()
    fun getSurfaceTexture(cb: (SurfaceTexture) -> Unit) {}
    fun setVideoWh(width: Int, height: Int){}
    fun setWordWh(width: Int, height: Int)
    fun setRotationAngle(angle: Int){}
}