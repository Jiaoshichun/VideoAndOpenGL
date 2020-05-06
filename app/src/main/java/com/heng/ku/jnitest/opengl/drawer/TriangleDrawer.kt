package com.heng.ku.jnitest.opengl.drawer

import android.opengl.GLES20
import com.heng.ku.jnitest.opengl.OpenGLTools.loadShader
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

class TriangleDrawer : IDrawer {


    //顶点坐标
    private val mVertexCoordinates = floatArrayOf(
        -1f, -1f,
        1f, -1f,
        0f, 1f
    )

    //纹理id
    private var textureId = -1
    private var mVertexBuffer: FloatBuffer

    // OpenGl ES程序
    private var mProgram = -1

    // 顶点坐标接收者
    private var mVertexPosHandler: Int = -1

    private var mVertexShader = -1
    private var mFragmentShader = -1

    init {
        val buffer = ByteBuffer.allocateDirect(mVertexCoordinates.size * 4)
        buffer.order(ByteOrder.nativeOrder())
        mVertexBuffer = buffer.asFloatBuffer()
        mVertexBuffer.put(mVertexCoordinates)
        mVertexBuffer.position(0)

    }

    override fun draw() {
        if (textureId == -1) return
        createGlPro()
        doDraw()
    }

    private fun doDraw() {
        //启动顶点的句柄
        GLES20.glEnableVertexAttribArray(mVertexPosHandler)

        //设置着色器参数
        GLES20.glVertexAttribPointer(mVertexPosHandler, 2, GLES20.GL_FLOAT, false, 0, mVertexBuffer)

        //开始绘制
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 3)
    }

    private fun createGlPro() {
        if (mProgram == -1) {
            mVertexShader = loadShader(GLES20.GL_VERTEX_SHADER, getVertexCode())
            mFragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, getFragmentCode())

            //创建 openGL es 程序  必须在渲染线程中创建
            mProgram = GLES20.glCreateProgram()
            //将定点着色器 绑定到程序
            GLES20.glAttachShader(mProgram, mVertexShader)
            //将片元着色器 绑定到程序
            GLES20.glAttachShader(mProgram, mFragmentShader)

            //连接 openGL es程序
            GLES20.glLinkProgram(mProgram)

            mVertexPosHandler = GLES20.glGetAttribLocation(mProgram, "aPosition")
        }
        //使用程序
        GLES20.glUseProgram(mProgram)
    }

    override fun setTextureID(id: Int) {
        textureId = id
    }

    override fun release() {

        GLES20.glDisableVertexAttribArray(mVertexPosHandler)
        GLES20.glDeleteShader(mVertexShader)
        GLES20.glDeleteShader(mFragmentShader)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0)
        GLES20.glDeleteTextures(1, intArrayOf(textureId), 0)
        GLES20.glDeleteProgram(mProgram)
    }

    private fun getVertexCode(): String {
        return """
            attribute vec4 aPosition;
            void main() {
                gl_Position = aPosition;
            }
        """.trimIndent()
    }

    private fun getFragmentCode(): String {
        return """
            precision mediump float;
            void main() {
                gl_FragColor = vec4(1.0, 1.0, 1.0, 1.0);
            }
        """.trimIndent()
    }



    override fun setRotationAngle(angle: Int) {

    }

    override fun setVideoWh(width: Int, height: Int) {

    }

    override fun setWordWh(width: Int, height: Int) {

    }
}