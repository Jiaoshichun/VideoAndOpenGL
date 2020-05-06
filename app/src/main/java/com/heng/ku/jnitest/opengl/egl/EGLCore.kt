package com.heng.ku.jnitest.opengl.egl


import android.graphics.SurfaceTexture
import android.opengl.*
import android.view.Surface

/**
EGLDisplay
EGL定义的一个抽象的系统显示类，用于操作设备窗口。
EGLConfig
EGL配置，如rgba位数
EGLSurface
渲染缓存，一块内存空间，所有要渲染到屏幕上的图像数据，都要先缓存在EGLSurface上。
EGLContext
OpenGL上下文，用于存储OpenGL的绘制状态信息、数据
 */
class EGLCore {
    private val EGL_RECORDABLE_ANDROID = 0x3142
    private val EGL_CONTEXT_CLIENT_VERSION = 0x3098
    private var mEglDisplay = EGL14.EGL_NO_DISPLAY
    //    private var mEgl: EGL14? = null
    var mEglConfig: EGLConfig? = null
        private set
    var mEglContext: EGLContext = EGL14.EGL_NO_CONTEXT
        private set

    /**
     * 初始化EGLDisplay
     */
    fun init(version: Int) {
        if (mEglDisplay !== EGL14.EGL_NO_DISPLAY) {
            throw RuntimeException("EGL already set up")
        }

        //创建EglDisplay
        mEglDisplay = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY)
        if (mEglDisplay === EGL14.EGL_NO_DISPLAY) {
            throw RuntimeException("eglGetDisplay failed：  " + GLUtils.getEGLErrorString(EGL14.eglGetError()))
        }
        //初始化EglDisplay
        val intArray = IntArray(2)
        if (!EGL14.eglInitialize(mEglDisplay, intArray, 0, intArray, 1)) {
            throw RuntimeException("eglInitialize failed：  " + GLUtils.getEGLErrorString(EGL14.eglGetError()))
        }
        if (mEglContext == EGL14.EGL_NO_CONTEXT) {
            mEglConfig = getConfig(version)
            mEglContext = EGL14.eglCreateContext(
                mEglDisplay,
                mEglConfig,
                EGL14.EGL_NO_CONTEXT,
                intArrayOf(EGL_CONTEXT_CLIENT_VERSION, version, EGL14.EGL_NONE),
                0
            )
        }
    }

    /**
     * 获取EGLConfig
     */
    private fun getConfig(version: Int): EGLConfig {
        val referableType =
            if (version == 2) EGL14.EGL_OPENGL_ES2_BIT else EGLExt.EGL_OPENGL_ES3_BIT_KHR

        val attribList = intArrayOf(
            EGL14.EGL_RED_SIZE,
            8,
            EGL14.EGL_GREEN_SIZE,
            8,
            EGL14.EGL_BLUE_SIZE,
            8,
            EGL14.EGL_ALPHA_SIZE,
            8,
            EGL14.EGL_DEPTH_SIZE,
            16,
            EGL14.EGL_STENCIL_SIZE,
            0,
            EGL_RECORDABLE_ANDROID,
            1,
            EGL14.EGL_RENDERABLE_TYPE,
            referableType,
            EGL14.EGL_NONE
        )
        val configs = arrayOfNulls<EGLConfig>(1)
        val numConfig = IntArray(1)
        if(!EGL14.eglChooseConfig(
            mEglDisplay,
            attribList,
            0,
            configs,
            0,
            configs.size,
            numConfig,
            0
        )) {
            throw RuntimeException("eglChooseConfig,failed:  " + GLUtils.getEGLErrorString(EGL14.eglGetError()))
        }
        return configs[0]!!
    }

    /**
     * 创建可显示的渲染缓存
     * @param surface 渲染窗口的surface
     */
    fun createWindowSurface(surface: Any): EGLSurface {
        if (surface !is Surface && surface !is SurfaceTexture) {
            throw RuntimeException("surface invalid")
        }
        return EGL14.eglCreateWindowSurface(mEglDisplay, mEglConfig, surface, null, 0)
            ?: throw RuntimeException("EGLSurface is null")
    }

    /**
     * 创建离屏渲染缓存
     */
    fun createPbufferSurface(): EGLSurface {
        val surfaceAttribs = intArrayOf(EGL14.EGL_WIDTH, 1, EGL14.EGL_HEIGHT, 1, EGL14.EGL_NONE)
        return EGL14.eglCreatePbufferSurface(mEglDisplay, mEglConfig, surfaceAttribs, 0)

    }

    /**
     * 将上下文和当前的渲染窗口相关联
     */
    fun eglMakeCurrent(eglSurface: EGLSurface) {
        if (!EGL14.eglMakeCurrent(mEglDisplay, eglSurface, eglSurface, mEglContext)) {
            throw RuntimeException("eglMakeCurrent fail  " + GLUtils.getEGLErrorString(EGL14.eglGetError()))
        }
    }

    /**
     * 设置当前帧的时间，单位：纳秒
     * 编码时，需要再调用swapBuffers前调用该方法
     */
    fun setPresentationTime(eglSurface: EGLSurface, nsecs: Long) {
        EGLExt.eglPresentationTimeANDROID(mEglDisplay, eglSurface, nsecs)
    }

    /**
     * 将缓存图像数据发送到设备进行显示
     */
    fun swapBuffers(eglSurface: EGLSurface): Boolean {
        return EGL14.eglSwapBuffers(mEglDisplay, eglSurface)
    }

    /**
     * 销毁渲染窗口
     */
    fun destroySurface(eglSurface: EGLSurface) {
        EGL14.eglMakeCurrent(
            mEglDisplay,
            EGL14.EGL_NO_SURFACE,
            EGL14.EGL_NO_SURFACE,
            EGL14.EGL_NO_CONTEXT
        )
        EGL14.eglDestroySurface(mEglDisplay, eglSurface)
    }

    /**
     * 是否资源
     */
    fun release() {
        if (mEglContext != EGL14.EGL_NO_CONTEXT) {
            EGL14.eglMakeCurrent(
                mEglDisplay,
                EGL14.EGL_NO_SURFACE,
                EGL14.EGL_NO_SURFACE,
                EGL14.EGL_NO_CONTEXT
            )
            EGL14.eglDestroyContext(mEglDisplay, mEglContext)
            EGL14.eglTerminate(mEglDisplay)
        }
        mEglContext = EGL14.EGL_NO_CONTEXT
        mEglConfig = null
        mEglDisplay = EGL14.EGL_NO_DISPLAY
    }
}