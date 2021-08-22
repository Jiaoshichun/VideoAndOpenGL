package com.heng.record.video.view.utils

import android.media.AudioFormat
import android.media.AudioRecord
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.atomic.AtomicBoolean

private const val TAG = "WavFileUtils"

/**
 * wav文件工具类
 */
class WavFileUtils(private val audioRecordUtils: AudioRecordUtils) {
    private var isStart: AtomicBoolean = AtomicBoolean(false)
    private var wavFileName: String? = null
    private var wavTmpFileName: String? = null
    private var audioOutputStream: FileOutputStream? = null

    init {
        audioRecordUtils.addCallBack(::onAudioFrame)
    }

    /**
     * 是否正在写文件
     */
    @Volatile
    private var isWriting = false

    /**
     * 开始录制
     */
    fun start(audioFile: String) {
        val wavFile = File(audioFile)
        val name = wavFile.name
        val parentFile = wavFile.parentFile
        if (!parentFile.exists() || !parentFile.isDirectory) {
            if (!parentFile.mkdirs()) {
                Log.e(TAG, "设置音频文件路径有问题,录制失败  $audioFile")
                return
            }
        }
        wavFileName = audioFile
        wavTmpFileName = File(parentFile, "$name.tmp").absolutePath
        audioOutputStream = FileOutputStream(wavTmpFileName)
        isStart.set(true)
        audioRecordUtils.start()
    }

    /**
     * 音频帧回调
     */
    private fun onAudioFrame(data: ByteArray, size: Int, timeNs: Long) {
        isWriting = true
        if (!isStart.get()) {
            isWriting = false
            return
        }
        audioOutputStream?.write(data, 0, size)
        isWriting = false
    }

    /**
     * 停止录制
     */
    fun stop() {
        if (!isStart.compareAndSet(true, false)) {
            return
        }
        audioRecordUtils.stop()
        while (isWriting) {
            Log.i(TAG, "stop   正在写数据中...")
        }
        val outPutStream = audioOutputStream ?: return
        audioOutputStream = null
        kotlin.runCatching {
            outPutStream.close()
        }

        val channelConfig = if (audioRecordUtils.channelCount == 1) {
            AudioFormat.CHANNEL_IN_MONO
        } else {
            AudioFormat.CHANNEL_IN_STEREO
        }
        val bitsPerSample =
            if (audioRecordUtils.audioFormat == AudioFormat.ENCODING_PCM_16BIT) 16 else 8
        val recBufSize =
            AudioRecord.getMinBufferSize(
                audioRecordUtils.sampleRate,
                channelConfig,
                audioRecordUtils.audioFormat
            )
        //添加wav头信息
        WavHeadUtils.copyWaveFile(
            wavTmpFileName,
            wavFileName,
            audioRecordUtils.sampleRate.toLong(),
            bitsPerSample,
            audioRecordUtils.channelCount,
            recBufSize
        )
        kotlin.runCatching {
            File(wavTmpFileName).delete()
        }
    }

}