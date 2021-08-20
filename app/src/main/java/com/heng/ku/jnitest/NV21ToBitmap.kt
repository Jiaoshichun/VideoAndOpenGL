package com.heng.ku.jnitest


import android.content.Context
import android.graphics.*
import android.os.Build
import android.renderscript.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent

import java.io.ByteArrayOutputStream

/**
 * yuv转为bitmap
 */
class NV21ToBitmap private constructor(private val isUseRenderScript: Boolean) : LifecycleObserver {
    private var context: Context? = null
    private val TAG = "NV21ToBitmap"

    /**使用RenderScript进行转换*/
    constructor(
        activity: AppCompatActivity
    ) : this(true) {
        activity.lifecycle.addObserver(this)
        context = activity.applicationContext
    }

    /**空构造方法，用YUVImage进行转换*/
    @Deprecated("推荐使用带参数的构造方法，将会使用RenderScript进行转换，速度更快")
    constructor() : this(false)

    private var renderScript: RenderScript? = null
    private var yuvToRgbIntrinsic: ScriptIntrinsicYuvToRGB? = null
    private var inAllocation: Allocation? = null
    private var outAllocation: Allocation? = null
    private var yuvType: Type? = null
    private var rgbaType: Type? = null


    private var yuvImage: YuvImage? = null
    private var rect: Rect? = null
    fun nv21ToBitmap(nv21: ByteArray, width: Int, height: Int): Bitmap {
        if (isUseRenderScript) return nv21ToBitmapRenderScript(nv21, width, height)
        return nv21ToBitmapYuvImage(nv21, width, height)
    }

    /**
     * 使用yuvImage进行转换
     */
    private fun nv21ToBitmapYuvImage(nv21: ByteArray, width: Int, height: Int): Bitmap {
//        val start = SystemClock.elapsedRealtime()
        val bitmap = ByteArrayOutputStream().let {
            if (yuvImage == null) {
                yuvImage = YuvImage(nv21, ImageFormat.NV21, width, height, null)
                rect = Rect(0, 0, width, height)
            }
            yuvImage?.compressToJpeg(rect, 100, it)
            BitmapFactory.decodeByteArray(it.toByteArray(), 0, it.size())
        }
//        Log.v(TAG, "nv21ToBitmapYuvImage->耗时:${SystemClock.elapsedRealtime() - start}")
        return bitmap
    }

    /**
     * 使用RenderScript进行yuv转换
     */
    private fun nv21ToBitmapRenderScript(nv21: ByteArray, width: Int, height: Int): Bitmap {
//        val start = SystemClock.elapsedRealtime()
        if (renderScript == null) renderScript = RenderScript.create(context)

        if (yuvToRgbIntrinsic == null) {
            yuvToRgbIntrinsic =
                ScriptIntrinsicYuvToRGB.create(renderScript, Element.U8_4(renderScript))
        }
        if (inAllocation == null) {
            yuvType = Type.Builder(renderScript, Element.U8(renderScript)).setX(nv21.size).create()
            inAllocation = Allocation.createTyped(renderScript, yuvType)
        }
        if (outAllocation == null) {
            rgbaType =
                Type.Builder(renderScript, Element.RGBA_8888(renderScript)).setX(width).setY(height)
                    .create()
            outAllocation = Allocation.createTyped(renderScript, rgbaType)
        }

        inAllocation?.copyFrom(nv21)

        yuvToRgbIntrinsic?.setInput(inAllocation)
        yuvToRgbIntrinsic?.forEach(outAllocation)

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        outAllocation?.copyTo(bitmap)
//        Log.v(TAG, "nv21ToBitmapRenderScript->耗时:${SystemClock.elapsedRealtime() - start}")
        return bitmap

    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    private fun onDestroy() {
        rgbaType?.destroy()
        yuvType?.destroy()
        inAllocation?.destroy()
        outAllocation?.destroy()
        yuvToRgbIntrinsic?.destroy()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            RenderScript.releaseAllContexts()
        } else {
            renderScript?.destroy()
        }
    }
}