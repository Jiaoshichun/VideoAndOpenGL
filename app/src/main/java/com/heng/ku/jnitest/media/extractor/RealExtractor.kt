package com.heng.ku.jnitest.media.extractor


import android.media.MediaExtractor
import android.media.MediaFormat
import java.nio.ByteBuffer

/**
 * 音视频抽离器
 * 正真实现音视频抽离的类
 */
class RealExtractor(path: String, isAudio: Boolean) {
    private val mExtractor = MediaExtractor()
    /**通道索引*/
    var mTrack = -1
        private set

    /**当前帧时间戳*/
    var mCurSampleTime: Long = 0
        private set
    //当前的标识
    var mCurSampleFlags: Int = 0
        private set
    var mFormat: MediaFormat? = null
        private set

    init {
        mExtractor.setDataSource(path)
        for (i in 0 until mExtractor.trackCount) {
            val trackFormat = mExtractor.getTrackFormat(i)
            val mime = trackFormat.getString(MediaFormat.KEY_MIME)
            if (isAudio && mime.startsWith("audio/")) {
                mTrack = i
                mFormat = trackFormat
                break
            } else if (!isAudio && mime.startsWith("video/")) {
                mTrack = i
                mFormat = trackFormat
                break
            }
        }
        mExtractor.selectTrack(mTrack)
    }

    /**
     * 读取样本数据
     * @param byteBuf 要读取数据字节
     * @param isAudio  true 音频抽取 false 视频抽取
     */
    fun readSampleData(byteBuf: ByteBuffer): Int {
        byteBuf.clear()

        val readSampleData = mExtractor.readSampleData(byteBuf, 0)
        if (readSampleData < 0) {
            return -1
        }
        mCurSampleTime = mExtractor.sampleTime
        mCurSampleFlags = mExtractor.sampleFlags
        mExtractor.advance()
        return readSampleData
    }

    /**
     * 跳转到指定时间  并返回真正的 时间
     */
    fun seekTo(timeUs: Long): Long {
        val lastTime = mExtractor.sampleTime
        mExtractor.seekTo(timeUs * 1000, MediaExtractor.SEEK_TO_PREVIOUS_SYNC)
        if (lastTime < mExtractor.sampleTime) {
            mExtractor.advance()
        }
        return mExtractor.sampleTime / 1000
    }

    /**
     * 停止
     */
    fun stop() {
        mExtractor.release()
    }
     fun getWidth(): Int {
        return mFormat?.getInteger(MediaFormat.KEY_WIDTH) ?: 0
    }

     fun getHeight(): Int {
        return mFormat?.getInteger(MediaFormat.KEY_HEIGHT) ?: 0
    }

}