package com.heng.ku.jnitest

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.os.Looper
import android.util.Log
import android.view.MotionEvent
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.heng.ku.jnitest.media.Mp4Repack
import com.mylhyl.acp.Acp
import com.mylhyl.acp.AcpOptions
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.*
import java.io.File
import androidx.core.app.ComponentActivity.ExtraData
import androidx.core.content.ContextCompat.getSystemService
import android.icu.lang.UCharacter.GraphemeClusterBreak.T



val VIDEO_FILE1 =
    Environment.getExternalStorageDirectory().absolutePath + File.separator + "heng" + File.separator + "mvtest.mp4"
val VIDEO_FILE2 =
    Environment.getExternalStorageDirectory().absolutePath + File.separator + "heng" + File.separator + "mvtest_2.mp4"
val DEST_VIDEO_FILE =
    Environment.getExternalStorageDirectory().absolutePath + File.separator + "heng" + File.separator + "dest.mp4"

/**
 *主要参考https://github.com/ChenLittlePing/LearningVideo 学习
 */
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Acp.getInstance(this).request(
            AcpOptions.Builder()
                .setPermissions(
                    Manifest.permission.CAMERA
                    , Manifest.permission.WRITE_EXTERNAL_STORAGE
                    , Manifest.permission.RECORD_AUDIO
                    , Manifest.permission.INTERNET
                    , Manifest.permission.READ_EXTERNAL_STORAGE
                )
                .build(),
            null
        )
        //MediaCodecc 播放视频
        btn_play.setOnClickListener { startActivity(Intent(this, VideoPlayActivity::class.java)) }
        //OpenGL 渲染视频
        btn_gl_video.setOnClickListener {
            startActivity(Intent(this, GlVideoPlayActivity::class.java))
        }
        btn_soul_video.setOnClickListener {
            startActivity(Intent(this, SoulVideoPlayActivity::class.java))
        }
        //OpenGL 显示 三角形
        btn_gl_triangle.setOnClickListener {
            GlTriangleActivity.start(
                this,
                GlTriangleActivity.Type.TRIANGLE
            )
        }
        //OpenGL 显示 Bitmap
        btn_gl_bitmap.setOnClickListener {
            GlTriangleActivity.start(this, GlTriangleActivity.Type.BITMAP)
        }
        //OpenGL 显示 颜色渐变 三角形
        btn_gl_triangle2.setOnClickListener {
            GlTriangleActivity.start(
                this,
                GlTriangleActivity.Type.COLOR_TRIANGLE
            )
        }

        //OpenGL 显示 立方体
        btn_gl_cube.setOnClickListener {
            GlTriangleActivity.start(
                this,
                GlTriangleActivity.Type.CUBE
            )
        }

        //OpenGL 显示圆形
        btn_gl_circle.setOnClickListener {
            GlTriangleActivity.start(
                this,
                GlTriangleActivity.Type.CIRCLE
            )
        }

        //OpenGl 显示圆锥体
        btn_gl_cone.setOnClickListener {
            GlTriangleActivity.start(
                this,
                GlTriangleActivity.Type.CONE
            )
        }

        //OpenGl 显示圆柱体
        btn_gl_cylinder.setOnClickListener {
            GlTriangleActivity.start(
                this,
                GlTriangleActivity.Type.CYLINDER
            )
        }
        btn_muxer.setOnClickListener {
            Mp4Repack(
                VIDEO_FILE2,
                DEST_VIDEO_FILE
            ).start {
                Looper.prepare()
                Toast.makeText(this, "封装完毕", Toast.LENGTH_SHORT).show()
                Looper.loop()
            }
        }

        btn_encoder.setOnClickListener {
            startActivity(Intent(this, EncoderVideoActivity::class.java))
        }

        btn_camera_record.setOnClickListener {
            startActivity(Intent(this, CameraRecordActivity::class.java))
        }
        btn_camera_record_sdk.setOnClickListener {
            startActivity(Intent(this, RecordVideoSdkActivity::class.java))
        }
        isLaunchByVirtualApp()
        GlobalScope.launch  (Dispatchers.Main){
           val io1 = withContext(Dispatchers.IO) {
               Log.d("chun","io1 ${Thread.currentThread().name}")
               "io1"
           }
            val withContext = withContext(Dispatchers.Default) {
                Log.d("chun","Default ${Thread.currentThread().name}")
                "Default"
            }
            Log.d("chun",io1+withContext+Thread.currentThread().name)

            Log.d("chun", Log.getStackTraceString(Throwable()))
        }
    }
    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        return super.dispatchTouchEvent(ev)
    }
    /**
     * 判断是否是虚拟App
     */
    private fun isLaunchByVirtualApp(): Boolean {
        try {
            // md5(anti-virtual-app)
            val antiFilename = ".6784547bf9f3b712caa4775aefa142f9"
            val antiFilePath = "/data/data/${packageName}/$antiFilename"
            val antiFile = File(antiFilePath)
            if (antiFile.exists()) {
                if (antiFile.isFile) {
                    antiFile.delete()
                } else {
                    antiFile.deleteRecursively()
                }
            }
            antiFile.createNewFile()
            val process = Runtime.getRuntime().exec("find $antiFilePath")
            val findResult = process?.let { it.inputStream.bufferedReader().readLine() }
            return antiFilePath != findResult
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }

}
