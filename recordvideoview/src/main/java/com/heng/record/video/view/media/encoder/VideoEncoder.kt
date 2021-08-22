package com.heng.record.video.view.media.encoder

import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaCodecInfo.CodecCapabilities
import android.media.MediaFormat
import android.os.Build
import android.view.Surface
import com.heng.record.video.view.utils.LogUtils
import com.heng.record.video.view.media.MMuxer
import java.nio.ByteBuffer
import android.media.MediaCodecList
import java.util.*


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

    override fun encodeType() = "video/avc"

    override fun configEncoder(mediaCodec: MediaCodec) {
        require(!(width <= 0 || height <= 0)) { "Encode width or height is invalid, width: $width, height: $height" }
        val bitrate = 3 * width * height * frameRate
        val outputFormat = MediaFormat.createVideoFormat(encodeType(), width, height)
        outputFormat.setInteger(MediaFormat.KEY_BIT_RATE, calcBitRate())
        outputFormat.setInteger(MediaFormat.KEY_FRAME_RATE, frameRate)
        outputFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 2)
        outputFormat.setInteger(
            MediaFormat.KEY_COLOR_FORMAT,
            MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface
        )
        try {
//            configEncoderWithCQ(mediaCodec, outputFormat)
//        } catch (e: Exception) {
////            e.printStackTrace()
//            // 捕获异常，设置为系统默认配置 BITRATE_MODE_VBR
//            try {
                configEncoderWithVBR(mediaCodec, outputFormat)
            } catch (e: Exception) {
//                e.printStackTrace()
                LogUtils.e(TAG, "配置视频编码器失败")
//            }
        }
        mSurface = mediaCodec.createInputSurface()
    }
    private fun calcBitRate(): Int {
        val bitrate = (BPP * frameRate.toFloat() * width.toFloat() * height.toFloat()).toInt()
        LogUtils.i(TAG, String.format("bitrate=%5.2f[Mbps]", bitrate.toFloat() / 1024f / 1024f))
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
        LogUtils.d(TAG, "configEncoderWithCQ")
        codec.configure(outputFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
    }

    private fun configEncoderWithVBR(codec: MediaCodec, outputFormat: MediaFormat) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            outputFormat.setInteger(
                MediaFormat.KEY_BITRATE_MODE,
                MediaCodecInfo.EncoderCapabilities.BITRATE_MODE_VBR
            )
        }
        LogUtils.d(TAG, "configEncoderWithVBR")
        codec.configure(outputFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
    }

    override fun encodeManually() = false
    override fun release(muxer: MMuxer) {
        muxer.releaseVideoTrack()
    }

    /**
     * 参考：https://www.jianshu.com/p/4491f0f906e6
     */
    private fun getSupportColorFormat(): Int {
        val numCodecs = MediaCodecList.getCodecCount()
        var codecInfo: MediaCodecInfo? = null
        run {
            var i = 0
            while (i < numCodecs && codecInfo == null) {
                val info = MediaCodecList.getCodecInfoAt(i)
                if (!info.isEncoder) {
                    i++
                    continue
                }
                val types = info.supportedTypes
                var found = false
                var j = 0
                while (j < types.size && !found) {
                    if (types[j] == "video/avc") {
                        LogUtils.d(TAG, "found")
                        found = true
                    }
                    j++
                }
                if (!found) {
                    i++
                    continue
                }
                codecInfo = info
                i++
            }
        }
        LogUtils.e("AvcEncoder", "Found " + codecInfo!!.name + " supporting " + "video/avc")
        // Find a color profile that the codec supports
        val capabilities = codecInfo!!.getCapabilitiesForType("video/avc")
        LogUtils.e(
            "AvcEncoder",
            "length-" + capabilities.colorFormats.size + "==" + Arrays.toString(capabilities.colorFormats)
        )
        for (i in capabilities.colorFormats.indices) {
            LogUtils.d(TAG, "MediaCodecInfo COLOR FORMAT :" + capabilities.colorFormats[i])
            if (capabilities.colorFormats[i] == CodecCapabilities.COLOR_FormatYUV420SemiPlanar || capabilities.colorFormats[i] == CodecCapabilities.COLOR_FormatYUV420Planar) {
                return capabilities.colorFormats[i]
            }
        }
        return CodecCapabilities.COLOR_FormatYUV420Flexible
    }
}