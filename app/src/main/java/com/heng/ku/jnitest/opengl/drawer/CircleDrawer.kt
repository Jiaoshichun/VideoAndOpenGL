package com.heng.ku.jnitest.opengl.drawer

import android.opengl.GLES20
import android.opengl.Matrix
import com.heng.ku.jnitest.opengl.OpenGLTools
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * 圆形
 */
class CircleDrawer(val height: Float = 0f) : IDrawer {
    private var mWordW = 0
    private var mWordH = 0

    private var mTextureId = -1
    private var mProgram = -1

    private var mVertexShader = -1
    private var mFragmentShader = -1

    private var mAttrPosition = -1

    var mMatrix: FloatArray? = null
    private var mCoorsBuffer: FloatBuffer
    private val mCoors = mutableListOf<Float>()

    init {
        mCoors.add(0f)
        mCoors.add(0f)
        mCoors.add(height)

        for (i in 0 until 360) {
            val radian = i / (2 * PI)
            mCoors.add(sin(radian).toFloat())
            mCoors.add(cos(radian).toFloat())
            mCoors.add(height)
        }
        val direct = ByteBuffer.allocateDirect(mCoors.size * 4)
        direct.order(ByteOrder.nativeOrder())
        mCoorsBuffer = direct.asFloatBuffer()
        mCoorsBuffer.put(mCoors.toFloatArray())
        mCoorsBuffer.position(0)


    }

    override fun draw() {
        if (mTextureId == -1) return
        createProgram()
        initMatrix()
        GLES20.glUseProgram(mProgram)

        GLES20.glEnableVertexAttribArray(mAttrPosition)
        GLES20.glVertexAttribPointer(mAttrPosition, 3, GLES20.GL_FLOAT, false, 0, mCoorsBuffer)

        val uMatrix = GLES20.glGetUniformLocation(mProgram, "uMatrix")
        GLES20.glUniformMatrix4fv(uMatrix, 1, false, mMatrix, 0)

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_FAN, 0, mCoors.size /3)
    }

    private fun initMatrix() {
        if (mMatrix != null || mWordH == 0 || mWordW == 0) return

        mMatrix = FloatArray(16)

        val ratio = mWordH.toFloat() / mWordW

        val f1 = FloatArray(16)
        Matrix.frustumM(f1, 0, -1f, 1f, -ratio, ratio, 1f, 8f)

        val f2 = FloatArray(16)
        Matrix.setLookAtM(f2, 0, 0f, 0f, 2f, 0f, 0f, 0f, 0f, 1f, 0f)

        Matrix.multiplyMM(mMatrix, 0, f1, 0, f2, 0)
    }

    private fun createProgram() {
        if (mProgram != -1) return
        mProgram = GLES20.glCreateProgram()

        mVertexShader = OpenGLTools.loadShader(GLES20.GL_VERTEX_SHADER, getVertexCode())
        mFragmentShader = OpenGLTools.loadShader(GLES20.GL_FRAGMENT_SHADER, getFragmentCode())

        GLES20.glAttachShader(mProgram, mVertexShader)
        GLES20.glAttachShader(mProgram, mFragmentShader)

        GLES20.glLinkProgram(mProgram)
        mAttrPosition = GLES20.glGetAttribLocation(mProgram, "aPosition")

    }

    override fun setTextureID(id: Int) {
        mTextureId = id
    }

    override fun release() {
        GLES20.glDisableVertexAttribArray(mAttrPosition)
        GLES20.glDeleteShader(mFragmentShader)
        GLES20.glDeleteShader(mVertexShader)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0)
        GLES20.glDeleteTextures(1, intArrayOf(mTextureId), 0)
        GLES20.glDeleteProgram(mProgram)
    }

    private fun getVertexCode(): String {
        return """
            attribute vec4 aPosition;
            uniform mat4 uMatrix;
            void main(){
                gl_Position=uMatrix*aPosition;
            }
        """.trimIndent()
    }

    private fun getFragmentCode(): String {
        return """
            precision mediump float;
            void main(){
                gl_FragColor=vec4(1.0,1.0,1.0,1.0);
            }
        """.trimIndent()
    }

    override fun setWordWh(width: Int, height: Int) {
        mWordW = width
        mWordH = height
    }
}