package com.heng.record.video.view.media.decoder


/**
 * 解码状态
 *
 * @author Chen Xiaoping (562818444@qq.com)
 * @since LearningVideo
 * @version LearningVideo
 * @Datetime 2019-09-02 10:00
 *
 */
enum class DecodeState {
    /**解码中*/
    DECODING,
    /**解码暂停*/
    PAUSE,
    /**解码器停止*/
    STOP,
    /**未初始化*/
    UNINIT,
    /**错误*/
    ERROR,
    /***准备开始*/
    PREPARE
}
