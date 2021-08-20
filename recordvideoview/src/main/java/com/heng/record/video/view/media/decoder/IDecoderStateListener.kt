package com.heng.record.video.view.media.decoder

import android.media.MediaCodec
import com.heng.record.video.view.media.decoder.BaseDecoder
import java.nio.ByteBuffer


/**
 * 解码状态回调接口
 *
 * @author Chen Xiaoping (562818444@qq.com)
 * @since LearningVideo
 * @version LearningVideo
 * @Datetime 2019-09-02 09:56
 *
 */
interface IDecoderStateListener {
    fun decoderPrepare(decodeJob: BaseDecoder?)
    fun decoderRunning(decodeJob: BaseDecoder?)
    fun decoderPause(decodeJob: BaseDecoder?)
    fun decodeOneFrame(
        decodeJob: BaseDecoder?,
        buffer: ByteBuffer,
        bufferInfo: MediaCodec.BufferInfo
    )
    fun decoderStop(decodeJob: BaseDecoder?)
    fun decoderError(decodeJob: BaseDecoder?)
}