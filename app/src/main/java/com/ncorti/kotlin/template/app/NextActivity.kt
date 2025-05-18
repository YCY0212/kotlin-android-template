package com.ncorti.kotlin.template.app

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.ncorti.kotlin.template.app.databinding.ActivityNextBinding
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.utils.YouTubePlayerTracker

class NextActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNextBinding
    private val tracker = YouTubePlayerTracker()
    private val handler = Handler(Looper.getMainLooper())

    private val startSec = 10f
    private val endSec = 20f

    private lateinit var youTubePlayer: YouTubePlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNextBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val youtubeUrl = intent.getStringExtra("youtube_url") ?: ""
        val videoId = extractYoutubeVideoId(youtubeUrl)

        if (videoId == null) {
            Toast.makeText(this, "유효한 YouTube 링크가 아닙니다.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        lifecycle.addObserver(binding.youtubePlayerView)

        binding.youtubePlayerView.addYouTubePlayerListener(object : AbstractYouTubePlayerListener() {
            override fun onReady(player: YouTubePlayer) {
                youTubePlayer = player
                youTubePlayer.addListener(tracker)
                youTubePlayer.loadVideo(videoId, startSec)

                handler.post(object : Runnable {
                    override fun run() {
                        if (tracker.currentSecond >= endSec) {
                            youTubePlayer.seekTo(startSec)
                        }
                        handler.postDelayed(this, 500)
                    }
                })
            }
        })
    }

    private fun extractYoutubeVideoId(url: String): String? {
        return try {
            val regex = Regex("(?:v=|youtu\\.be/|embed/)([\\w-]{11})")
            val match = regex.find(url)
            match?.groups?.get(1)?.value
        } catch (e: Exception) {
            null
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
    }
}
