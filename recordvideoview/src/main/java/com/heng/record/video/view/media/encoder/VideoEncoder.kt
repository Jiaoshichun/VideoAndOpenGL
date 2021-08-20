package com.heng.record.video.view.media.encoder

import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.os.Build
import android.util.Log
import android.view.Surface
import com.heng.record.video.view.media.MMuxer
import java.nio.ByteBuffer

private const val TAG = "VideoEncoder"
private val BPP = 0.11f
/**
 * 视频编码器
 */
class VideoEncoder(mMuxer: MMuxer, width: Int, height: Int, private val frameRate: Int = 15) :
    BaseEncoder(mMuxer, width, height) {
    var mSurface: Surface? = null
        private set

    override fun writeData(
        muxer: MMuxer,
        byteBuffer: ByteBuffer,
        bufferInfo: MediaCodec.BufferInfo
    ) {
        muxer.writeVideoSampleData(byteBuffer, bufferInfo)
    }

    override fun addTrack(muxer: MMuxer, format: MediaFormat) {
        muxer.addVideoTrack(format)
    }

    override fun encodeType() = MediaFormat.MIMETYPE_VIDEO_AVC

    override fun configEncoder(mediaCodec: MediaCodec) {
        require(!(width <= 0 || height <= 0)) { "Encode width or height is invalid, width: $width, height: $height" }
        val bitrate = 3 * width * height * frameRate
        val outputFormat = MediaFormat.createVideoFormat(encodeType(), width, height)
        outputFormat.setInteger(MediaFormat.KEY_BIT_RATE, calcBitRate())
        outputFormat.setInteger(MediaFormat.KEY_FRAME_RATE, frameRate)
        outputFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 6)
        outputFormat.setInteger(
            MediaFormat.KEY_COLOR_FORMAT,
            MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface
        )
        try {
            configEncoderWithCQ(mediaCodec, outputFormat)
        } catch (e: Exception) {
//            e.printStackTrace()
            // 捕获异常，设置为系统默认配置 BITRATE_MODE_VBR
            try {
                configEncoderWithVBR(mediaCodec, outputFormat)
            } catch (e: Exception) {
//                e.printStackTrace()
                Log.e(TAG, "配置视频编码器失败")
            }
        }
        mSurface = mediaCodec.createInputSurface()
    }
    private fun calcBitRate(): Int {
        val bitrate = (BPP * frameRate.toFloat() * width.toFloat() * height.toFloat()).toInt()
        Log.i(TAG, String.format("bitrate=%5.2f[Mbps]", bitrate.toFloat() / 1024f / 1024f))
        return bitrate
    }
    private fun configEncoderWithCQ(codec: MediaCodec, outputFormat: MediaFormat) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // 本部分手机不支持 BITRATE_MODE_CQ 模式，有可能会异常
            outputFormat.setInteger(
                MediaFormat.KEY_BITRATE_MODE,
                MediaCodecInfo.EncoderCapabilities.BITRATE_MODE_CQ
            )
        }
        Log.d(TAG, "configEncoderWithCQ")
        codec.configure(outputFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
    }

    private fun configEncoderWithVBR(codec: MediaCodec, outputFormat: MediaFormat) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            outputFormat.setInteger(
                MediaFormat.KEY_BITRATE_MODE,
                MediaCodecInfo.EncoderCapabilities.BITRATE_MODE_VBR
            )
        }
        Log.d(TAG, "configEncoderWithVBR")
        codec.configure(outputFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
    }

    override fun encodeManually() = false
    override fun release(muxer: MMuxer) {
        muxer.releaseVideoTrack()
    }

}