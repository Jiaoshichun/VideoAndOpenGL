package com.heng.record.video.view.media

import android.media.MediaCodec
import java.nio.ByteBuffer


/**
 * 一帧数据
 *
 * @author Chen Xiaoping (562818444@qq.com)
 * @since LearningVideo
 * @version LearningVideo
 * @Datetime 2019-12-15 19:28
 *
 */
class Frame {
    var buffer: ByteArray? = null

    var bufferInfo = MediaCodec.BufferInfo()
        private set

    fun setBufferInfo(info: MediaCodec.BufferInfo) {
        bufferInfo.set(info.offset, info.size, info.presentationTimeUs, info.flags)
    }

    fun setBufferInfo(offset: Int, size: Int, presentationTimeUs: Long, flags: Int) {
        bufferInfo.set(offset, size, presentationTimeUs, flags)
    }

    override fun toString(): String {
        return "Frame(buffer=$buffer, bufferInfo.flags=${bufferInfo.flags},bufferInfo.presentationTimeUs=${bufferInfo.presentationTimeUs})" +
                ",bufferInfo.size=${bufferInfo.size},bufferInfo.offset=${bufferInfo.offset}"
    }

}