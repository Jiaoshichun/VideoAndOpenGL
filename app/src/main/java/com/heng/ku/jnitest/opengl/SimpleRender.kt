package com.heng.ku.jnitest.opengl

import android.opengl.GLES20
import android.opengl.GLSurfaceView
import com.heng.ku.jnitest.opengl.drawer.IDrawer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class SimpleRender : GLSurfaceView.Renderer {
    private val mDrawers = mutableListOf<IDrawer>()


    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        //实现清屏效果
        GLES20.glClearColor(0f, 0f, 0f, 0f)
        //开启深度测试
        GLES20.glEnable(GLES20.GL_DEPTH_TEST)
        val createTextureIds = OpenGLTools.createTextureIds(mDrawers.size)
        for (i in 0 until mDrawers.size) {
            mDrawers[i].setTextureID(createTextureIds[i])
        }

    }


    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        //设置绘制区域
        GLES20.glViewport(0, 0, width, height)
        mDrawers.forEach {
            it.setWordWh(width, height)
        }
    }


    override fun onDrawFrame(gl: GL10?) {
        //清除深度测试缓存
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)
        mDrawers.forEach {
            it.draw()
        }
    }

    fun addDrawer(drawer: IDrawer) {
        mDrawers.add(drawer)
    }
}