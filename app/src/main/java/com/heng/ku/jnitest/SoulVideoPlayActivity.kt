package com.heng.ku.jnitest

import android.os.Bundle
import android.util.Log
import android.view.Surface
import androidx.appcompat.app.AppCompatActivity
import com.heng.ku.jnitest.media.decoder.AudioDecoder
import com.heng.ku.jnitest.media.decoder.VideoDecoder
import com.heng.ku.jnitest.opengl.EGLRender
import com.heng.ku.jnitest.opengl.drawer.IDrawer
import com.heng.ku.jnitest.opengl.drawer.SoulVideoDrawer
import kotlinx.android.synthetic.main.activity_soul_video_play.*

/**
 * OpenGL渲染视频
 */
class SoulVideoPlayActivity : AppCompatActivity() {
    private val TAG = "GlVideoPlayActivity"
    private var drawer: IDrawer? = null
    private var mVideoDecoder: VideoDecoder? = null
    private var mAudioDecoder: AudioDecoder? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_soul_video_play)
        initDrawer()
        val render = EGLRender()
        render.addDrawer(drawer!!)
        render.setSurface(glSurfaceView)
    }


    private fun initDrawer() {
        drawer = SoulVideoDrawer().apply {
            getSurfaceTexture {
                mVideoDecoder = VideoDecoder(VIDEO_FILE1, Surface(it))
                mAudioDecoder = AudioDecoder(VIDEO_FILE1)
                mVideoDecoder!!.start()
                mAudioDecoder!!.start()
                setVideoWh(mVideoDecoder!!.getWidth(), mVideoDecoder!!.getHeight())
                setRotationAngle(mVideoDecoder!!.getRotationAngle())
                Log.d(TAG, "initFirstDrawer:${mVideoDecoder!!.getRotationAngle()}")
            }
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        drawer?.release()
        mAudioDecoder?.stop()
        mVideoDecoder?.stop()
    }
}