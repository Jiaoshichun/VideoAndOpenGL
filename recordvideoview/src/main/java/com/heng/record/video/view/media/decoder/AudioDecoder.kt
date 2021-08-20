package com.heng.record.video.view.media.decoder

import android.media.*
import com.heng.record.video.view.media.extractor.AudioExtractor
import java.nio.ByteBuffer

/**
 * 音频解码器
 */
class AudioDecoder(mFilePath: String) : BaseDecoder(mFilePath) {

    private var mChannelCount = 1
    private var mSampleRateInHz = 0
    private var mAudioFormat = 0
    /**音频数据缓存*/
    private var mAudioOutTempBuf: ByteArray? = null
    private var mAudioTrack: AudioTrack? = null
    override fun initRender(): Boolean {
        val channel = if (mChannelCount == 1) {
            AudioFormat.CHANNEL_OUT_MONO
        } else {
            AudioFormat.CHANNEL_OUT_STEREO
        }
        val bufferSize = AudioTrack.getMinBufferSize(mSampleRateInHz, channel, mAudioFormat)
        mAudioOutTempBuf = ByteArray(bufferSize)
        mAudioTrack = AudioTrack(
            AudioManager.STREAM_MUSIC,
            mSampleRateInHz,
            channel,
            mAudioFormat,
            bufferSize,
            AudioTrack.MODE_STREAM
        )
        mAudioTrack!!.play()
        return true
    }

    override fun render(outputBuffer: ByteBuffer, bufferInfo: MediaCodec.BufferInfo) {
        if (mAudioOutTempBuf!!.size < bufferInfo.size) {
            mAudioOutTempBuf = ByteArray(bufferInfo.size)
        }
        outputBuffer.position(0)
        outputBuffer.get(mAudioOutTempBuf, 0, bufferInfo.size)
        mAudioTrack?.write(mAudioOutTempBuf!!, 0, bufferInfo.size)
    }

    override fun initExtractor(path: String) = AudioExtractor(path)

    override fun configure(codec: MediaCodec, mediaFormat: MediaFormat): Boolean {
        codec.configure(mediaFormat, null, null, 0)
        return true
    }

    override fun initSpecParams(format: MediaFormat) {
        mChannelCount = format.getInteger(MediaFormat.KEY_CHANNEL_COUNT)
        mSampleRateInHz = format.getInteger(MediaFormat.KEY_SAMPLE_RATE)
        mAudioFormat = if (format.containsKey(MediaFormat.KEY_PCM_ENCODING)) {
            format.getInteger(MediaFormat.KEY_PCM_ENCODING)
        } else {
            //如果没有这个参数，默认为16位采样
            AudioFormat.ENCODING_PCM_16BIT
        }
    }

    override fun doneDecode() {
        try {
            mAudioTrack?.stop()
            mAudioTrack?.release()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}