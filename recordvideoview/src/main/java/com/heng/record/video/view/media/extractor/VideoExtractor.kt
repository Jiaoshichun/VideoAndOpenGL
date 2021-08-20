package com.heng.record.video.view.media.extractor

import android.media.MediaFormat
import com.heng.record.video.view.media.extractor.IExtractor
import com.heng.record.video.view.media.extractor.RealExtractor
import java.nio.ByteBuffer

/**
 * 视频分离器
 */
class VideoExtractor(path: String) : IExtractor {


    private val mRealExtractor = RealExtractor(path, false)
    override fun getFormat(): MediaFormat? {
        return mRealExtractor.mFormat
    }

    override fun readBuffer(byteBuffer: ByteBuffer): Int {
        return mRealExtractor.readSampleData(byteBuffer)
    }

    override fun getCurrentTimestamp(): Long {
        return mRealExtractor.mCurSampleTime
    }

    override fun getSampleFlag(): Int {
        return mRealExtractor.mCurSampleFlags
    }

    override fun seek(pos: Long): Long {
        return mRealExtractor.seekTo(pos)
    }

    override fun stop() {
        mRealExtractor.stop()
    }

    override fun getTrack() = mRealExtractor.mTrack

    override fun getRotationAngle(): Int {
        val mediaFormat = getFormat()
        if (mediaFormat?.containsKey(MediaFormat.KEY_ROTATION) == true) {
            return mediaFormat.getInteger(MediaFormat.KEY_ROTATION)
        }
        return 0
    }

}