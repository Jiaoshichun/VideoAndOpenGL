package com.heng.record.video.view.opengl

import android.opengl.GLES20
import com.heng.record.video.view.BuildConfig
import com.heng.record.video.view.utils.LogUtils

object OpenGLTools {
    /**
     * 创建纹理ID
     */
    fun createTextureIds(count: Int): IntArray {
        val texture = IntArray(count)
        GLES20.glGenTextures(count, texture, 0) //生成纹理
        return texture
    }

    /**
     * 加载着色器
     * @param type 着色器类型
     * @param code 要加载的着色器代码
     */
    fun loadShader(type: Int, code: String): Int {

        val shader = GLES20.glCreateShader(type)
        GLES20.glShaderSource(shader, code)
        GLES20.glCompileShader(shader)
        if (BuildConfig.DEBUG) {
            val compiled = IntArray(1)
            GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0)
            if (compiled[0] == 0) {//若编译失败则显示错误日志并删除此shader
                LogUtils.e("ES20_ERROR", "Could not compile shader $type:");
                LogUtils.e("ES20_ERROR", GLES20.glGetShaderInfoLog(shader));
                GLES20.glDeleteShader(shader)
                return 0
            }
        }
        return shader
    }

    /**
     * 新建FBO纹理
     */
    fun createFBOTexure(width: Int, height: Int): IntArray {
        //创建纹理
        val texture = IntArray(1)
        GLES20.glGenTextures(texture.size, texture, 0)

        //绑定纹理ID
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture[0])

        // 根据颜色参数，宽高等信息，为上面的纹理ID，生成一个2D纹理
        GLES20.glTexImage2D(
            GLES20.GL_TEXTURE_2D,
            0,
            GLES20.GL_RGB,
            width,
            height,
            0,
            GLES20.GL_RGB,
            GLES20.GL_UNSIGNED_BYTE,
            null
        )

        // 设置纹理边缘参数
        GLES20.glTexParameterf(
            GLES20.GL_TEXTURE_2D,
            GLES20.GL_TEXTURE_MIN_FILTER,
            GLES20.GL_NEAREST.toFloat()
        )
        GLES20.glTexParameterf(
            GLES20.GL_TEXTURE_2D,
            GLES20.GL_TEXTURE_MAG_FILTER,
            GLES20.GL_NEAREST.toFloat()
        )
        GLES20.glTexParameterf(
            GLES20.GL_TEXTURE_2D,
            GLES20.GL_TEXTURE_WRAP_S,
            GLES20.GL_CLAMP_TO_EDGE.toFloat()
        )
        GLES20.glTexParameterf(
            GLES20.GL_TEXTURE_2D,
            GLES20.GL_TEXTURE_WRAP_T,
            GLES20.GL_CLAMP_TO_EDGE.toFloat()
        )
        //解绑纹理id
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0)
        return texture
    }

    /**
     * 新建FrameBuffer
     */
    fun createFrameBuffer(): Int {
        val ints = IntArray(1)
        GLES20.glGenFramebuffers(1, ints, 0)
        return ints[0]
    }

    /**
     * 绑定FBO
     */
    fun bindFBO(fb: Int, textureId: Int) {
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, fb)
        GLES20.glFramebufferTexture2D(
            GLES20.GL_FRAMEBUFFER,
            GLES20.GL_COLOR_ATTACHMENT0,
            GLES20.GL_TEXTURE_2D,
            textureId,
            0
        )
    }

    /**
     * 解绑FBO
     */
    fun unBindFBO() {
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, GLES20.GL_NONE)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0)
    }

    /**
     * 删除FBO
     */
    fun deleteFBO(frame: IntArray, texture: IntArray) {
        //删除Frame Buffer
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, GLES20.GL_NONE)
        GLES20.glDeleteFramebuffers(1, frame, 0)
        //删除纹理
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0)
        GLES20.glDeleteTextures(1, texture, 0)
    }

//    /**
//     * 从OpenGl中获取Bitmap
//     * 将OpenGL中的RGBA像素转为ARGB像素
//     */
//    fun getBitmapFromGl(width: Int, height: Int): Bitmap {
//        val pixBuffer = IntBuffer.allocate(width * height)
//        GLES20.glReadPixels(
//            0,
//            0,
//            width,
//            height,
//            GLES20.GL_RGBA,
//            GLES20.GL_UNSIGNED_BYTE,
//            pixBuffer
//        )
//        val glPixel = pixBuffer.array()
//        val argbPixel = IntArray(height * width)
//        ColorHelper.FIXGLPIXEL(glPixel, argbPixel, width, height)
//        return Bitmap.createBitmap(
//            argbPixel,
//            width,
//            height,
//            Bitmap.Config.ARGB_8888
//        )
//    }
}