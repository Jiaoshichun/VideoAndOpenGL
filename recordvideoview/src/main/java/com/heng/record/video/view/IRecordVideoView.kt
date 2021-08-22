package com.heng.record.video.view

import java.nio.ByteBuffer

interface IRecordVideoView {
    /**
     * 开始预览
     */
    fun startPreview(cameraId: Int)

    /**
     * 开始录制视频
     * @param videoPath 视频存储路径
     *
     */
    fun startRecordVideo(videoPath: String)

    /**
     * 停止录制视频
     */
    fun stopRecordVideo()

    /**
     * 开始录制音频
     * @param audioPath 音频存储路径
     *
     */
    fun startRecordAudio(audioPath: String)

    /**
     * 停止录制音频
     */
    fun stopRecordAudio()

    /**
     * 停止预览
     */
    fun stopPreview()

    /**
     * 设置回调
     */
    fun setCallBack(callBack: CallBack)

    interface CallBack {
        /**
         * 视频帧回调
         */
        fun onPreviewFrame(data: ByteArray, width: Int, height: Int)

        /**
         * 音频帧回调
         */
        fun onAudioFrame(data: ByteArray, size: Int)
    }
}