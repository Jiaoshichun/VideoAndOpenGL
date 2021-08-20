package com.heng.record.video.view.media.decoder

import android.media.MediaCodec
import android.media.MediaFormat
import android.os.SystemClock
import android.util.Log
import com.heng.record.video.view.media.extractor.IExtractor
import java.nio.ByteBuffer

/**
 * 1.返回可填充有效数据的输入缓冲区的索引
 * 2.向输入缓冲区 输入音频/视频数据
 * 3.将带有音频/视频数据的输入缓冲区 推送解码队列
 * 4.获取解码后输出缓冲数据
 * 5.音频/视频 与实际流失时间同步
 * 6.渲染输出数据
 * 7.释放缓冲区 返回到编码器  必须要做
 *
 * 注意： 1.Extractor 必须在selectTrack()之后调用 readSampleData  一定要selectTrack对
 *        2. mBufferInfo.presentationTimeUs 是微秒
 */
abstract class BaseDecoder(private val mFilePath: String) : IDecoder {
    private val TAG = javaClass.simpleName
    private var mExtractor: IExtractor? = null


    // 是否需要音视频渲染同步
    var isSyncRender = true
    private var mCurDecodeState = DecodeState.UNINIT
        set(value) {
            Log.d(TAG, value.name)
            field = value
            when (value) {
                DecodeState.PREPARE -> mStateListener?.decoderPrepare(this)
                DecodeState.DECODING -> mStateListener?.decoderRunning(this)
                DecodeState.ERROR -> mStateListener?.decoderError(this)
                DecodeState.STOP -> mStateListener?.decoderStop(this)
                DecodeState.PAUSE -> mStateListener?.decoderPause(this)
                else -> {
                }
            }
        }
    private var mCodec: MediaCodec? = null
    private var mDuration: Long = 0L
    private val mLock = Object()
    @Volatile
    private var mSeekPos = -1L
    private var isRunning = false

    // 编码状态监听器
    var mStateListener: IDecoderStateListener? = null

    /**
     * 解码数据信息
     */
    private var mBufferInfo = MediaCodec.BufferInfo()
    private var mStartTime = -1L
    override fun run() {
        if (!isRunning) {
            waitDecode()
        }
        mCurDecodeState = DecodeState.PREPARE
        if (!init()) {
            mCurDecodeState = DecodeState.ERROR
            return
        }
        mCurDecodeState = DecodeState.DECODING
        var isEos = false
        try {

            while (isRunning) {
                if (mStartTime == -1L) {
                    mStartTime = SystemClock.elapsedRealtime()
                }
                if (mCurDecodeState == DecodeState.PAUSE) {
                    waitDecode()
                    mStartTime = SystemClock.elapsedRealtime() - getCurTimeStamp()
                }
                /** 1. 返回可填充有效数据的输入缓冲区的索引 */
                val inputBufferIndex = mCodec!!.dequeueInputBuffer(1000)
                if (inputBufferIndex > -1) {
                    if (mSeekPos != -1L) {
                        val seek = mExtractor!!.seek(mSeekPos)
                        mSeekPos = -1L
                        mStartTime = SystemClock.elapsedRealtime() - seek
                    }
                    /** 2.向输入缓冲区 输入音频/视频数据 */
                    val size = mExtractor!!.readBuffer(mCodec!!.inputBuffers[inputBufferIndex])
                    if (size >= 0) {
                        /** 3. 将带有音频/视频数据的输入缓冲区 推送解码队列 */
                        mCodec!!.queueInputBuffer(
                            inputBufferIndex,
                            0,
                            size,
                            mExtractor!!.getCurrentTimestamp(),
                            mExtractor!!.getSampleFlag()
                        )
                    } else {
                        // 如果无更多音频/视频数据 设置  BUFFER_FLAG_END_OF_STREAM flag
                        mCodec!!.queueInputBuffer(inputBufferIndex, 0, 0,  mExtractor!!.getCurrentTimestamp(), MediaCodec.BUFFER_FLAG_END_OF_STREAM)
                        isEos = true
                    }
                }
                /** 4.获取解码后输出缓冲数据  */
                val outputBufferIndex = mCodec!!.dequeueOutputBuffer(mBufferInfo, 1000)
                if (outputBufferIndex > -1) {
                    /*** 5. 音频/视频 与实际流失时间同步  */
                    syncTime()
                    if (isSyncRender) {
                        /** 6.渲染输出数据 */
                        render(mCodec!!.outputBuffers[outputBufferIndex], mBufferInfo)
                    }
                    Log.d(TAG, "mBufferInfo.presentationTimeUs->${mBufferInfo.presentationTimeUs}")
                    mStateListener?.decodeOneFrame(
                        this,
                        mCodec!!.outputBuffers[outputBufferIndex],
                        mBufferInfo
                    )
                    /** 7.释放缓冲区 返回到编码器  必须要做 */
                    mCodec!!.releaseOutputBuffer(outputBufferIndex, true)
                }
                if (isEos) {
                    mCurDecodeState = DecodeState.STOP
                    break
                }
            }

        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            doneDecode()
            release()
        }
    }

    private fun release() {
        try {
            mCurDecodeState = DecodeState.STOP
            mCodec?.stop()
            mCodec?.release()
            mExtractor?.stop()
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    abstract fun doneDecode()

    /**
     * 同步时间 避免视频/音频播放时间过快
     */
    private fun syncTime() {
        val realDuration = SystemClock.elapsedRealtime() - mStartTime
        if (realDuration < getCurTimeStamp()) {
            try {
                synchronized(mLock) {
                    mLock.wait(getCurTimeStamp() - realDuration)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }


    private fun init(): Boolean {
        //初始化音视频分离器
        if (mExtractor!!.getFormat() == null) {
            Log.e(TAG, "mExtractor!!.getFormat()==null")
            return false
        }
        initParams()
        if (isSyncRender) {
            if (!initRender()) return false
        }
        if (!initCodec()) return false
        return true

    }

    /**
     * 初始化编码器
     */
    private fun initCodec(): Boolean {
        try {
            //初始化解码器
            mCodec =
                MediaCodec.createDecoderByType(mExtractor!!.getFormat()!!.getString(MediaFormat.KEY_MIME))
            if (!configure(mCodec!!, mExtractor!!.getFormat()!!)) {//如果surfaceView的surface未创建好 则等待
                waitDecode()
            }
            mCodec!!.start()
        } catch (e: Exception) {
            return false
        }

        return true
    }

    protected fun waitDecode() {
        try {
            synchronized(mLock) {
                mLock.wait()
            }
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }

    /**
     * 通知解码线程继续运行
     */
    protected fun notifyDecode() {
        synchronized(mLock) {
            mLock.notifyAll()
        }
    }

    /**
     * 初始化渲染器
     */
    abstract fun initRender(): Boolean

    /**
     * 渲染
     */
    abstract fun render(
        outputBuffer: ByteBuffer,
        bufferInfo: MediaCodec.BufferInfo
    )

    private fun initParams() {
        val mediaFormat = mExtractor!!.getFormat()!!
        mDuration = mediaFormat.getLong(MediaFormat.KEY_DURATION)
        initSpecParams(mediaFormat)
    }

    override fun start() {
        mExtractor = initExtractor(mFilePath)
        Thread(this).start()
        isRunning = true
        notifyDecode()
    }

    override fun resume() {
        if (mCurDecodeState == DecodeState.PAUSE) {
            mCurDecodeState = DecodeState.DECODING
        }
        notifyDecode()
    }

    override fun stop() {
        mCurDecodeState = DecodeState.STOP
        isRunning = false
    }

    override fun pause() {
        mCurDecodeState = DecodeState.PAUSE
    }

    override fun getMediaFormat(): MediaFormat? {
        return mExtractor?.getFormat()
    }

    override fun getTrack(): Int {
        return mExtractor?.getTrack() ?: -1
    }

    override fun getCurState() = mCurDecodeState
    override fun getFilePath() = mFilePath
    override fun getCurTimeStamp() = mBufferInfo.presentationTimeUs / 1000
    /**
     * 初始化分离器
     */
    abstract fun initExtractor(path: String): IExtractor

    /**
     * 配置解码器
     */
    abstract fun configure(codec: MediaCodec, mediaFormat: MediaFormat): Boolean

    /**
     * 初始化子类自己特有的参数
     */
    abstract fun initSpecParams(format: MediaFormat)

    override fun getDuration() = mDuration


    override fun seekTo(pos: Long) {
        mSeekPos = pos
    }

    override fun getWidth(): Int {
        return mExtractor?.getFormat()?.getInteger(MediaFormat.KEY_WIDTH) ?: 0
    }

    override fun getHeight(): Int {
        return mExtractor?.getFormat()?.getInteger(MediaFormat.KEY_HEIGHT) ?: 0
    }

    override fun getRotationAngle(): Int {
        return mExtractor?.getRotationAngle() ?: 0
    }
}