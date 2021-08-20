package com.heng.record.video.view

import android.media.AudioFormat

object RecordeVideoConfig {
    /////////音频参数
    const val DEFAULT_AUDIO_SAMPLE_RATE = 44100
    const val DEFAULT_AUDIO_CHANNEL_COUNT = 1
    const val DEFAULT_AUDIO_BIT_PER_SAMPLE = 16
    const val DEFAULT_AUDIO_FORMAT =  AudioFormat.ENCODING_PCM_16BIT

    var audioSampleRate: Int = DEFAULT_AUDIO_SAMPLE_RATE//采样率
    var audioChannelCount: Int = DEFAULT_AUDIO_CHANNEL_COUNT//声道数
    var audioBitPerSample: Int = DEFAULT_AUDIO_BIT_PER_SAMPLE//每个采样占用的字节数
    var audioFormat: Int = DEFAULT_AUDIO_FORMAT//音频编码格式


    /////////视频参数
    const val DEFAULT_VIDEO_FRAME_RATE = 15
    const val DEFAULT_VIDEO_WIDTH = 480
    const val DEFAULT_VIDEO_HEIGHT = 640
    const val VIDEO_ORIENTATION_PORTRAIT = 1//竖屏
    const val VIDEO_ORIENTATION_LANDSCAPE = 2//横屏
    const val DEFAULT_VIDEO_ORIENTATION = VIDEO_ORIENTATION_PORTRAIT

    var videoFrameRate = DEFAULT_VIDEO_FRAME_RATE//视频帧率
    var videoWidth = DEFAULT_VIDEO_WIDTH//视频宽
    var videoHeight = DEFAULT_VIDEO_HEIGHT//视频高
    var previewWidth = DEFAULT_VIDEO_HEIGHT//预览宽
    var previewHeight = DEFAULT_VIDEO_WIDTH//预览高  由于默认竖屏，旋转90度，宽高颠倒
    var videoOrientation = DEFAULT_VIDEO_ORIENTATION//视频方向，默认竖屏
}