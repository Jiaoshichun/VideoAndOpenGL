package com.heng.ku.jnitest

import android.media.MediaCodec
import android.os.Bundle
import android.view.Surface
import androidx.appcompat.app.AppCompatActivity
import com.heng.ku.jnitest.media.Frame
import com.heng.ku.jnitest.media.MMuxer
import com.heng.ku.jnitest.media.decoder.AudioDecoder
import com.heng.ku.jnitest.media.decoder.BaseDecoder
import com.heng.ku.jnitest.media.decoder.DefDecodeStateListener
import com.heng.ku.jnitest.media.decoder.VideoDecoder
import com.heng.ku.jnitest.media.encoder.AudioEncoder
import com.heng.ku.jnitest.media.encoder.VideoEncoder
import com.heng.ku.jnitest.opengl.EGLRender
import com.heng.ku.jnitest.opengl.drawer.VideoDrawer
import kotlinx.android.synthetic.main.activity_encoder_video.*
import java.nio.ByteBuffer

/**
 * 编码Mp4
 */
class EncoderVideoActivity : AppCompatActivity() {
    private val TAG = "MainActivity"
    private var videoDecoder: VideoDecoder? = null
    private var audioDecoder: AudioDecoder? = null

    private val eglRender = EGLRender()
    private lateinit var videoEncoder: VideoEncoder
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_encoder_video)
        val mMuxer = MMuxer(DEST_VIDEO_FILE)
        videoEncoder = VideoEncoder(mMuxer, 1080, 1920)
        videoEncoder.start()
        val audioEncoder = AudioEncoder(mMuxer)
        audioEncoder.start()
        eglRender.apply {
            setSurface(videoEncoder.mSurface!!, 1080, 1920)
            addDrawer(initVideoDrawer(videoEncoder, audioEncoder))
        }

    }

    private fun initVideoDrawer(
        videoEncoder: VideoEncoder,
        audioEncoder: AudioEncoder
    ): VideoDrawer {
        return VideoDrawer().apply {
            this.getSurfaceTexture {
                videoDecoder = VideoDecoder(VIDEO_FILE2, Surface(it))
                audioDecoder = AudioDecoder(VIDEO_FILE2)
                videoDecoder?.start()
                audioDecoder?.start()
                audioDecoder?.isSyncRender = false
                videoDecoder?.isSyncRender = false
                this.setVideoWh(videoDecoder!!.getWidth(), videoDecoder!!.getHeight())
                this.setRotationAngle(videoDecoder!!.getRotationAngle())

                audioDecoder!!.mStateListener = object : DefDecodeStateListener {
                    override fun decodeOneFrame(
                        decodeJob: BaseDecoder?,
                        buffer: ByteBuffer,
                        bufferInfo: MediaCodec.BufferInfo
                    ) {
                        audioEncoder.encodeOneFrame(Frame().apply {
                            this.buffer = buffer
                            setBufferInfo(bufferInfo)
                        })
                    }

                    override fun decoderStop(decodeJob: BaseDecoder?) {
                        super.decoderStop(decodeJob)
                        audioEncoder.stop()
                    }
                }

                videoDecoder!!.mStateListener = object : DefDecodeStateListener {
                    override fun decoderStop(decodeJob: BaseDecoder?) {
                        runOnUiThread {
                            txtEncode.text = "编码完成"
                        }
                        videoEncoder.stop()
                        eglRender.stop()

                    }

                    override fun decodeOneFrame(
                        decodeJob: BaseDecoder?,
                        buffer: ByteBuffer,
                        bufferInfo: MediaCodec.BufferInfo
                    ) {
                        videoEncoder.encodeOneFrame(Frame().apply {
                            this.buffer = buffer
                            setBufferInfo(bufferInfo)
                        })
                    }

                }
            }
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        audioDecoder?.stop()
        videoDecoder?.stop()
        eglRender.stop()
    }
}
