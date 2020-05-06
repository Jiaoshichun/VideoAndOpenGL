package com.heng.ku.jnitest.opengl.drawer

import android.opengl.GLES20
import android.opengl.Matrix
import android.util.Log
import com.heng.ku.jnitest.opengl.OpenGLTools
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import kotlin.math.cos
import kotlin.math.sin

/**
 * 圆柱体
 */
class CylinderDrawer : IDrawer {
    private var textureId = -1
    private var mProgram = -1
    private var mWidth = 0
    private var mHeight = 0
    private var mVertexShader = -1
    private var mFragmentShader = -1
    private var mVertexPosBuffer: FloatBuffer
    private val radius = 1f
    private var mMatrix: FloatArray? = null

    private var mAttrPosition = -1

    private val pos = mutableListOf<Float>()
    private val PER = 3
    private val mTopCircle = CircleDrawer(2f)
    private val mBottomCircle = CircleDrawer()

    init {

        for (i in 0 .. 360) {
            pos.add((radius * sin(i * Math.PI / 180f)).toFloat())
            pos.add((radius * cos(i * Math.PI / 180f)).toFloat())
            pos.add(0.0f)

            pos.add((radius * sin(i * Math.PI / 180f)).toFloat())
            pos.add((radius * cos(i * Math.PI / 180f)).toFloat())
            pos.add(2f)
        }

        val aa = ByteBuffer.allocateDirect(pos.size * 4)
        aa.order(ByteOrder.nativeOrder())
        mVertexPosBuffer = aa.asFloatBuffer()
        mVertexPosBuffer.put(pos.toFloatArray())
        mVertexPosBuffer.position(0)
    }

    override fun draw() {
        if (textureId == -1) return
        initMatrix()
        mTopCircle.mMatrix = mMatrix
        mBottomCircle.mMatrix = mMatrix
        mTopCircle.draw()
        mBottomCircle.draw()
        createProgram()

        GLES20.glUseProgram(mProgram)

        GLES20.glEnableVertexAttribArray(mAttrPosition)

        GLES20.glVertexAttribPointer(mAttrPosition, PER, GLES20.GL_FLOAT, false, 0, mVertexPosBuffer)

        val uMatrix = GLES20.glGetUniformLocation(mProgram, "uMatrix")
        GLES20.glUniformMatrix4fv(uMatrix, 1, false, mMatrix, 0)


        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, pos.size / PER)

    }

    private fun initMatrix() {
        if (mMatrix != null || mWidth == 0 || mHeight == 0) return
        mMatrix = FloatArray(16)

        val ratio = mHeight.toFloat() / mWidth
        val f2 = FloatArray(16)
        Matrix.frustumM(f2, 0, -1f, 1f, -ratio, ratio, 1f, 20f)

        val f1 = FloatArray(16)
        Matrix.setLookAtM(f1, 0, -4f, -4f, -4f, 0f, 0f, 0f, 0f, 1f, 0f)


        Matrix.multiplyMM(mMatrix, 0, f2, 0, f1, 0)

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

        // 存放链接成功program数量的数组
        val linkStatus = IntArray(1)
        // 获取program的链接情况
        GLES20.glGetProgramiv(mProgram, GLES20.GL_LINK_STATUS, linkStatus, 0)
        // 若链接失败则报错并删除程序
        if (linkStatus[0] != GLES20.GL_TRUE) {
            Log.e("ES20_ERROR", "Could not link program: ")
            Log.e("ES20_ERROR", GLES20.glGetProgramInfoLog(mProgram))
            GLES20.glDeleteProgram(mProgram)
            mProgram = 0
        }
    }

    private fun getVertexCode(): String {
        return """
            attribute vec4 aPosition;
            uniform mat4 uMatrix;
            varying vec4 vColor;
            void main(){
                gl_Position=uMatrix*aPosition;
                if(aPosition.z==0.0){
                    vColor=vec4(1.0,0.0,0.0,0.8);
                }else{
                    vColor=vec4(1.0,1.0,1.0,1.0);
                }
            }
        """.trimIndent()
    }

    private fun getFragmentCode(): String {
        return """
            precision mediump float;
            varying vec4 vColor;
            void main(){
                gl_FragColor=vColor;
            }
        """.trimIndent()
    }

    override fun setTextureID(id: Int) {
        textureId = id
        mTopCircle.setTextureID(id)
        mBottomCircle.setTextureID(id)
    }

    override fun release() {
        mBottomCircle.release()
        mTopCircle.release()

        GLES20.glDisableVertexAttribArray(mAttrPosition)
        GLES20.glDeleteShader(mVertexShader)
        GLES20.glDeleteShader(mFragmentShader)
        GLES20.glDeleteTextures(1, intArrayOf(textureId), 0)
        GLES20.glDeleteProgram(mProgram)
    }

    override fun setWordWh(width: Int, height: Int) {
        mWidth = width
        mHeight = height
        mTopCircle.setWordWh(width, height)
        mBottomCircle.setWordWh(width, height)
    }
}