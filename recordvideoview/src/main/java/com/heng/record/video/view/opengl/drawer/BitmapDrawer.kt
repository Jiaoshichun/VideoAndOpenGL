package com.heng.record.video.view.opengl.drawer

import android.graphics.Bitmap
import android.opengl.GLES20
import android.opengl.GLUtils
import com.heng.record.video.view.opengl.OpenGLTools.loadShader
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

class BitmapDrawer(private val mBitmap: Bitmap) : IDrawer {


    private val mVertexCoors = floatArrayOf(
        -1f, -1f,
        1f, -1f,
        -1f, 1f,
        1f, 1f
    )
    private val mTextureCoors = floatArrayOf(
        0f, 1f,
        1f, 1f,
        0f, 0f,
        1f, 0f
    )
    private val mVertexBuffer: FloatBuffer
    private val mTextureBuffer: FloatBuffer

    private var mTextureId = -1

    private var mProgram = -1

    private var mVertexShader = -1
    private var mFragmentShader = -1

    private var mVertexPosHandler = -1
    private var mTexturePosHandler = -1
    private var mTextureHandler = -1

    init {
        val buffer = ByteBuffer.allocateDirect(mVertexCoors.size * 4)
        buffer.order(ByteOrder.nativeOrder())
        mVertexBuffer = buffer.asFloatBuffer()
        mVertexBuffer.put(mVertexCoors)
        mVertexBuffer.position(0)

        val buffer2 = ByteBuffer.allocateDirect(mTextureCoors.size * 4)
        buffer2.order(ByteOrder.nativeOrder())
        mTextureBuffer = buffer2.asFloatBuffer()
        mTextureBuffer.put(mTextureCoors)
        mTextureBuffer.position(0)
    }

    override fun draw() {
        if (mTextureId == -1) return
        //创建程序
        createProgram()
        //激活并绑定纹理单元
        activeTexture()
        //绑定图片到纹理单元
        bindBitmapToTexture()

        doDraw()
    }

    /**
     * 绑定图片到纹理单元
     */
    private fun bindBitmapToTexture() {
        if (!mBitmap.isRecycled) {
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, mBitmap, 0)
        }
    }

    /**
     * 激活并绑定纹理单元
     */
    private fun activeTexture() {
        //激活指定纹理单元
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)

        //绑定纹理id到纹理单元
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureId)

        //将激活的纹理单元传递到着色器里面
        GLES20.glUniform1i(mTextureHandler, 0)

        //配置边缘过渡参数
        //配置纹理过滤模式
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR.toFloat())
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR.toFloat())

        //配置纹理环绕方式
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE)
    }

    private fun doDraw() {
        //启用顶点的句柄
        GLES20.glEnableVertexAttribArray(mVertexPosHandler)
        GLES20.glEnableVertexAttribArray(mTexturePosHandler)
        //设置着色器参数， 第二个参数表示一个顶点包含的数据数量，这里为xy，所以为2
        GLES20.glVertexAttribPointer(mVertexPosHandler, 2, GLES20.GL_FLOAT, false, 0, mVertexBuffer)
        GLES20.glVertexAttribPointer(mTexturePosHandler, 2, GLES20.GL_FLOAT, false, 0, mTextureBuffer)
        //开始绘制
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)
    }

    private fun createProgram() {
        if (mProgram == -1) {
            mVertexShader = loadShader(GLES20.GL_VERTEX_SHADER, getVertexCode())
            mFragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, getFragmentCode())

            //创建程序
            mProgram = GLES20.glCreateProgram()

            //将创新和着色器进行绑定
            GLES20.glAttachShader(mProgram, mVertexShader)
            GLES20.glAttachShader(mProgram, mFragmentShader)

            //连接程序
            GLES20.glLinkProgram(mProgram)

            mVertexPosHandler = GLES20.glGetAttribLocation(mProgram, "aPosition")
            mTexturePosHandler = GLES20.glGetAttribLocation(mProgram, "aCoordinate")
            mTextureHandler = GLES20.glGetUniformLocation(mProgram, "uTexture")
        }
        //使用程序
        GLES20.glUseProgram(mProgram)
    }

    private fun getFragmentCode(): String {
        return """
           precision mediump float;
           uniform sampler2D uTexture;
           varying vec2 vCoordinate;
           void main(){
                vec4 color=texture2D(uTexture,vCoordinate);
                gl_FragColor=color;
           }
        """.trimIndent()
    }

    private fun getVertexCode(): String {
        return """
            attribute vec4 aPosition;
            attribute vec2 aCoordinate;
            varying vec2 vCoordinate;
            void main(){
                gl_Position=aPosition;
                vCoordinate=aCoordinate;
            }
        """.trimIndent()
    }


    override fun setTextureID(id: Int) {
        mTextureId = id
    }

    override fun release() {
        GLES20.glDisableVertexAttribArray(mTexturePosHandler)
        GLES20.glDisableVertexAttribArray(mVertexPosHandler)

        GLES20.glDetachShader(mProgram, mVertexShader)
        GLES20.glDetachShader(mProgram, mFragmentShader)
        GLES20.glDeleteShader(mVertexShader)
        GLES20.glDeleteShader(mFragmentShader)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0)
        GLES20.glDeleteTextures(1, IntArray(mTextureId), 0)
        GLES20.glDeleteProgram(mProgram)
    }

    override fun setRotationAngle(angle: Int) {

    }

    override fun setVideoWh(width: Int, height: Int) {

    }

    override fun setWordWh(width: Int, height: Int) {

    }
}