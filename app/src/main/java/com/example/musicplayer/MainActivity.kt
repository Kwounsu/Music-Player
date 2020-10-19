package com.example.musicplayer

import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import java.util.concurrent.TimeUnit


class MainActivity : AppCompatActivity() {

    lateinit var forwardBtn: ImageButton
    lateinit var backwardBtn: ImageButton
    lateinit var pauseBtn: ImageButton
    lateinit var playBtn: ImageButton
    lateinit var mediaPlayer: MediaPlayer
    private lateinit var songName: TextView
    private lateinit var startTime: TextView
    private lateinit var songTime: TextView
    private lateinit var seekBar: SeekBar
    private var handler: Handler = Handler()
    private var onTime: Int = 0
    private var playTime: Int = 0
    private var endTime: Int = 0
    private var forwardTime: Int = 5000
    private var backwardTime: Int = 5000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        title = "KotlinApp"
        backwardBtn = findViewById(R.id.btnBackward)
        forwardBtn = findViewById(R.id.btnForward)
        playBtn = findViewById(R.id.btnPlay)
        pauseBtn = findViewById(R.id.btnPause)
        songName = findViewById(R.id.txtSongName)
        startTime = findViewById(R.id.txtStartTime)
        songTime = findViewById(R.id.txtSongTime)
        songName.text = "Nana Kwabena - Find Your Way Beat"
        mediaPlayer = MediaPlayer.create(this, R.raw.find_your_way_beat)
        seekBar = findViewById(R.id.seekBar)
        seekBar.isClickable = false
        pauseBtn.isEnabled = true

        playBtn.setOnClickListener {
            Toast.makeText(this, "Playing Audio", Toast.LENGTH_SHORT).show()
            mediaPlayer.start()
            endTime = mediaPlayer.duration
            playTime = mediaPlayer.currentPosition
            seekBar.max = endTime
            onTime = 1
            songTime.text = String.format(
                "%d min, %d sec",
                TimeUnit.MILLISECONDS.toMinutes(endTime.toLong()),
                TimeUnit.MILLISECONDS.toSeconds(endTime.toLong()) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(endTime.toLong()))
            )
            startTime.text = String.format(
                "%d min, %d sec",
                TimeUnit.MILLISECONDS.toMinutes(playTime.toLong()),
                TimeUnit.MILLISECONDS.toSeconds(playTime.toLong()) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(playTime.toLong()))
            )
            seekBar.progress = playTime
            handler.postDelayed(updateSongTime, 100)
            pauseBtn.isEnabled = true
            playBtn.isEnabled = false
        }
        btnPause.setOnClickListener {
            mediaPlayer.pause()
            pauseBtn.isEnabled = false
            playBtn.isEnabled = true
            Toast.makeText(applicationContext, "Audio Paused", Toast.LENGTH_SHORT).show()
        }
        btnForward.setOnClickListener {
            if ((playTime + forwardTime) <= endTime) {
                playTime += forwardTime
                mediaPlayer.seekTo(playTime)
            } else {
                playTime = endTime
                mediaPlayer.seekTo(playTime)
            }
            if (!playBtn.isEnabled) {
                playBtn.isEnabled = true
            }
        }
        btnBackward.setOnClickListener {
            if ((playTime - backwardTime) > 0) {
                playTime -= backwardTime
                mediaPlayer.seekTo(playTime)
            } else {
                playTime = 0
                mediaPlayer.seekTo(playTime)
            }
            if (!playBtn.isEnabled) {
                playBtn.isEnabled = true
            }
        }
    }

    private val updateSongTime = object : Runnable {
        override fun run() {
            playTime = mediaPlayer.currentPosition
            startTime.text = String.format(
                "%d min, %d sec",
                TimeUnit.MILLISECONDS.toMinutes(playTime.toLong()),
                TimeUnit.MILLISECONDS.toSeconds(playTime.toLong())
                    - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(playTime.toLong()))
            )
            seekBar.progress = playTime
            handler.postDelayed(this, 100)
        }
    }
}