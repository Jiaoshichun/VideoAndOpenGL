package com.heng.ku.jnitest.opengl

import android.opengl.EGL14
import android.opengl.EGLSurface
import android.opengl.GLES20
import android.util.Log
import android.view.Surface
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import com.heng.ku.jnitest.opengl.drawer.IDrawer
import com.heng.ku.jnitest.opengl.egl.EGLCore


class EGLRender : SurfaceHolder.Callback {

    override fun surfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {
        mGLThread.surfaceChanged(width, height)
    }

    override fun surfaceDestroyed(holder: SurfaceHolder?) {
        mGLThread.surfaceDestroyed()
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        mSurface = holder.surface
        mGLThread.surfaceCreated()
    }

    private val mEGLContextClientVersion = 2
    private var mGLThread = GLThread().apply { start() }
    private var mSurface: Surface? = null

    fun setSurface(surface: SurfaceView) {
        surface.holder.addCallback(this)

        surface.addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
            override fun onViewDetachedFromWindow(v: View?) {
                mGLThread.onStop()
            }

            override fun onViewAttachedToWindow(v: View?) {
            }
        })
    }

    fun setSurface(surface: Surface, width: Int, height: Int) {
        if (mSurface != null) {
            mGLThread.mHaveBindEGLContext = false
            mSurface = surface
            mGLThread.surfaceChanged(width, height)
        }else{
            mSurface = surface
            mGLThread.surfaceCreated()
            mGLThread.surfaceChanged(width, height)
        }
    }


    inner class GLThread : Thread() {

        private val eglCore = EGLCore()
        private val mWaitLock = Object()
        @Volatile
        private var curState = RenderState.NO_SURFACE
        private var mWidth = 0
        private var mHeight = 0
        private var eglSurface: EGLSurface? = null

        // 是否绑定了EGLSurface
        var mHaveBindEGLContext = false
        //是否已经新建过EGL上下文，用于判断是否需要生产新的纹理ID
        private var mNeverCreateEglContext = true

        override fun run() {
            val name = name
            setName("GLThread:+${id}")
            if (mSurface == null) holdOn()
            eglCore.init(mEGLContextClientVersion)
            loop@ while (true) {
                when (curState) {
                    RenderState.FRESH_SURFACE -> {
                        bindContext()
                    }
                    RenderState.SURFACE_CHANGE -> {
                        bindContext()
                        onSurfaceChanged(mWidth, mHeight)
                        curState = RenderState.RENDERING
                    }

                    RenderState.RENDERING -> {
                        onDrawFrame()
                        eglCore.swapBuffers(eglSurface!!)
                    }
                    RenderState.SURFACE_DESTROY -> {
                        eglSurface?.let {
                            eglCore.destroySurface(it)
                            mHaveBindEGLContext = false
                        }
                        curState = RenderState.NO_SURFACE
                    }
                    RenderState.STOP -> {
                        eglCore.release()
                        break@loop
                    }
                    else -> holdOn()
                }
                sleep(16)
            }
            setName(name)
        }

        private fun bindContext() {
            if (!mHaveBindEGLContext) {
                mHaveBindEGLContext = true
                if (eglSurface != null) {
                    eglCore.destroySurface(eglSurface!!)
                }
                eglSurface = eglCore.createWindowSurface(mSurface!!)
                eglCore.eglMakeCurrent(eglSurface!!)
            }
            if (mNeverCreateEglContext) {
                mNeverCreateEglContext = false
                onSurfaceCreated()
            }
        }

        private fun holdOn() {
            synchronized(mWaitLock) {
                mWaitLock.wait()
            }
        }

        private fun notifyGo() {
            synchronized(mWaitLock) {
                mWaitLock.notify()
            }
        }

        fun surfaceChanged(width: Int, height: Int) {
            mWidth = width
            mHeight = height
            curState = RenderState.SURFACE_CHANGE
            notifyGo()
        }

        fun surfaceDestroyed() {
            curState = RenderState.SURFACE_DESTROY
            notifyGo()
        }

        fun surfaceCreated() {
            curState = RenderState.FRESH_SURFACE
            notifyGo()
        }


        fun render() {
            notifyGo()
        }

        fun onStop() {
            curState = RenderState.STOP
        }
    }

    private val mDrawers = mutableListOf<IDrawer>()


    private fun onSurfaceCreated() {
        //实现清屏效果
        GLES20.glClearColor(0f, 0f, 0f, 0f)
        //开启深度测试
        GLES20.glEnable(GLES20.GL_DEPTH_TEST)
        val createTextureIds = OpenGLTools.createTextureIds(mDrawers.size)
        for (i in 0 until mDrawers.size) {
            mDrawers[i].setTextureID(createTextureIds[i])
        }

    }


    private fun onSurfaceChanged(width: Int, height: Int) {
        //设置绘制区域
        GLES20.glViewport(0, 0, width, height)
        mDrawers.forEach {
            it.setWordWh(width, height)
        }
    }


    private fun onDrawFrame() {
        //清除深度测试缓存
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)
        mDrawers.forEach {
            it.draw()
        }
    }

    fun addDrawer(drawer: IDrawer) {
        mDrawers.add(drawer)
    }

    fun stop() {
        mGLThread.onStop()
    }

    /**
     * 渲染状态
     */
    enum class RenderState {
        NO_SURFACE, //没有有效的surface
        FRESH_SURFACE, //持有一个未初始化的新的surface
        SURFACE_CHANGE, // surface尺寸变化
        RENDERING, //初始化完毕，可以开始渲染
        SURFACE_DESTROY, //surface销毁
        STOP //停止绘制
    }
}