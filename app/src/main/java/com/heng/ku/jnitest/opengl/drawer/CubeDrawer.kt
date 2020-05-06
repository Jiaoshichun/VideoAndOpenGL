package com.heng.ku.jnitest.opengl.drawer

import android.opengl.GLES20
import android.opengl.Matrix
import com.heng.ku.jnitest.opengl.OpenGLTools
import java.nio.*

/**
 * 立方体
 */
class CubeDrawer : IDrawer {

    private var mTextureId = -1
    //程序
    private var mProgram = -1
    //顶点着色器
    private var mVertexShader = -1
    //片元着色器
    private var mFragmentShader = -1

    //顶点坐标
    private val mVertexCoors = floatArrayOf(
        -1f, -1f, 1f,
        1f, -1f, 1f,
        -1f, -1f, -1f,
        1f, -1f, -1f,
        1f, 1f, -1f,
        -1f, 1f, -1f,
        -1f, 1f, 1f,
        1f, 1f, 1f
    )
    private val COORDS_PER_VERTEX = 3
    //顶点个数
    private val vertexCount = mVertexCoors.size / COORDS_PER_VERTEX

    //顶点之间的偏移量
    private val vertexStride = COORDS_PER_VERTEX * 4 // 每个顶点四个字节
    private val mIndexes = shortArrayOf(
        0, 1, 2, 1, 2, 3,
        6, 0, 2, 6, 5, 2,
        0, 6, 1, 6, 1, 7,
        1, 7, 3, 7, 3, 4,
        7, 6, 5, 7, 4, 5,
        2, 3, 5, 3, 5, 4
    )
    private val mColors = floatArrayOf(
        1f, 0f, 0f, 1f,
        1f, 0f, 0f, 1f,
        1f, 0f, 0f, 1f,
        1f, 0f, 0f, 1f,
        0f, 1f, 0f, 1f,
        0f, 1f, 0f, 1f,
        0f, 1f, 0f, 1f,
        0f, 1f, 0f, 1f
    )
    private val mColorsBuffer: FloatBuffer
    private val mVertexBuffer: FloatBuffer
    private val mIndexesBuffer: ShortBuffer

    private var mPositionAttr = -1
    private var mColorAttr = -1
    private var mMatrixUniform = -1
    private var mMatrix: FloatArray? = null
    private var mWordW = 0
    private var mWordH = 0

    init {
        val aa = ByteBuffer.allocateDirect(mColors.size * 4)
        aa.order(ByteOrder.nativeOrder())
        mColorsBuffer = aa.asFloatBuffer()
        mColorsBuffer.put(mColors)
        mColorsBuffer.position(0)

        val bb = ByteBuffer.allocateDirect(mVertexCoors.size * 4)
        bb.order(ByteOrder.nativeOrder())
        mVertexBuffer = bb.asFloatBuffer()
        mVertexBuffer.put(mVertexCoors)
        mVertexBuffer.position(0)

        val cc = ByteBuffer.allocateDirect(mIndexes.size * 2)
        cc.order(ByteOrder.nativeOrder())
        mIndexesBuffer = cc.asShortBuffer()
        mIndexesBuffer.put(mIndexes)
        mIndexesBuffer.position(0)

    }

    override fun draw() {
        if (mTextureId == -1) return
        createProgram()
        initMatrix()
        doDraw()
    }

    private fun doDraw() {
        //属性可用
        GLES20.glEnableVertexAttribArray(mPositionAttr)
        GLES20.glEnableVertexAttribArray(mColorAttr)

        //设置属性值
        GLES20.glVertexAttribPointer(mPositionAttr, 3, GLES20.GL_FLOAT, false, 0, mVertexBuffer)
        GLES20.glVertexAttribPointer(mColorAttr, 4, GLES20.GL_FLOAT, false, 0, mColorsBuffer)
        if (mMatrix != null) {
            GLES20.glUniformMatrix4fv(mMatrixUniform, 1, false, mMatrix, 0)
        }

        //索引法绘制正方体
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, mIndexes.size, GLES20.GL_UNSIGNED_SHORT, mIndexesBuffer)
    }

    private fun initMatrix() {
        if (mMatrix != null || mWordH == 0 || mWordW == 0) return
        mMatrix = FloatArray(16)

        //计算宽高比
        val i = mWordH.toFloat() / mWordW
        val m1 = FloatArray(16)
        Matrix.frustumM(m1, 0, -1f, 1f, -i, i, 5f, 20f)
        //设置相机位置
        val m2 = FloatArray(16)
        Matrix.setLookAtM(m2, 0, 5f, -10f, 5f, 0f, 0f, 0f, 0f, 0.0f, 1.0f)
        //计算变换矩阵
        Matrix.multiplyMM(mMatrix, 0, m1, 0, m2, 0)

    }

    private fun createProgram() {
        if (mProgram == -1) {
            //加载着色器
            mVertexShader = OpenGLTools.loadShader(GLES20.GL_VERTEX_SHADER, getVertexCode())
            mFragmentShader = OpenGLTools.loadShader(GLES20.GL_FRAGMENT_SHADER, getFragmentCode())

            //创建程序
            mProgram = GLES20.glCreateProgram()

            //将程序和着色器进行绑定
            GLES20.glAttachShader(mProgram, mVertexShader)
            GLES20.glAttachShader(mProgram, mFragmentShader)

            //连接着色器
            GLES20.glLinkProgram(mProgram)

            //获取对应的属性
            mPositionAttr = GLES20.glGetAttribLocation(mProgram, "aPosition")
            mColorAttr = GLES20.glGetAttribLocation(mProgram, "aColor")
            mMatrixUniform = GLES20.glGetUniformLocation(mProgram, "mMatrix")

        }
        //适用程序
        GLES20.glUseProgram(mProgram)
    }

    private fun getVertexCode(): String {
        return """
            attribute vec4 aPosition;
            uniform mat4 mMatrix; 
            varying vec4 vColor;
            attribute vec4 aColor;
            void main(){
                gl_Position=mMatrix*aPosition;
                vColor=aColor;
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
        mTextureId = id
    }

    override fun release() {
        GLES20.glDisableVertexAttribArray(mPositionAttr)
        GLES20.glDisableVertexAttribArray(mColorAttr)
        GLES20.glDetachShader(mProgram, mVertexShader)
        GLES20.glDetachShader(mProgram, mFragmentShader)
        GLES20.glDeleteShader(mVertexShader)
        GLES20.glDeleteShader(mFragmentShader)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0)
        GLES20.glDeleteTextures(1, intArrayOf(mTextureId), 0)
        GLES20.glDeleteProgram(mProgram)
    }

    override fun setWordWh(width: Int, height: Int) {
        mWordW = width
        mWordH = height
    }
}