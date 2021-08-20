package com.heng.record.video.view.media.decoder

import android.media.MediaFormat
import com.heng.record.video.view.media.decoder.DecodeState

interface IDecoder : Runnable {
    /**
     * 开始
     */
    fun start()

    /**
     * 恢复播放
     */
    fun resume()

    /**
     * 暂停
     */
    fun pause()

    /**
     * 停止
     */
    fun stop()


    /**
     * 跳转到指定位置
     */
    fun seekTo(pos: Long)

    /**
     * 获取解码器当前的状态
     */
    fun getCurState(): DecodeState

    /**
     * 获取视频宽
     */
    fun getWidth(): Int

    /**
     * 获取视频高
     */
    fun getHeight(): Int

    /**
     * 获取视频长度
     */
    fun getDuration(): Long

    /**
     * 当前帧时间，单位：ms
     */
    fun getCurTimeStamp(): Long

    /**
     * 获取视频旋转角度
     */
    fun getRotationAngle(): Int

    /**
     * 获取音视频对应的格式参数
     */
    fun getMediaFormat(): MediaFormat?

    /**
     * 获取音视频对应的媒体轨道
     */
    fun getTrack(): Int

    /**
     * 获取解码的文件路径
     */
    fun getFilePath(): String
}