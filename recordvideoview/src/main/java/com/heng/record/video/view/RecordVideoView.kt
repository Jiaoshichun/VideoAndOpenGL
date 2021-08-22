package com.heng.record.video.view

import android.content.Context
import android.graphics.ImageFormat
import android.graphics.SurfaceTexture
import android.hardware.Camera
import android.util.AttributeSet
import android.view.Surface
import android.view.TextureView
import android.widget.FrameLayout
import com.heng.record.video.view.media.Frame
import com.heng.record.video.view.media.MMuxer
import com.heng.record.video.view.media.encoder.AudioEncoder
import com.heng.record.video.view.media.encoder.VideoEncoder
import com.heng.record.video.view.opengl.EGLRenderPro
import com.heng.record.video.view.opengl.drawer.VideoDrawer
import com.heng.record.video.view.utils.AudioRecordUtils
import com.heng.record.video.view.utils.WavFileUtils

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

    /**
     * wav录制工具类
     */
    private var wavFileUtils = WavFileUtils(audioRecordUtils)
    private val texture = SurfaceTexture(EGLRenderPro.TEXTURE_ID).apply {
        setOnFrameAvailableListener {
            if (isRecordVideo) {
                videoEncoder?.encodeOneFrame(Frame())
            }
        }
    }

    /**
     * 初始化视频绘制
     */
    private val videoDrawer = VideoDrawer().apply {
        eglRender.addDrawer(this)
        setTexture(texture)
    }

    /**
     * 开始预览
     * 开启相机
     */
    override fun startPreview(cameraId: Int) {
        stopPreview()
        startCamera(texture, cameraId)
    }

    /**
     * 打开相机
     */
    private fun startCamera(it: SurfaceTexture, cameraId: Int) {
        setVideoDrawerRotation(cameraId)
        camera = Camera.open(cameraId)
        val params = camera!!.parameters
        params.previewFormat = ImageFormat.NV21;

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

    /**
     * 设置drawer旋转角度
     */
    private fun setVideoDrawerRotation(cameraId: Int) {
        videoDrawer.setVideoWh(
            RecordeVideoConfig.previewWidth,
            RecordeVideoConfig.previewHeight
        )

        if (RecordeVideoConfig.videoOrientation == RecordeVideoConfig.VIDEO_ORIENTATION_PORTRAIT) {
            //竖屏前置旋转270度 后置旋转90度
            videoDrawer.setRotationAngle(if (cameraId == Camera.CameraInfo.CAMERA_FACING_BACK) 90 else 270)
        }

    }

    /**
     * 录制视频
     * 开始时会先停止录制视频和音频
     */
    override fun startRecordVideo(videoPath: String) {
        stopRecordVideo()
        stopRecordAudio()
        if (camera == null) {
            //相机未打开 不能录制

        }
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

    /**
     * 停止录制视频
     */
    override fun stopRecordVideo() {

        isRecordVideo = false
        isRecordAudio = false
        audioRecordUtils.stop()
        videoEncoder?.stop()
        audioEncoder?.stop()
        eglRender.isStart = false
        videoEncoder?.mSurface?.let {
            eglRender.removeSurface(it)
        }
        videoEncoder = null
        audioEncoder = null

    }

    /**
     * 开始录制音频
     * 开始时会先停止录制视频和音频
     */
    override fun startRecordAudio(audioPath: String) {
        stopRecordVideo()
        stopRecordAudio()
        isRecordVideo = false
        isRecordAudio = true
        wavFileUtils.start(audioPath)

    }

    /**
     * 停止录制音频
     */
    override fun stopRecordAudio() {
        isRecordVideo = false
        isRecordAudio = false
        wavFileUtils.stop()
    }

    /**
     * 停止预览
     */
    override fun stopPreview() {
        isRecordVideo = false
        isRecordAudio = false
        camera?.stopPreview()
        camera?.setPreviewCallback(null)
        camera?.release()
        camera = null
    }

    /**
     * 设置回调
     */
    override fun setCallBack(callBack: IRecordVideoView.CallBack) {
        this.mCallBack = callBack
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

    /**
     * AudioRecordUtils音频回调
     * 如果录制视频，进行音频编码
     * 如果录制音频，通过WavFileUtils存储音频
     */
    private fun audioRecordCallBack(byteArray: ByteArray, size: Int, timeNs: Long) {
        mCallBack?.onAudioFrame(byteArray, size)
        if (isRecordVideo) {//如果录制视屏进行编码
            audioEncoder?.encodeOneFrame(Frame().apply {
                buffer = byteArray
                setBufferInfo(0, size, (timeNs + 500) / 1000, 0)
            })
        }

    }

    /**
     * 相机回调数据
     */
    override fun onPreviewFrame(data: ByteArray?, camera: Camera?) {
        val bytes = data ?: return
        mCallBack?.onPreviewFrame(
            bytes,
            RecordeVideoConfig.previewWidth,
            RecordeVideoConfig.previewHeight
        )
    }

    private var mCallBack: IRecordVideoView.CallBack? = null
}