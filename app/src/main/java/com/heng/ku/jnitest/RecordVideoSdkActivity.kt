package com.heng.ku.jnitest

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_record_video_sdk.*

class RecordVideoSdkActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_record_video_sdk)
        btnStartRecord.setOnClickListener {
            recordVideoView.startRecordVideo(VIDEO_FILE1)
        }
        btnStopRecord.setOnClickListener {
            recordVideoView.stopRecordVideo()
        }
        btnStartPreview.setOnClickListener {
            recordVideoView.startPreview(0)
        }
        btnStopPreview.setOnClickListener {
            recordVideoView.stopPreview()
        }
    }
}