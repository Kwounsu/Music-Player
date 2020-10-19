package com.example.musicplayer

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.media.MediaBrowserServiceCompat
import androidx.mediarouter.media.MediaControlIntent
import androidx.mediarouter.media.MediaRouteSelector
import androidx.mediarouter.media.MediaRouter
import androidx.mediarouter.media.MediaRouterParams
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

        // Seekbar
        seekBar.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if(mediaPlayer!=null && fromUser) {
                    playTime = progress
                    mediaPlayer.seekTo(playTime)
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        // Play, Pause, Backward, Forward buttons
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

        // Notification bar Media Controller
        notification()
        createNotificationChannel()

    }

    private fun notification() {
        val builder = NotificationCompat.Builder(this, "CHANNEL_ID")
            .setSmallIcon(R.drawable.ic_icon_foreground)
            .setContentTitle(songName.text.toString())
            .setContentText("textContent")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        with(NotificationManagerCompat.from(this)) {
            // notificationId is a unique int for each notification that you must define
            notify(1, builder.build())
        }
    }

    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.channel_name)
            val descriptionText = getString(R.string.channel_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel("CHANNEL_ID", name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private class MediaRouterCallback : MediaRouter.Callback() {
        override fun onRouteSelected(
            router: MediaRouter,
            route: MediaRouter.RouteInfo,
            reason: Int
        ) {
            if (reason == MediaRouter.UNSELECT_REASON_ROUTE_CHANGED) {
                println("Unselected because route changed, continue playback")
            } else if (reason == MediaRouter.UNSELECT_REASON_STOPPED) {
                println("Unselected because route was stopped, stop playback")
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