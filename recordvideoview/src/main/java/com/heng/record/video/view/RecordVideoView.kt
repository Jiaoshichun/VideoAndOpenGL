package com.heng.record.video.view

import android.content.Context
import android.graphics.SurfaceTexture
import android.hardware.Camera
import android.util.AttributeSet
import android.util.Log
import android.view.Surface
import android.view.TextureView
import android.widget.FrameLayout
import com.heng.record.video.view.media.AudioRecordUtils
import com.heng.record.video.view.media.Frame
import com.heng.record.video.view.media.MMuxer
import com.heng.record.video.view.media.encoder.AudioEncoder
import com.heng.record.video.view.media.encoder.VideoEncoder
import com.heng.record.video.view.opengl.EGLRenderPro
import com.heng.record.video.view.opengl.drawer.VideoDrawer
import java.nio.ByteBuffer

/**
 * 视频录制view
 */
class RecordVideoView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) :
    FrameLayout(context, attrs, defStyleAttr), IRecordVideoView,
    TextureView.SurfaceTextureListener, Camera.PreviewCallback {

    private val textureView: TextureView = TextureView(context).apply {
        addView(this, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
        surfaceTextureListener = this@RecordVideoView
    }

    private var camera: Camera? = null

    //视频渲染
    private val eglRender = EGLRenderPro()

    //是否录制视频
    private var isRecordVideo = false

    //是否录制音频
    private var isRecordAudio = false

    /**
     * 语音录制工具类
     */
    private var audioRecordUtils = AudioRecordUtils(
        RecordeVideoConfig.audioSampleRate,
        RecordeVideoConfig.audioChannelCount,
        RecordeVideoConfig.audioFormat
    ).apply {
        addCallBack(::audioRecordCallBack)
    }

    private var videoEncoder: VideoEncoder? = null
    private var audioEncoder: AudioEncoder? = null


    private val texture = SurfaceTexture(EGLRenderPro.TEXTURE_ID).apply {
        setOnFrameAvailableListener {
            if (isRecordVideo) {
                videoEncoder?.encodeOneFrame(Frame())
            }
        }
    }

    init {
        initDrawer()
    }

    /**
     * 初始化视频绘制
     */
    private fun initDrawer() {
        VideoDrawer().apply {
            eglRender.addDrawer(this)
            setVideoWh(
                RecordeVideoConfig.previewWidth,
                RecordeVideoConfig.previewHeight
            )
            if (RecordeVideoConfig.videoOrientation == RecordeVideoConfig.VIDEO_ORIENTATION_PORTRAIT) {
                //竖屏需要旋转90度
                setRotationAngle(90)
            }

            setTexture(texture)
        }
    }

    override fun startPreview(cameraId: Int) {
        stopPreview()
        startCamera(texture, cameraId)
    }

    private fun startCamera(it: SurfaceTexture, cameraId: Int) {
        camera = Camera.open(cameraId)
        val params = camera!!.parameters
        params.setPreviewSize(RecordeVideoConfig.previewWidth, RecordeVideoConfig.previewHeight)
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
        camera?.setPreviewCallback(this)
    }

    override fun startRecordVideo(videoPath: String) {
        stopRecordVideo()
        stopRecordAudio()
        isRecordVideo = true
        isRecordAudio = true
        val mMuxer = MMuxer(videoPath)
        videoEncoder = VideoEncoder(
            mMuxer,
            RecordeVideoConfig.videoWidth, RecordeVideoConfig.videoHeight
        )
        audioEncoder = AudioEncoder(
            mMuxer, RecordeVideoConfig.audioSampleRate,
            RecordeVideoConfig.audioChannelCount,
            RecordeVideoConfig.audioBitPerSample
        )
        audioEncoder!!.start()
        audioRecordUtils.start()
        videoEncoder!!.start()
        eglRender.isStart = true
        eglRender.addSurface(
            videoEncoder!!.mSurface!!,
            RecordeVideoConfig.videoWidth,
            RecordeVideoConfig.videoHeight
        )
    }

    override fun stopRecordVideo() {

        isRecordVideo = false
        isRecordAudio = false
        audioRecordUtils.stop()
        videoEncoder?.stop()
        videoEncoder = null
        audioEncoder?.stop()
        audioEncoder = null
        eglRender.isStart = false
        videoEncoder?.mSurface?.let {
            eglRender.removeSurface(it)
        }
    }

    override fun startRecordAudio(audioPath: String) {
        stopRecordVideo()
        stopRecordAudio()
        isRecordVideo = false
        isRecordAudio = true
        audioRecordUtils.start()

    }

    override fun stopRecordAudio() {
        isRecordVideo = false
        isRecordAudio = false
        audioRecordUtils.stop()
    }

    override fun stopPreview() {
        isRecordVideo = false
        isRecordAudio = false
        camera?.stopPreview()
        camera?.setPreviewCallback(null)
        camera?.release()
        camera = null
    }


    override fun onSurfaceTextureAvailable(surface: SurfaceTexture?, width: Int, height: Int) {
        eglRender.addSurface(
            Surface(surface),
            textureView.width,
            textureView.height
        )

    }

    override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture?, width: Int, height: Int) {
    }

    override fun onSurfaceTextureDestroyed(surface: SurfaceTexture?): Boolean {
        return false
    }

    override fun onSurfaceTextureUpdated(surface: SurfaceTexture?) {
    }

    private fun audioRecordCallBack(byteBuffer: ByteBuffer, size: Int, timeNs: Long) {
        mCallBack?.onAudioFrame(byteBuffer, size)
        if (isRecordVideo) {//如果录制视屏进行编码
            audioEncoder?.encodeOneFrame(Frame().apply {
                buffer = byteBuffer
                setBufferInfo(0, size, (timeNs + 500) / 1000, 0)
            })
        }
        if (isRecordAudio) {//如果录制音频，将音频进行存储

        }

    }

    /**
     * 相机回调数据
     */
    override fun onPreviewFrame(data: ByteArray?, camera: Camera?) {
        val bytes = data ?: return
        mCallBack?.let {
            it.onPreviewFrame(
                bytes,
                RecordeVideoConfig.previewWidth,
                RecordeVideoConfig.previewHeight
            )
        }
    }

    var mCallBack: IRecordVideoView.CallBack? = null
}