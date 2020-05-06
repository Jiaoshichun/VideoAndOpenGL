package com.heng.ku.jnitest.media.decoder

import android.media.MediaCodec
import java.nio.ByteBuffer


/**
 * 默认解码状态监听器
 *
 * @author Chen Xiaoping (562818444@qq.com)
 * @since LearningVideo
 * @version LearningVideo
 *
 */
interface DefDecodeStateListener: IDecoderStateListener {
    override fun decoderPrepare(decodeJob: BaseDecoder?) {}
    override fun decoderRunning(decodeJob: BaseDecoder?) {}
    override fun decoderPause(decodeJob: BaseDecoder?) {}
    override fun decodeOneFrame(decodeJob: BaseDecoder?, buffer: ByteBuffer,bufferInfo: MediaCodec.BufferInfo) {}
    override fun decoderStop(decodeJob: BaseDecoder?) {}
    override fun decoderError(decodeJob: BaseDecoder?) {}
}