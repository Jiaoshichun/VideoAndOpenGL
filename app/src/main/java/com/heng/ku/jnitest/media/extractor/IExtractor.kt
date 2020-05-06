package com.heng.ku.jnitest.media.extractor

import android.media.MediaFormat
import java.nio.ByteBuffer

/**
 * 音视频分离器
 */
interface IExtractor {
    fun getFormat(): MediaFormat?

    /**
     * 读取音视频数据
     */
    fun readBuffer(byteBuffer: ByteBuffer): Int

    /**
     * 获取当前帧时间
     */
    fun getCurrentTimestamp(): Long

    fun getSampleFlag(): Int

    /**
     * Seek到指定位置，并返回实际帧的时间戳
     */
    fun seek(pos: Long): Long


    /**
     * 停止读取数据
     */
    fun stop()

    /**
     * 获取轨道索引
     */
    fun getTrack(): Int

    fun getRotationAngle(): Int
}