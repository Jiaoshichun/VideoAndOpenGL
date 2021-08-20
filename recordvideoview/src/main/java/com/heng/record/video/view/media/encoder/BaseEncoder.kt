package com.heng.record.video.view.media.encoder

import android.media.MediaCodec
import android.media.MediaFormat
import android.util.Log
import com.heng.record.video.view.media.Frame
import com.heng.record.video.view.media.MMuxer
import java.nio.ByteBuffer
import java.util.*

/**
 * 编码器
 */


abstract class BaseEncoder(
    private val muxer: MMuxer,
    protected val width: Int = -1,
    protected val height: Int = -1
) : Runnable {

    private val TAG = this.javaClass.simpleName
    private val mLock = Object()
    private lateinit var mediaCodec: MediaCodec

    //是否结束
    private var isEos = false

    /**
     * 初始化编码器
     */
    private fun initCodec() {
        mediaCodec = MediaCodec.createEncoderByType(encodeType())
        bufferInfo = MediaCodec.BufferInfo()
        configEncoder(mediaCodec)
        mediaCodec.start()
    }

    // 编码帧序列
    private var mFrames = LinkedList<Frame>()
    // 当前编码帧信息
    private var bufferInfo = MediaCodec.BufferInfo()

    //    private var startTime = -1L
    override fun run() {
        isEos = false
//        startTime = -1L
        synchronized(mFrames) {
            mFrames.clear()
        }
        while (!isEos) {
            while (mFrames.isNotEmpty() && !isEos) {
                val frame = synchronized(mFrames) { mFrames.removeFirst() }
                when {
                    encodeManually() -> encode(frame)
//                    frame.buffer == null -> { // 如果是自动编码（比如视频），遇到结束帧的时候，直接结束掉
//                        Log.d(TAG, "发送编码结束标志")
//                        // This may only be used with encoders receiving input from a Surface
//                        mediaCodec.signalEndOfInputStream()
//                        isEos = true
//                    }
                }
                //取出数据
                drain()
            }
            if (!isEos) {
                waitEncode()
            }
        }
        if (!encodeManually()) {//如果是自动的 ，需要发送结束的标记
            mediaCodec.signalEndOfInputStream()
        } else {
            val dequeueInputBufferIndex = mediaCodec.dequeueInputBuffer(-1)
            mediaCodec.queueInputBuffer(
                dequeueInputBufferIndex,
                0,
                0,
                bufferInfo.presentationTimeUs,
                MediaCodec.BUFFER_FLAG_END_OF_STREAM
            )
        }
        done()
        Log.d(TAG, "编码完成")
    }

    /**
     * 编码结束，是否资源
     */
    private fun done() {
        try {
            Log.i(TAG, "release")
            mediaCodec.stop()
            mediaCodec.release()
            release(muxer)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    abstract fun release(muxer: MMuxer)

    private fun encode(frame: Frame) {
        val dequeueInputBufferIndex = mediaCodec.dequeueInputBuffer(-1)
        if (dequeueInputBufferIndex > -1) {
            val byteBuffer = mediaCodec.inputBuffers[dequeueInputBufferIndex] ?: return
            byteBuffer.clear()
            if (frame.buffer != null) {
                byteBuffer.put(frame.buffer)
            }
            if (frame.buffer == null || frame.bufferInfo.size <= 0) {
                isEos = true
            } else {
                frame.buffer?.flip()
                frame.buffer?.mark()
                mediaCodec.queueInputBuffer(
                    dequeueInputBufferIndex,
                    0,
                    frame.bufferInfo.size,
                    frame.bufferInfo.presentationTimeUs,
                    0
                )
            }
            frame.buffer?.clear()
        }
    }

    /**
     * 等待编码
     */
    protected fun waitEncode() {
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
    protected fun notifyEncode() {
        synchronized(mLock) {
            mLock.notifyAll()
        }
    }

    /**
     * 取出数据
     */
    private fun drain() {
        loop@ while (!isEos) {
            val outputIndex =
                mediaCodec.dequeueOutputBuffer(
                    bufferInfo, 1000
                )
            Log.d(
                TAG,
                "drain - time》${bufferInfo.presentationTimeUs} bufferInfo.flag:${bufferInfo.flags} index:$outputIndex"
            )
            when (outputIndex) {
                MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED -> continue@loop
                MediaCodec.INFO_TRY_AGAIN_LATER -> return
                MediaCodec.INFO_OUTPUT_FORMAT_CHANGED -> {
                    addTrack(muxer, mediaCodec.outputFormat)
                }
                else -> {
                    if (bufferInfo.flags == MediaCodec.BUFFER_FLAG_CODEC_CONFIG) {
                        // SPS or PPS, which should be passed by MediaFormat.
                        mediaCodec.releaseOutputBuffer(outputIndex, false)
                        continue@loop
                    }
                    if (!isEos) {
                        /** 通过设置 EGLExt.eglPresentationTimeANDROID(mEglDisplay, eglSurface, nsecs) 可以保证视频时间一致*/
//                      获取的视频时间不是从0开始  为了使音视频同步，将第一次编码的时间置为0，并记录开始时间，每次重置为时间差
//                        if (startTime == -1L) {
//                            startTime = bufferInfo.presentationTimeUs
//                            bufferInfo.presentationTimeUs = 0
//                        } else {
//                            bufferInfo.presentationTimeUs =
//                                bufferInfo.presentationTimeUs - startTime
//                        }
                        Log.d(
                            TAG,
                            "drain - bufferInfo.flags:》${bufferInfo.flags}  " +
                                    "bufferInfo.presentationTimeUs:${bufferInfo.presentationTimeUs}  " +
                                    "bufferInfo.size:${bufferInfo.size}  " +
                                    "bufferInfo.offset:${bufferInfo.offset} "
                        )
                        val outputBuffer = mediaCodec.outputBuffers[outputIndex] ?: return
                        writeData(muxer, outputBuffer, bufferInfo)
                    }
                    mediaCodec.releaseOutputBuffer(outputIndex, false)
                }
            }
        }
    }

    abstract fun writeData(muxer: MMuxer, byteBuffer: ByteBuffer, bufferInfo: MediaCodec.BufferInfo)

    abstract fun addTrack(muxer: MMuxer, format: MediaFormat)

    /**
     * 将一帧数据压入队列，等待编码
     */
    fun encodeOneFrame(frame: Frame) {
        if (isEos) return
        Log.d(TAG, "encodeOneFrame->$frame")
        synchronized(mFrames) {
            mFrames.addLast(frame)
        }
        notifyEncode()
    }

    /**
     * 编码类型
     */
    abstract fun encodeType(): String

    /**
     * 配置编码器
     */
    abstract fun configEncoder(mediaCodec: MediaCodec)

    /**
     * 是否手动编码
     * 视频：false 音频：true
     *
     * 注：视频编码通过Surface，MediaCodec自动完成编码；音频数据需要用户自己压入编码缓冲区，完成编码
     */
    open fun encodeManually() = true

    fun start() {
        initCodec()
        Thread(this).start()
    }

    fun stop() {
        isEos = true
        notifyEncode()
    }
}