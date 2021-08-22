package com.heng.record.video.view.media.encoder

import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.os.Build
import com.heng.record.video.view.utils.LogUtils
import com.heng.record.video.view.media.MMuxer
import java.nio.ByteBuffer

/**
 * 音频编码器
 */

private const val TAG = "AudioEncoder"
private const val compressionRatio =0.5f

class AudioEncoder(
    mMuxer: MMuxer,
    private val sampleRate: Int = 44100,//采样率
    private val channelCount: Int = 2,//声道数
    private val bitPerSample: Int = 16//每个采样占用的字节数
) : BaseEncoder(mMuxer) {
    override fun configEncoder(mediaCodec: MediaCodec) {
        val audioFormat = MediaFormat.createAudioFormat(encodeType(), sampleRate, channelCount)
        audioFormat.setInteger(
            MediaFormat.KEY_BIT_RATE,
            (sampleRate * channelCount * bitPerSample * compressionRatio).toInt()
        )
        audioFormat.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, 100 * 1024)
        audioFormat.setInteger(MediaFormat.KEY_CHANNEL_COUNT, channelCount)
        try {
            configEncoderWithCQ(mediaCodec, audioFormat)
        } catch (e: Exception) {
            e.printStackTrace()
            try {
                configEncoderWithVBR(mediaCodec, audioFormat)
            } catch (e: Exception) {
                e.printStackTrace()
                LogUtils.e(TAG, "配置音频编码器失败")
            }
        }
    }

    private fun configEncoderWithCQ(codec: MediaCodec, outputFormat: MediaFormat) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // 本部分手机不支持 BITRATE_MODE_CQ 模式，有可能会异常
            outputFormat.setInteger(
                MediaFormat.KEY_BITRATE_MODE,
                MediaCodecInfo.EncoderCapabilities.BITRATE_MODE_CQ
            )
        }
        codec.configure(outputFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
    }

    private fun configEncoderWithVBR(codec: MediaCodec, outputFormat: MediaFormat) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            outputFormat.setInteger(
                MediaFormat.KEY_BITRATE_MODE,
                MediaCodecInfo.EncoderCapabilities.BITRATE_MODE_VBR
            )
        }
        codec.configure(outputFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
    }

    override fun release(muxer: MMuxer) {
        muxer.releaseAudioTrack()
    }

    override fun writeData(
        muxer: MMuxer,
        byteBuffer: ByteBuffer,
        bufferInfo: MediaCodec.BufferInfo
    ) {
        muxer.writeAudioSampleData(byteBuffer, bufferInfo)
    }

    override fun addTrack(muxer: MMuxer, format: MediaFormat) {
        muxer.addAudioTrack(format)
    }
    //audio/mp4a-latm"
    override fun encodeType() = "audio/mp4a-latm"


}