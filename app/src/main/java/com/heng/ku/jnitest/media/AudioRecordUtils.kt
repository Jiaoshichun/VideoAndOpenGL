package com.heng.ku.jnitest.media

import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.SystemClock
import android.util.Log
import java.nio.ByteBuffer
import kotlin.properties.Delegates


private val TAG = "AudioRecordUtils"
private val AUDIO_SOURCES = intArrayOf(
    MediaRecorder.AudioSource.MIC,
    MediaRecorder.AudioSource.DEFAULT,
    MediaRecorder.AudioSource.CAMCORDER,
    MediaRecorder.AudioSource.VOICE_COMMUNICATION,
    MediaRecorder.AudioSource.VOICE_RECOGNITION
)

class AudioRecordUtils constructor(
    private val sampleRate: Int = 44100,//采样率
    private val channelCount: Int = 2,//声道数
    private val audioFormat: Int = AudioFormat.ENCODING_PCM_16BIT//编码格式
) {
    @Volatile
    private var isEOS = false //是否结束

    private var mAudioRecord: AudioRecord? = null
    private var mBufferSize by Delegates.notNull<Int>()
    private var mByteBuffer: ByteBuffer? = null
    @Volatile
    private var isRunning = false//是否正在运行
    //读取到数据后的回调  数据byffer 长度  时间(纳秒)
    private val mCallBackList = mutableListOf<(ByteBuffer, Int, Long) -> Unit>()

    private fun initAudioRecord(
        channelCount: Int,
        sampleRate: Int,
        audioFormat: Int
    ) {
        val channelConfig = if (channelCount == 1) {
            AudioFormat.CHANNEL_IN_MONO
        } else {
            AudioFormat.CHANNEL_IN_STEREO
        }
        mBufferSize =
            AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)
        if (mByteBuffer == null)
            mByteBuffer = ByteBuffer.allocateDirect(mBufferSize)
        for (source in AUDIO_SOURCES) {
            try {
                mAudioRecord = AudioRecord(
                    source, sampleRate,
                    channelConfig, AudioFormat.ENCODING_PCM_16BIT, mBufferSize
                )
                if (mAudioRecord!!.state != AudioRecord.STATE_INITIALIZED) {
                    mAudioRecord = null
                }
            } catch (e: Exception) {
                mAudioRecord = null
            }

            if (mAudioRecord != null) break
        }
        if (mAudioRecord == null) throw RuntimeException("create AudioRecord error")
    }

    fun start() {
        check(!isRunning) { "AudioRecord already start" }
        initAudioRecord(channelCount, sampleRate, audioFormat)
        isEOS = false
        isRunning = true
        AudioThread().start()
    }

    fun addCallBack(dataCallBack: ((ByteBuffer, Int, Long) -> Unit)) {
        mCallBackList.add(dataCallBack)
    }

    fun removeCallBack(dataCallBack: ((ByteBuffer, Int, Long) -> Unit)) {
        mCallBackList.remove(dataCallBack)
    }

    fun stop() {
        isEOS = true
        isRunning = false
    }


    private inner class AudioThread : Thread() {
        private var startTimeUs = 0L
        override fun run() {
            android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO)
            try {
                startTimeUs = SystemClock.elapsedRealtimeNanos()
                mAudioRecord!!.startRecording()
                while (!isEOS && isRunning) {
                    mByteBuffer!!.clear()
                    val size = mAudioRecord!!.read(mByteBuffer!!, mBufferSize)
                    mByteBuffer!!.position(size)
                    mByteBuffer!!.flip()
                    mCallBackList.forEach {
                        val l = SystemClock.elapsedRealtimeNanos() - startTimeUs
                        Log.d(TAG, "startTimeUs:$startTimeUs   duration->$l")
                        it.invoke(
                            mByteBuffer!!,
                            size,
                            l
                        )
                    }

                }

            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            } finally {
                mAudioRecord!!.stop()
                mAudioRecord!!.release()
                Log.d(TAG, "stop")
            }

        }

    }
}