package com.heng.ku.jnitest

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.heng.ku.jnitest.media.decoder.*
import kotlinx.android.synthetic.main.activity_video_play.*

class VideoPlayActivity : AppCompatActivity() {
    private val TAG = "MainActivity"
    private var videoDecoder: VideoDecoder? = null
    private var audioDecoder: AudioDecoder? = null
    private var pos = -1L
    private var isStart = false
    private var isPause = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_play)


        videoDecoder = VideoDecoder(VIDEO_FILE1, surfaceView)
        audioDecoder = AudioDecoder(VIDEO_FILE1)
        videoDecoder?.start()
        audioDecoder?.start()
        videoDecoder!!.mStateListener=object :DefDecodeStateListener{
            override fun decoderStop(decodeJob: BaseDecoder?) {
                start.text = "开始"
                isStart = false
                isPause = false
            }

            override fun decoderRunning(decodeJob: BaseDecoder?) {
                if (pos != -1L) {
                    videoDecoder!!.seekTo(pos)
                    audioDecoder!!.seekTo(pos)
                    pos = -1L
                }
            }
        }
        start.setOnClickListener {
            isStart = !isStart
            if (isStart) {
                videoDecoder!!.start()
                audioDecoder!!.start()
                start.text = "停止"
            } else {
                videoDecoder!!.stop()
                audioDecoder!!.stop()
                start.text = "开始"
            }

        }
        pause.setOnClickListener {
            if (!isStart) return@setOnClickListener
            isPause = !isPause
            if (isPause) {
                videoDecoder!!.pause()
                audioDecoder!!.pause()
                pause.text = "继续"
            } else {
                videoDecoder!!.resume()
                audioDecoder!!.resume()
                pause.text = "暂停"
            }


        }
    }

    override fun onStop() {
        super.onStop()
        pos = videoDecoder!!.getCurTimeStamp()
        audioDecoder?.stop()
        videoDecoder?.stop()
    }

    override fun onStart() {
        super.onStart()
        if (pos != -1L) {
            audioDecoder!!.start()
            videoDecoder!!.start()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        audioDecoder?.stop()
        videoDecoder?.stop()
    }
}
