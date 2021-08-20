package com.heng.record.video.view.media.decoder

import android.media.MediaCodec
import android.media.MediaFormat
import android.view.Surface
import android.view.SurfaceHolder
import android.view.SurfaceView
import com.heng.record.video.view.media.extractor.VideoExtractor
import java.nio.ByteBuffer

/**
 * 视频解码器
 */
class VideoDecoder private constructor(private val mFilePath: String) : BaseDecoder(mFilePath) {

    constructor(mFilePath: String, surface: Surface) : this(mFilePath) {
        mSurface = surface
    }

    constructor(mFilePath: String, surfaceView: SurfaceView) : this(mFilePath) {
        mSurfaceView = surfaceView
    }

    private var mSurfaceView: SurfaceView? = null
    private var mSurface: Surface? = null

    override fun initRender() = true

    override fun render(outputBuffer: ByteBuffer, bufferInfo: MediaCodec.BufferInfo) {
    }

    override fun initExtractor(path: String) = VideoExtractor(mFilePath)

    override fun configure(codec: MediaCodec, mediaFormat: MediaFormat): Boolean {

        if (mSurface == null) { //设置了 surfaceView
            if (mSurfaceView!!.holder.surface?.isValid == true) {
                //如果surface已创建成功 则进行相关配置
                mSurface = mSurfaceView!!.holder.surface
                codec.configure(mediaFormat, mSurface, null, 0)
                notifyDecode()
                return true
            }
            mSurfaceView!!.holder.addCallback(object : SurfaceHolder.Callback2 {
                override fun surfaceRedrawNeeded(holder: SurfaceHolder?) {

                }

                override fun surfaceChanged(
                    holder: SurfaceHolder?,
                    format: Int,
                    width: Int,
                    height: Int
                ) {
                }

                override fun surfaceDestroyed(holder: SurfaceHolder?) {
                }

                override fun surfaceCreated(holder: SurfaceHolder?) {
                    mSurface = holder!!.surface
                    configure(codec, mediaFormat)
                }

            })
            return false
        } else {//设置了 surface
            codec.configure(mediaFormat, mSurface, null, 0)
            notifyDecode()
            return true
        }
    }

    override fun initSpecParams(format: MediaFormat) {

    }


    override fun doneDecode() {

    }
}