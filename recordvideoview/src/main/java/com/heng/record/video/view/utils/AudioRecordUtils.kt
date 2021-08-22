package com.heng.record.video.view.utils

import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.SystemClock
import com.heng.record.video.view.utils.LogUtils
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
     val sampleRate: Int = 44100,//采样率
     val channelCount: Int = 2,//声道数
     val audioFormat: Int = AudioFormat.ENCODING_PCM_16BIT//编码格式
) {
    @Volatile
    private var isEOS = false //是否结束

    private var mAudioRecord: AudioRecord? = null
    private var mBufferSize by Delegates.notNull<Int>()
    private var mByteArrays: ByteArray? = null
    @Volatile
    private var isRunning = false//是否正在运行
    //读取到数据后的回调  数据byffer 长度  时间(纳秒)
    private val mCallBackList = mutableListOf<(ByteArray, Int, Long) -> Unit>()

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
        if(mBufferSize<0){
            throw IllegalArgumentException("语音录制配置参数有误：$mBufferSize " +
                    "sampleRate:$sampleRate channelConfig:$channelConfig audioFormat:$audioFormat" +
                    "  channelCount:$channelCount")
        }
        if (mByteArrays == null)
            mByteArrays = ByteArray(mBufferSize)
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

    fun addCallBack(dataCallBack: ((ByteArray, Int, Long) -> Unit)) {
        mCallBackList.add(dataCallBack)
    }

    fun removeCallBack(dataCallBack: ((ByteArray, Int, Long) -> Unit)) {
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
                    val size = mAudioRecord!!.read(mByteArrays!!, 0,mBufferSize)
                    mCallBackList.forEach {
                        val l = SystemClock.elapsedRealtimeNanos() - startTimeUs
                        LogUtils.d(TAG, "startTimeUs:$startTimeUs   duration->$l")
                        it.invoke(
                            mByteArrays!!.copyOf(),
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
                mAudioRecord=null
                LogUtils.d(TAG, "stop")
            }

        }

    }
}