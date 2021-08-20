package com.heng.record.video.view.opengl

import android.opengl.EGLSurface
import android.opengl.GLES20
import android.os.SystemClock
import android.view.Surface
import com.heng.record.video.view.opengl.drawer.IDrawer
import com.heng.record.video.view.opengl.egl.EGLCore


private const val TAG = "EGLRenderPro"

class EGLRenderPro {
    class SurfaceWrapper(
        val surface: Surface,
        val width: Int,
        val height: Int,
        var eglSurface: EGLSurface? = null,
        var isRemove: Boolean = false
    )

    @Volatile
    var isStart = false
        set(value) {
            field = value
            startTime = if (field) {
                SystemClock.elapsedRealtimeNanos()
            } else {
                0L
            }
        }

    @Volatile
    private var startTime = 0L

    companion object {
        const val TEXTURE_ID = 5
    }

    private val mEGLContextClientVersion = 2
    private var mGLThread = GLThread().apply { start() }
    private val surfaceWrapList = mutableListOf<SurfaceWrapper>()


    fun addSurface(surface: Surface, width: Int, height: Int) {
        surfaceWrapList.add(SurfaceWrapper(surface, width, height))
        mGLThread.surfaceChange()
    }

    fun removeSurface(surface: Surface) {
        val surfaceWrapper = surfaceWrapList.find {
            it.surface == surface
        } ?: return
        surfaceWrapper.isRemove = true
        mGLThread.surfaceChange()
    }

    inner class GLThread : Thread() {

        private val eglCore = EGLCore()
        private val mWaitLock = Object()

        @Volatile
        private var curState = RenderState.NO_SURFACE


        //是否已经新建过EGL上下文，用于判断是否需要生产新的纹理ID
        private var mNeverCreateEglContext = true

        override fun run() {
            val name = name
            setName("GLThread:+${id}")
            if (surfaceWrapList.isNullOrEmpty()) holdOn()
            eglCore.init(mEGLContextClientVersion)
            var runTime: Long
            loop@ while (true) {
                runTime = SystemClock.elapsedRealtime()
                when (curState) {
                    RenderState.SURFACE_CHANGE -> {
                        filterSurface()
                        surfaceCreated()
                        curState = RenderState.RENDERING
                    }

                    RenderState.RENDERING -> {
                        filterSurface()
                        surfaceWrapList.forEach {
                            it.eglSurface?.apply {
                                eglCore.eglMakeCurrent(this)
                                onSurfaceChanged(
                                    it.width,
                                    it.height
                                )
                                onDrawFrame()
                                if (isStart) {
                                    eglCore.setPresentationTime(
                                        this,
                                        SystemClock.elapsedRealtimeNanos() - startTime
                                    )
                                }
                                eglCore.swapBuffers(this)
                            }
                        }

                    }
                    RenderState.STOP -> {
                        surfaceWrapList.forEach {
                            it.eglSurface?.apply {
                                eglCore.destroySurface(this)
                            }
                            it.surface.release()
                        }
                        curState = RenderState.NO_SURFACE
                        eglCore.release()
                        break@loop
                    }
                    else -> holdOn()
                }
                sleep((SystemClock.elapsedRealtime() + 16 - runTime).coerceAtLeast(0))
            }
            setName(name)
        }

        private fun filterSurface() {
            surfaceWrapList.forEach {
                if (it.eglSurface == null && !it.isRemove) {
                    it.eglSurface = eglCore.createWindowSurface(it.surface)
                } else if (it.isRemove && it.eglSurface != null) {
                    eglCore.destroySurface(it.eglSurface!!)
                    it.surface.release()
                }
            }
            surfaceWrapList.removeAll { it.isRemove }
        }

        private fun surfaceCreated() {
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

        fun surfaceChange() {
            curState = RenderState.SURFACE_CHANGE
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
//        val createTextureIds = OpenGLTools.createTextureIds(mDrawers.size)
        for (i in 0 until mDrawers.size) {
            mDrawers[i].setTextureID(TEXTURE_ID)
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

    fun removeDrawer(drawer: IDrawer) {
        mDrawers.remove(drawer)
    }

    fun stop() {
        mGLThread.onStop()
    }

    /**
     * 渲染状态
     */
    enum class RenderState {
        NO_SURFACE, //没有有效的surface
        SURFACE_CHANGE, //surface发生变化
        RENDERING, //初始化完毕，可以开始渲染
        STOP //停止绘制
    }
}