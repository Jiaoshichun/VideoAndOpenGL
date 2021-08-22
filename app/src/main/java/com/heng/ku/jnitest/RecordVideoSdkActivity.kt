package com.heng.ku.jnitest

import android.hardware.Camera
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import com.heng.record.video.view.RecordeVideoConfig
import kotlinx.android.synthetic.main.activity_record_video_sdk.*
import java.io.File

class RecordVideoSdkActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_record_video_sdk)
//        RecordeVideoConfig.previewHeight=720
//        RecordeVideoConfig.previewWidth=1280
//        RecordeVideoConfig.videoWidth=720
//        RecordeVideoConfig.videoHeight=1280

        btnStartRecord.setOnClickListener {
            recordVideoView.startRecordVideo(VIDEO_FILE1)
        }
        btnStopRecord.setOnClickListener {
            recordVideoView.stopRecordVideo()
        }
        btnStartPreview.setOnClickListener {
            recordVideoView.startPreview(Camera.CameraInfo.CAMERA_FACING_BACK)
        }
        btnStopPreview.setOnClickListener {
            recordVideoView.stopPreview()
        }
        btnStartRecordAudio.setOnClickListener {
            recordVideoView.startRecordAudio(
                Environment.getExternalStorageDirectory().absolutePath +
                        File.separator + "heng" + File.separator + "mvtest.wav"
            )

        }
        btnStopRecordAudio.setOnClickListener {
            recordVideoView.stopRecordAudio()
        }
    }
}