//package com.heng.ku.jnitest.opengl.egl
//
//import android.content.Context
//import android.opengl.GLSurfaceView
//import android.util.AttributeSet
//import android.view.SurfaceHolder
//import android.view.SurfaceView
//import javax.microedition.khronos.egl.EGLSurface
//import javax.microedition.khronos.opengles.GL10
//
//open class EGLSurfaceView @JvmOverloads constructor(
//    context: Context,
//    attributeSet: AttributeSet? = null, style: Int = 0
//) : SurfaceView(context, attributeSet, style), SurfaceHolder.Callback {
//
//    init {
//        holder.addCallback(this)
//    }
//
//    private var mGLThread= GLThread().apply { start() }
//    private var mEGLContextClientVersion = 2
//    private var mRenderer: GLSurfaceView.Renderer? = null
//
//    /**
//     * 设置客户端版本
//     */
//    fun setEGLContextClientVersion(version: Int) {
//        mEGLContextClientVersion = version
//    }
//
//    /**
//     * 设置renderer
//     */
//    fun setRenderer(renderer: GLSurfaceView.Renderer) {
//        mRenderer = renderer
//    }
//
//    override fun surfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {
//        mGLThread.surfaceChanged(width, height)
//    }
//
//    override fun surfaceDestroyed(holder: SurfaceHolder?) {
//        mGLThread.surfaceDestroyed()
//    }
//
//    override fun surfaceCreated(holder: SurfaceHolder?) {
//        mGLThread.surfaceCreated()
//    }
//
//
//    override fun onDetachedFromWindow() {
//        super.onDetachedFromWindow()
//        mGLThread.onStop()
//    }
//
//
//    inner class GLThread : Thread() {
//
//        private val eglCore = EGLCore()
//        private val mWaitLock = Object()
//        private var curState = RenderState.NO_SURFACE
//        private var mWidth = 0
//        private var mHeight = 0
//        private var eglSurface: EGLSurface? = null
//
//        // 是否绑定了EGLSurface
//        private var mHaveBindEGLContext = false
//        //是否已经新建过EGL上下文，用于判断是否需要生产新的纹理ID
//        private var mNeverCreateEglContext = true
//        override fun run() {
//            val name = name
//            setName("GLThread:+${id}")
//            if(mRenderer==null)holdOn()
//            eglCore.init(mEGLContextClientVersion)
//            loop@ while (true) {
//
//                when (curState) {
//                    RenderState.FRESH_SURFACE -> {
//                        bindContext()
//                    }
//                    RenderState.SURFACE_CHANGE -> {
//                        bindContext()
//                        mRenderer?.onSurfaceChanged(eglCore.mEglContext.gl as GL10, mWidth, mHeight)
//                        curState = RenderState.RENDERING
//                    }
//
//                    RenderState.RENDERING -> {
//                        mRenderer?.onDrawFrame(eglCore.mEglContext.gl as GL10)
//                        eglCore.swapBuffers(eglSurface!!)
//                    }
//                    RenderState.SURFACE_DESTROY -> {
//                        eglSurface?.let {
//                            eglCore.destroySurface(it)
//                            mHaveBindEGLContext = false
//                        }
//                        curState=RenderState.NO_SURFACE
//                    }
//                    RenderState.STOP -> {
//                        eglCore.release()
//                        break@loop
//                    }
//                    else ->   holdOn()
//                }
//                sleep(16)
//            }
//            setName(name)
//        }
//
//        private fun bindContext() {
//            if (!mHaveBindEGLContext) {
//                mHaveBindEGLContext = true
//                eglSurface = eglCore.createWindowSurface(holder.surface)
//                eglCore.eglMakeCurrent(eglSurface!!)
//            }
//            if (mNeverCreateEglContext) {
//                mNeverCreateEglContext = false
//                mRenderer?.onSurfaceCreated(eglCore.mEglContext.gl as GL10, eglCore.mEglConfig)
//            }
//        }
//
//        private fun holdOn() {
//            synchronized(mWaitLock) {
//                mWaitLock.wait()
//            }
//        }
//
//        private fun notifyGo() {
//            synchronized(mWaitLock) {
//                mWaitLock.notify()
//            }
//        }
//
//        fun surfaceChanged(width: Int, height: Int) {
//            mWidth = width
//            mHeight = height
//            curState = RenderState.SURFACE_CHANGE
//            notifyGo()
//        }
//
//        fun surfaceDestroyed() {
//            curState = RenderState.SURFACE_DESTROY
//            notifyGo()
//        }
//
//        fun surfaceCreated() {
//            curState = RenderState.FRESH_SURFACE
//            notifyGo()
//        }
//
//        fun render() {
//            notifyGo()
//        }
//
//        fun onStop() {
//            curState=RenderState.STOP
//        }
//    }
//
//    /**
//     * 渲染状态
//     */
//    enum class RenderState {
//        NO_SURFACE, //没有有效的surface
//        FRESH_SURFACE, //持有一个未初始化的新的surface
//        SURFACE_CHANGE, // surface尺寸变化
//        UPDATE_SURFACE, // surface更新
//        RENDERING, //初始化完毕，可以开始渲染
//        SURFACE_DESTROY, //surface销毁
//        STOP //停止绘制
//    }
//}