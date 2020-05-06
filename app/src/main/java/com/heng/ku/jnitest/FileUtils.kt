package com.heng.ku.jnitest

import android.graphics.Bitmap
import android.os.Environment
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

object FileUtils {

    private fun getParentFile(): File {
        val file =
            File(Environment.getExternalStorageDirectory().absolutePath + File.separator + "heng")
        if (!file.exists() || !file.isDirectory) file.mkdirs()
        return file
    }

    private const val MAX_SIZE = 400
    private const val VALUE_1024 = 1024
    private const val VALUE_OPTION = 6
    fun saveBitmap(path: String, bitmap: Bitmap) {
        ByteArrayOutputStream().use { out ->
            var option = 100
            bitmap.compress(Bitmap.CompressFormat.JPEG, option, out)
            while (out.toByteArray().size / VALUE_1024 > MAX_SIZE && option > VALUE_OPTION) {
                out.reset()
                option -= 6
                bitmap.compress(Bitmap.CompressFormat.JPEG, option, out)
            }
            FileOutputStream(path).use {
                out.writeTo(it)
                out.flush()
            }
        }

    }
}

fun Date.timeStr(): String {
    return SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA).format(this)
}