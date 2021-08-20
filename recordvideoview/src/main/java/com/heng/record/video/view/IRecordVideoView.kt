package com.heng.record.video.view

import java.nio.ByteBuffer

interface IRecordVideoView {
    /**
     * 开始预览
     */
    fun startPreview(cameraId:Int)

    /**
     * 开始录制视频
     * @param videoPath 视频存储路径
     *
     */
    fun startRecordVideo(videoPath:String)

    /**
     * 停止录制视频
     */
    fun stopRecordVideo()
    /**
     * 开始录制音频
     * @param audioPath 音频存储路径
     *
     */
    fun startRecordAudio(audioPath:String)

    /**
     * 停止录制音频
     */
    fun stopRecordAudio()

    /**
     * 停止预览
     */
    fun stopPreview()

    interface CallBack{
        fun onPreviewFrame(data:ByteArray,width:Int,height:Int)
        fun onAudioFrame(data:ByteBuffer,size:Int)
    }
}