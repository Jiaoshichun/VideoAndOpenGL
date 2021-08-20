package com.heng.record.video.view.media

import android.media.MediaCodec
import android.util.Log
import com.heng.record.video.view.media.extractor.AudioExtractor
import com.heng.record.video.view.media.extractor.VideoExtractor
import java.nio.ByteBuffer

private const val TAG = "Mp4Repack"

class Mp4Repack(srcPath: String, destPath: String) {
    private val audioExtractor = AudioExtractor(srcPath)
    private val videoExtractor = VideoExtractor(srcPath)
    private val mMuxer = MMuxer(destPath)
    fun start(callBack: () -> Unit) {
        Log.d(TAG, "开始")
        val audioFormat = audioExtractor.getFormat()
        if (audioFormat == null) {
            mMuxer.setNoAudio()
        } else {
            mMuxer.addAudioTrack(audioFormat)
        }
        val videoFormat = videoExtractor.getFormat()
        if (videoFormat == null) {
            mMuxer.setNoVideo()
        } else {
            mMuxer.addVideoTrack(videoFormat)
        }
        Thread {
            val buffer = ByteBuffer.allocate(500 * 1024)
            val bufferInfo = MediaCodec.BufferInfo()
            if (audioFormat != null) {
                Log.d(TAG, "开始封装语音轨")
                var size = audioExtractor.readBuffer(buffer)
                while (size > 0) {
                    bufferInfo.set(
                        0,
                        size,
                        audioExtractor.getCurrentTimestamp(),
                        audioExtractor.getSampleFlag()
                    )
                    mMuxer.writeAudioSampleData(buffer, bufferInfo)
                    size = audioExtractor.readBuffer(buffer)
                }
            }
            if (videoFormat != null) {
                var size = videoExtractor.readBuffer(buffer)
                Log.d(TAG, "开始封装视频轨")
                while (size > 0) {
                    bufferInfo.set(
                        0,
                        size,
                        videoExtractor.getCurrentTimestamp(),
                        videoExtractor.getSampleFlag()
                    )
                    mMuxer.writeVideoSampleData(buffer, bufferInfo)
                    size = videoExtractor.readBuffer(buffer)
                }
            }
            mMuxer.releaseAudioTrack()
            mMuxer.releaseVideoTrack()
            Log.d(TAG, "封装完毕")
            callBack.invoke()
        }.start()
    }
}