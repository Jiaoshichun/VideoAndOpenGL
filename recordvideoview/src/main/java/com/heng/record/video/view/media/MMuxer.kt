package com.heng.record.video.view.media

import android.media.MediaCodec
import android.media.MediaFormat
import android.media.MediaMuxer
import android.util.Log
import java.io.File
import java.nio.ByteBuffer

/**
 * 是否需要同步时间
 * 同步录制时，需要同步时间
 */
class MMuxer(private val path: String) {
    private val TAG = javaClass.simpleName
    @Volatile
    private var mediaMuxer: MediaMuxer? = null

    private var audioTrackIndex = -1
    private var hasAddAudioTrack = false
    private var videoTrackIndex = -1
    private var hasAddVideoTrack = false
    @Volatile
    private var isStart = false

    init {
        if (!File(path).parentFile.exists()) File(path).parentFile.mkdirs()
    }

    @Synchronized
    fun addAudioTrack(format: MediaFormat) {
        if (isStart) return
        audioTrackIndex = try {
            if (mediaMuxer == null) createMuxer()
            mediaMuxer!!.addTrack(format)
        } catch (e: Exception) {
            e.printStackTrace()
            return
        }
        hasAddAudioTrack = true
        startMuxer()
    }

    @Synchronized
    fun addVideoTrack(format: MediaFormat) {
        if (isStart) return
        videoTrackIndex = try {
            if (mediaMuxer == null) createMuxer()
            mediaMuxer!!.addTrack(format)
        } catch (e: Exception) {
            e.printStackTrace()
            return
        }
        mediaMuxer!!.setOrientationHint(getRotationAngle(format))
        hasAddVideoTrack = true
        startMuxer()
    }

    private fun createMuxer() {
        mediaMuxer = MediaMuxer(path, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
    }

    fun setNoAudio() {
        if (hasAddAudioTrack) return
        hasAddAudioTrack = true
        mIsAudioEnd = true
        startMuxer()
    }

    fun setNoVideo() {
        if (hasAddVideoTrack) return
        hasAddVideoTrack = true
        mIsVideoEnd = true
        startMuxer()
    }

    fun writeAudioSampleData(byteBuffer: ByteBuffer, bufferInfo: MediaCodec.BufferInfo): Boolean {
        if (!isStart) return false

        Log.d(TAG, "writeAudioSampleData->${bufferInfo.presentationTimeUs}")
        mediaMuxer?.writeSampleData(audioTrackIndex, byteBuffer, bufferInfo)
        return true
    }

    // do not support out of order frames (timestamp: 104997 < last: 105520 for Audio track
    fun writeVideoSampleData(byteBuffer: ByteBuffer, bufferInfo: MediaCodec.BufferInfo): Boolean {
        if (!isStart) return false
        Log.d(TAG, "writeVideoSampleData->${bufferInfo.presentationTimeUs}")
        mediaMuxer?.writeSampleData(videoTrackIndex, byteBuffer, bufferInfo)
        return true
    }

    private fun startMuxer() {
        if (hasAddAudioTrack && hasAddVideoTrack) {
            Log.d(TAG, "startMuxer")
            mediaMuxer?.start()
            isStart = true
        }
    }

    private var mIsAudioEnd = false
    private var mIsVideoEnd = false

    fun releaseVideoTrack() {
        Log.d(TAG, "releaseVideoTrack")
        mIsVideoEnd = true
        release()
    }

    fun releaseAudioTrack() {
        Log.d(TAG, "releaseAudioTrack")
        mIsAudioEnd = true
        release()
    }

    /**
     *
     * Timed-out waiting for video track to reach final audio timestamp !
     * java.lang.IllegalStateException: Failed to stop the muxer
     * 语音录制时，ByteBuffer长度太长导致的
     */
    @Synchronized
    private fun release() {
        if (mIsAudioEnd && mIsVideoEnd) {
            Log.d(TAG, "release")
            try {
                mediaMuxer?.stop()
                mediaMuxer?.release()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                hasAddAudioTrack = false
                hasAddVideoTrack = false
                isStart = false
                mediaMuxer = null
                audioTrackIndex = -1
                videoTrackIndex = -1
            }

        }

    }

    private fun getRotationAngle(format: MediaFormat): Int {
        if (format.containsKey(MediaFormat.KEY_ROTATION)) {
            return format.getInteger(MediaFormat.KEY_ROTATION)
        }
        return 0
    }
}
/*

第一次开始录制后，再重新录制会报该异常 录制的回调每次都是新建，导致之前的对象无法回收
 * 2020-03-31 16:15:57.402 10801-11001/com.heng.ku.jnitest E/MPEG4Writer: do not support out of order frames (timestamp: 7224 < last: 23219 for Audio track
2020-03-31 16:15:57.402 10801-11001/com.heng.ku.jnitest E/MPEG4Writer: Dumping Audio track's last 10 frames timestamp and frame type
2020-03-31 16:15:57.406 10801-10984/com.heng.ku.jnitest E/MediaAdapter: pushBuffer called before start

--------- beginning of crash
2020-03-31 16:15:57.414 10801-10984/com.heng.ku.jnitest E/AndroidRuntime: FATAL EXCEPTION: Thread-6
Process: com.heng.ku.jnitest, PID: 10801
java.lang.IllegalStateException: writeSampleData returned an error
at android.media.MediaMuxer.nativeWriteSampleData(Native Method)
at android.media.MediaMuxer.writeSampleData(MediaMuxer.java:682)
at com.heng.record.video.view.media.MMuxer.writeAudioSampleData(MMuxer.kt:76)
at com.heng.record.video.view.media.encoder.AudioEncoder.writeData(AudioEncoder.kt:75)
at com.heng.record.video.view.media.encoder.BaseEncoder.drain(BaseEncoder.kt:191)
at com.heng.record.video.view.media.encoder.BaseEncoder.run(BaseEncoder.kt:64)
at java.lang.Thread.run(Thread.java:764)





2020-03-31 16:43:14.910 13274-13768/com.heng.ku.jnitest W/MediaAnalyticsItem: Unable to record: [1:audiorecord:0:-1::0:-1:1:0:8:android.media.audiorecord.latency=20:android.media.audiorecord.samplerate=44100:android.media.audiorecord.channels=2:android.media.audiorecord.encoding=AUDIO_FORMAT_PCM_16_BIT:android.media.audiorecord.source=AUDIO_SOURCE_MIC:android.media.audiorecord.durationMs=2078:android.media.audiorecord.n=1:android.media.audiorecord.createdMs=1585644192771:] [forcenew=0]
2020-03-31 16:43:15.248 13274-13778/com.heng.ku.jnitest W/MPEG4Writer: Timed-out waiting for video track to reach final audio timestamp !
2020-03-31 16:43:15.250 13274-13778/com.heng.ku.jnitest W/System.err: java.lang.IllegalStateException: Failed to stop the muxer
2020-03-31 16:43:15.251 13274-13778/com.heng.ku.jnitest W/System.err:     at android.media.MediaMuxer.nativeStop(Native Method)
2020-03-31 16:43:15.251 13274-13778/com.heng.ku.jnitest W/System.err:     at android.media.MediaMuxer.stop(MediaMuxer.java:454)
2020-03-31 16:43:15.252 13274-13778/com.heng.ku.jnitest W/System.err:     at com.heng.record.video.view.media.MMuxer.release(MMuxer.kt:122)
2020-03-31 16:43:15.252 13274-13778/com.heng.ku.jnitest W/System.err:     at com.heng.record.video.view.media.MMuxer.releaseVideoTrack(MMuxer.kt:102)
2020-03-31 16:43:15.252 13274-13778/com.heng.ku.jnitest W/System.err:     at com.heng.record.video.view.media.encoder.VideoEncoder.release(VideoEncoder.kt:91)
2020-03-31 16:43:15.252 13274-13778/com.heng.ku.jnitest W/System.err:     at com.heng.record.video.view.media.encoder.BaseEncoder.done(BaseEncoder.kt:94)
2020-03-31 16:43:15.252 13274-13778/com.heng.ku.jnitest W/System.err:     at com.heng.record.video.view.media.encoder.BaseEncoder.run(BaseEncoder.kt:82)
2020-03-31 16:43:15.253 13274-13778/com.heng.ku.jnitest W/System.err:     at java.lang.Thread.run(Thread.java:764)
2020-03-31 16:43:15.502 13274-13293/com.heng.ku.jnitest W/System: A resource failed to call release.
 */