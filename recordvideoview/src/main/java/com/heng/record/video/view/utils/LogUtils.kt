package com.heng.record.video.view.utils

import android.util.Log

object LogUtils {
    var isPrintLog = false
    fun d(tag: String, msg: String) {
        if (!isPrintLog) return
        Log.d(tag, msg)
    }
    fun e(tag: String, msg: String) {
        if (!isPrintLog) return
        Log.e(tag, msg)
    }
    fun i(tag: String, msg: String) {
        if (!isPrintLog) return
        Log.i(tag, msg)
    }
    fun w(tag: String, msg: String) {
        if (!isPrintLog) return
        Log.w(tag, msg)
    }
}