package com.heng.ku.jnitest

import android.graphics.SurfaceTexture
import android.hardware.Camera
import android.media.AudioFormat
import android.os.Bundle
import android.util.Log
import android.view.Surface
import android.view.TextureView
import androidx.appcompat.app.AppCompatActivity
import com.heng.ku.jnitest.media.AudioRecordUtils
import com.heng.ku.jnitest.media.Frame
import com.heng.ku.jnitest.media.MMuxer
import com.heng.ku.jnitest.media.encoder.AudioEncoder
import com.heng.ku.jnitest.media.encoder.VideoEncoder
import com.heng.ku.jnitest.opengl.EGLRenderPro
import com.heng.ku.jnitest.opengl.drawer.VideoDrawer
import kotlinx.android.synthetic.main.activity_camera_record.*

private const val sampleRate = 44100//采样率
private const val channelCount = 2//声道数
private const val audioFormat = AudioFormat.ENCODING_PCM_16BIT//编码格式
private const val bitPerSample = 16//每个采样占的字节数

/**
 * 使用相机进行录制
 */
class CameraRecordActivity : AppCompatActivity() {

    private val TAG = "CameraRecordActivity"
    private var camera: Camera? = null
    private val eglRender = EGLRenderPro()

    private var isRunning = false


    private var audioRecordUtils = AudioRecordUtils(sampleRate, channelCount, audioFormat)
    private val mMuxer = MMuxer(DEST_VIDEO_FILE)
    private var videoEncoder = VideoEncoder(mMuxer, 720, 1280)
    private val audioEncoder = AudioEncoder(mMuxer, sampleRate, channelCount, bitPerSample)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera_record)
        Log.d(TAG, "onCreate")
        txtStart.text = if (isRunning) "停止" else "开始"
        val videoDrawer = VideoDrawer()

        val texture = SurfaceTexture(EGLRenderPro.TEXTURE_ID)
        texture.setOnFrameAvailableListener {
            if (isRunning) {
                videoEncoder.encodeOneFrame(Frame())
            }
        }
        Log.d(TAG, "startCamera")
        startCamera(texture)
        videoDrawer.setVideoWh(
            camera!!.parameters.previewSize.width,
            camera!!.parameters.previewSize.height
        )
        videoDrawer.setRotationAngle(90)

        videoDrawer.setTexture(texture)
        eglRender.addDrawer(videoDrawer)
        textureView.surfaceTextureListener = object : TextureView.SurfaceTextureListener {
            override fun onSurfaceTextureSizeChanged(
                surface: SurfaceTexture?,
                width: Int,
                height: Int
            ) {
            }

            override fun onSurfaceTextureUpdated(surface: SurfaceTexture?) {
            }

            override fun onSurfaceTextureDestroyed(surface: SurfaceTexture?): Boolean = true
            override fun onSurfaceTextureAvailable(
                surface: SurfaceTexture?,
                width: Int,
                height: Int
            ) {
                Log.d(TAG, "onSurfaceTextureAvailable")
                eglRender.addSurface(
                    Surface(surface),
                    textureView.width,
                    textureView.height
                )

            }

        }

        txtStart.setOnClickListener {
            isRunning = !isRunning
            if (isRunning) {
                startRecord()
                txtStart.text = "停止"
            } else {
                txtStart.text = "开始"
                stopRecord()
            }
        }
        audioRecordUtils.addCallBack { byteBuffer, size, timeNs ->
            audioEncoder.encodeOneFrame(Frame().apply {
                buffer = byteBuffer
                setBufferInfo(0, size, (timeNs + 500) / 1000, 0)
            })
        }
    }

    private fun startRecord() {
        audioEncoder.start()
        audioRecordUtils.start()
        videoEncoder.start()
        eglRender.isStart=true
        eglRender.addSurface(videoEncoder.mSurface!!, 720, 1280)
    }

    private fun stopRecord() {
        audioRecordUtils.stop()
        videoEncoder.stop()
        audioEncoder.stop()
        eglRender.isStart=false
        videoEncoder.mSurface?.let {
            eglRender.removeSurface(it)
        }
    }

    private fun startCamera(it: SurfaceTexture) {
        camera = Camera.open(0)
        val params = camera!!.parameters
        params.setPreviewSize(1280, 720)
        camera!!.setPreviewTexture(it)
        // 设置自动对焦
        val focusModes = params.supportedFocusModes
        if (null != focusModes && focusModes.size > 0
            && focusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)
        ) {
            params.focusMode = Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE
        }
        camera?.parameters = params
        camera?.startPreview()
    }

    override fun onPause() {
        super.onPause()
        stopRecord()
        camera?.stopPreview()
    }


    override fun onDestroy() {
        super.onDestroy()

        camera?.setPreviewCallback(null)
        camera?.release()
        eglRender.stop()
        camera = null
    }

    override fun onResume() {
        super.onResume()
        camera?.startPreview()
    }
}
