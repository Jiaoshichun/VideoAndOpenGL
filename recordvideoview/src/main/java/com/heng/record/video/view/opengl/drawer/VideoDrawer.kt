package com.heng.record.video.view.opengl.drawer

import android.graphics.RectF
import android.graphics.SurfaceTexture
import android.opengl.GLES11Ext
import android.opengl.GLES20
import android.opengl.Matrix
import android.util.Log
import com.heng.record.video.view.opengl.OpenGLTools.loadShader
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

/**
 * 要实现混合效果  必须开启混合模式
GLES20.glEnable(GLES20.GL_BLEND)
GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA)

GLES20.glEnable(GLES20.GL_DEPTH_TEST) 时，会出现无法透明的问题。
 */
class VideoDrawer : IDrawer {


    private val TAG = "VideoDrawer"
    //顶点坐标
    private val mVertexCoors = floatArrayOf(
        -1f, -1f,
        1f, -1f,
        -1f, 1f,
        1f, 1f
    )
    //纹理坐标
    private val mTextureCoors = floatArrayOf(
        0f, 1f,
        1f, 1f,
        0f, 0f,
        1f, 0f
    )


    //由于 GL不能直接接受float数组  所以通过 buffer的方式传入
    private val mVertexBuffer: FloatBuffer
    private val mTextureBuffer: FloatBuffer

    private var mTextureId = -1
    //OpenGL es程序
    private var mProgram = -1

    //顶点着色器
    private var mVertexShader = -1
    //片元着色器
    private var mFragmentShader = -1

    private var mVertexPosHandler = -1
    private var mTexturePosHandler = -1
    private var mTextureHandler = -1
    private var mMatrixHandler = -1
    private var mAttrAlpha = -1
    private var mSurfaceTexture: SurfaceTexture? = null
    private var mCallBack: ((SurfaceTexture) -> Unit)? = null
    private var mVideoW = 0
    private var mVideoH = 0
    private var mWordW = 0
    private var mWordH = 0
    private var mResultMatrix: FloatArray? = null
    private var mRotateAngle = 0

    var alpha = 1f

    private var scaleX = 1f
    private var scaleY = 1f

    private var rect = RectF()

    init {
        //使用
        val aa = ByteBuffer.allocateDirect(mVertexCoors.size * 4)
        aa.order(ByteOrder.nativeOrder())
        mVertexBuffer = aa.asFloatBuffer()
        mVertexBuffer.put(mVertexCoors)
        mVertexBuffer.position(0)

        val bb = ByteBuffer.allocateDirect(mTextureCoors.size * 4)
        bb.order(ByteOrder.nativeOrder())
        mTextureBuffer = bb.asFloatBuffer()
        mTextureBuffer.put(mTextureCoors)
        mTextureBuffer.position(0)

    }

    override fun draw() {
        if (mTextureId == -1) return
        initMatrix()
        if (mResultMatrix == null) return
        createProgram()
        activeTexture()
        updateTexture()
        doDraw()
    }

    /**
     * 初始化矩阵 适配视频宽高比
     */
    private fun initMatrix() {
        if (mResultMatrix != null) return
        if (mWordW == 0 || mWordH == 0 || mVideoW == 0 || mVideoH == 0) return
        mResultMatrix = FloatArray(16)

        //设置相机位置
        val mViewMatrix = FloatArray(16)
        var realAngle = mRotateAngle % 360
        if (realAngle < 0) {
            realAngle += 360
        }
        val upLook = when (realAngle) {
            0 -> floatArrayOf(0f, 1f, 0f)
            90 -> floatArrayOf(-1f, 0f, 0f)
            180 -> floatArrayOf(0f, -1f, 0f)
            else -> floatArrayOf(1f, 0f, 0f)
        }
        // eyeX eyxY eyxZ 相机的位置  centerX centerY centerZ 原点的位置  upX upY upZ 相机顶部的朝向
        Matrix.setLookAtM(mViewMatrix, 0, 0f, 0f, 5f, 0f, 0f, 0f, upLook[0], upLook[1], upLook[2])

        //如果选择90/270度 则将视频的宽高进行颠倒
        var videoW = mVideoW
        var videoH = mVideoH
        if (realAngle == 90 || realAngle == 270) {
            videoW = mVideoH
            videoH = mVideoW
        }

        //根据正交投影  将视频按照比例缩放到 布局中
        val mMatrix = FloatArray(16)
        //世界宽高比
        val wordRatio = mWordW.toFloat() / mWordH
        //视频宽高比
        val videoRatio = videoW.toFloat() / videoH
        if (videoRatio > wordRatio) {//以宽为准，宽填满 高缩小
            val i = mWordH / (mWordW.toFloat() / videoW * videoH)
            Matrix.orthoM(mMatrix, 0, -1f, 1f, -i, i, 3f, 5f)
            rect.left = -1f
            rect.right = 1f
            rect.top = i
            rect.bottom = -i
        } else {//以高为准 高充满 宽缩小
            val i = mWordW / (mWordH.toFloat() / videoH * videoW)
            Matrix.orthoM(mMatrix, 0, -i, i, -1f, 1f, 3f, 5f)
            rect.left = -i
            rect.right = i
            rect.top = 1f
            rect.bottom = -1f

        }

        //组合 相机的Matrix 和缩放的Matrix
        Matrix.multiplyMM(mResultMatrix, 0, mMatrix, 0, mViewMatrix, 0)

        Matrix.scaleM(mResultMatrix, 0, scaleX, scaleY, 1f)
    }

    private fun doDraw() {
        //启用顶点的句柄
        GLES20.glEnableVertexAttribArray(mTexturePosHandler)
        GLES20.glEnableVertexAttribArray(mVertexPosHandler)
        //设置着色器参数， 第二个参数表示一个顶点包含的数据数量，这里为xy，所以为2
        GLES20.glVertexAttribPointer(mVertexPosHandler, 2, GLES20.GL_FLOAT, false, 0, mVertexBuffer)
        GLES20.glVertexAttribPointer(
            mTexturePosHandler,
            2,
            GLES20.GL_FLOAT,
            false,
            0,
            mTextureBuffer
        )
        //设置矩阵转换
        GLES20.glUniformMatrix4fv(mMatrixHandler, 1, false, mResultMatrix, 0)

        GLES20.glVertexAttrib1f(mAttrAlpha, alpha)

        //开始绘制
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)
    }

    /**
     * 更新纹理
     */
    private fun updateTexture() {
        mSurfaceTexture?.updateTexImage()
    }

    /**
     * 激活 纹理
     */
    private fun activeTexture() {

        //激活指定纹理单元
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        // 将纹理id与纹理单元绑定
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, mTextureId)
        //将激活的纹理单元传递到着色器里面
        GLES20.glUniform1i(mTextureHandler, 0)

        //设置颜色过滤模式
        GLES20.glTexParameterf(
            GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
            GLES20.GL_TEXTURE_MIN_FILTER,
            GLES20.GL_LINEAR.toFloat()
        )
        GLES20.glTexParameterf(
            GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
            GLES20.GL_TEXTURE_MAG_FILTER,
            GLES20.GL_LINEAR.toFloat()
        )
        //设置 环绕方式
        GLES20.glTexParameteri(
            GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
            GLES20.GL_TEXTURE_WRAP_S,
            GLES20.GL_CLAMP_TO_EDGE
        )
        GLES20.glTexParameteri(
            GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
            GLES20.GL_TEXTURE_WRAP_T,
            GLES20.GL_CLAMP_TO_EDGE
        )
    }

    private fun createProgram() {
        if (mProgram == -1) {
            //创建着色器
            mVertexShader = loadShader(GLES20.GL_VERTEX_SHADER, getVertexCode())
            mFragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, getFragmentCode())
            //创建openGL es程序
            mProgram = GLES20.glCreateProgram()
            //将OpenGL ES程序与 着色器进行绑定
            GLES20.glAttachShader(mProgram, mVertexShader)
            GLES20.glAttachShader(mProgram, mFragmentShader)
            //连接程序
            GLES20.glLinkProgram(mProgram)

            mVertexPosHandler = GLES20.glGetAttribLocation(mProgram, "aPosition")
            mAttrAlpha = GLES20.glGetAttribLocation(mProgram, "aAlpha")
            mTexturePosHandler = GLES20.glGetAttribLocation(mProgram, "aCoordinate")
            mTextureHandler = GLES20.glGetUniformLocation(mProgram, "uTexture")
            mMatrixHandler = GLES20.glGetUniformLocation(mProgram, "uMatrix")
        }
        GLES20.glUseProgram(mProgram)
    }

    private fun getFragmentCode(): String {
        return """
            #extension GL_OES_EGL_image_external : require
            precision mediump float;
            varying vec2 vCoordinate;
            varying float vAlpha;
            uniform samplerExternalOES uTexture;
            void main(){
                vec4 color=texture2D(uTexture,vCoordinate);
                // 对rgb做一个均值 即实现将添加黑白滤镜
//                float gray=(color.r+color.g+color.b)/3.0;
                gl_FragColor=vec4(color.r,color.g,color.b,vAlpha);
            }
        """.trimIndent()
    }

    private fun getVertexCode(): String {
        return """
            precision mediump float;
            attribute vec4 aPosition;
            attribute vec2 aCoordinate;
            varying vec2 vCoordinate;
            uniform mat4 uMatrix;
            varying float vAlpha;
            attribute float aAlpha;
            void main(){
                gl_Position=uMatrix*aPosition;
                vCoordinate=aCoordinate;
                vAlpha=aAlpha;
            }
        """.trimIndent()
    }

    override fun setTextureID(id: Int) {
        //禁用深度测试
        GLES20.glDisable(GLES20.GL_DEPTH_TEST)

        //开启混合，即半透明
        GLES20.glEnable(GLES20.GL_BLEND)
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA)

        mTextureId = id
        if (mSurfaceTexture == null) {
            mSurfaceTexture = SurfaceTexture(id)
            mCallBack?.invoke(mSurfaceTexture!!)
        }
    }

    override fun release() {

        GLES20.glDisable(GLES20.GL_BLEND)
        //禁用句柄
        GLES20.glDisableVertexAttribArray(mTexturePosHandler)
        GLES20.glDisableVertexAttribArray(mVertexPosHandler)

        //删除着色器
        GLES20.glDetachShader(mProgram, mFragmentShader)
        GLES20.glDetachShader(mProgram, mVertexShader)
        GLES20.glDeleteShader(mFragmentShader)
        GLES20.glDeleteShader(mVertexShader)

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0)
        GLES20.glDeleteTextures(1, intArrayOf(mTextureId), 0)
        //删除OpenGL ES程序
        GLES20.glDeleteProgram(mProgram)
    }


    override fun setVideoWh(width: Int, height: Int) {
        mVideoW = width
        mVideoH = height
        mResultMatrix = null
    }

    override fun setWordWh(width: Int, height: Int) {
        mWordW = width
        mWordH = height
        mResultMatrix = null
    }

    override fun getSurfaceTexture(cb: (SurfaceTexture) -> Unit) {
        mCallBack = cb
    }

    override fun setRotationAngle(angle: Int) {
        mRotateAngle = angle
        mResultMatrix = null
    }

    fun scale(sx: Float, sy: Float) {
        scaleX = sx
        scaleY = sy
    }

    fun translate(x: Float, y: Float) {

        val realAngle = mRotateAngle % 360
        mResultMatrix?.let {
            val w = rect.width() * scaleX * 4 * x
            val h = rect.height() * scaleY * y * 4

            val a: Pair<Float, Float> = when (realAngle) {
                90 -> Pair(-h, w)
                180 -> Pair(-w, -h)
                270 -> Pair(h, -w)
                else -> Pair(w, h)

            }
            Log.d(TAG, "translate: x:$x y:$y     w:$w h:$h   realAngle:$realAngle   final->:$a")
            Matrix.translateM(
                it,
                0,
                a.first,
                a.second,
                0f
            )
        }
    }

    fun setTexture(texture: SurfaceTexture) {
        mSurfaceTexture = texture
    }
}