package com.heng.ku.jnitest

import android.os.Bundle
import android.util.Log
import android.view.Surface
import androidx.appcompat.app.AppCompatActivity
import com.heng.ku.jnitest.media.decoder.AudioDecoder
import com.heng.ku.jnitest.media.decoder.VideoDecoder
import com.heng.ku.jnitest.opengl.EGLRender
import com.heng.ku.jnitest.opengl.drawer.IDrawer
import com.heng.ku.jnitest.opengl.drawer.VideoDrawer
import kotlinx.android.synthetic.main.activity_gl_video_play.*

/**
 * OpenGL渲染视频
 */
class GlVideoPlayActivity : AppCompatActivity() {
    private val TAG = "GlVideoPlayActivity"
    private var drawer: IDrawer? = null
    private var drawer2: IDrawer? = null
    private var mVideoDecoder1: VideoDecoder? = null
    private var mVideoDecoder2: VideoDecoder? = null
    private var mAudioDecoder1: AudioDecoder? = null
    private var mAudioDecoder2: AudioDecoder? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gl_video_play)
        initFirstDrawer()
        initSecondDrawer()

        val render = EGLRender()
        render.addDrawer(drawer!!)
        render.addDrawer(drawer2!!)


        render.setSurface(glSurfaceView)
    }

    private fun initSecondDrawer() {
        drawer2 = VideoDrawer().apply {
            alpha = 0.5f
            getSurfaceTexture {
                mVideoDecoder1 = VideoDecoder(VIDEO_FILE1, Surface(it))
                mAudioDecoder1 = AudioDecoder(VIDEO_FILE1)
                mVideoDecoder1!!.start()
                mAudioDecoder1!!.start()
                setVideoWh(mVideoDecoder1!!.getWidth(), mVideoDecoder1!!.getHeight())
                Log.d(TAG, "initSecondDrawer:${mVideoDecoder1!!.getRotationAngle()}")
                setRotationAngle(mVideoDecoder1!!.getRotationAngle())
                scale(0.5f, 0.5f)
            }
            glSurfaceView.videoDrawer = this
        }
    }

    private fun initFirstDrawer() {
        drawer = VideoDrawer().apply {
            alpha = 1f
            getSurfaceTexture {
                mVideoDecoder2 = VideoDecoder(VIDEO_FILE2, Surface(it))
                mAudioDecoder2 = AudioDecoder(VIDEO_FILE2)
                mVideoDecoder2!!.start()
                mAudioDecoder2!!.start()
                setVideoWh(mVideoDecoder2!!.getWidth(), mVideoDecoder2!!.getHeight())
                setRotationAngle(mVideoDecoder2!!.getRotationAngle())
                Log.d(TAG, "initFirstDrawer:${mVideoDecoder2!!.getRotationAngle()}")
            }
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        drawer?.release()
        drawer2?.release()
        mAudioDecoder1?.stop()
        mAudioDecoder2?.stop()
        mVideoDecoder1?.stop()
        mVideoDecoder2?.stop()
    }
}