package com.ncorti.kotlin.template.app

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.ncorti.kotlin.template.app.databinding.ActivityNextBinding
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.utils.YouTubePlayerTracker
import java.util.Locale
import android.content.pm.PackageManager


class NextActivity3 : AppCompatActivity() {

    private lateinit var binding: ActivityNextBinding
    private val tracker = YouTubePlayerTracker()
    private val handler = Handler(Looper.getMainLooper())

    private lateinit var speechRecognizer: SpeechRecognizer
    private lateinit var speechIntent: Intent

    private lateinit var youTubePlayer: YouTubePlayer
    private var currentStep = 0

    // 🔁 구간 리스트 (start, end)
    private val stepList = listOf(
        Pair(10f, 20f),
        Pair(21f, 35f),
        Pair(36f, 50f)
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 전체화면 설정 (선택사항)
        window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION

        // 🔐 마이크 권한 요청
        if (checkSelfPermission(android.Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(android.Manifest.permission.RECORD_AUDIO), 1)
        }

        binding = ActivityNextBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val youtubeUrl = intent.getStringExtra("youtube_url") ?: ""
        val videoId = extractYoutubeVideoId(youtubeUrl)

        if (videoId == null) {
            Toast.makeText(this, "유효한 YouTube 링크가 아닙니다.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // 🎤 음성 인식 초기화
        initSpeechRecognizer()
        startListening()

        lifecycle.addObserver(binding.youtubePlayerView)

        binding.youtubePlayerView.addYouTubePlayerListener(object : AbstractYouTubePlayerListener() {
            override fun onReady(player: YouTubePlayer) {
                youTubePlayer = player
                youTubePlayer.addListener(tracker)

                val (startSec, _) = stepList[currentStep]
                youTubePlayer.loadVideo(videoId, startSec)

                startLooping()
            }
        })
    }


    private fun startLooping() {
        val (_, endSec) = stepList[currentStep]
        handler.post(object : Runnable {
            override fun run() {
                if (tracker.currentSecond >= endSec) {
                    val (startSec, _) = stepList[currentStep]
                    youTubePlayer.seekTo(startSec)
                }
                handler.postDelayed(this, 500)
            }
        })
    }

    private fun moveToNextStep() {
        if (currentStep + 1 >= stepList.size) {
            Toast.makeText(this, "마지막 단계입니다.", Toast.LENGTH_SHORT).show()
            return
        }

        currentStep++
        val (startSec, _) = stepList[currentStep]
        youTubePlayer.seekTo(startSec)
        Toast.makeText(this, "${currentStep + 1}단계로 이동합니다.", Toast.LENGTH_SHORT).show()
    }

    private fun initSpeechRecognizer() {
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
        speechIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.KOREAN)
        }

        speechRecognizer.setRecognitionListener(object : RecognitionListener {
            override fun onResults(results: Bundle) {
                val matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                matches?.forEach { result ->
                    if (result.contains("다음")) {
                        moveToNextStep()
                        return@forEach
                    }
                }
                startListening() // 다시 듣기 시작
            }

            override fun onError(error: Int) {
                startListening() // 에러 후 다시 듣기
            }

            override fun onReadyForSpeech(params: Bundle) {}
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {}
            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })
    }

    private fun startListening() {
        speechRecognizer.startListening(speechIntent)
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
        speechRecognizer.destroy()
    }
}
