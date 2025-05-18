package com.ncorti.kotlin.template.app

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.ncorti.kotlin.template.app.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.buttonConvert.setOnClickListener {
            val youtubeUrl = binding.editTextYoutube.text.toString()
            if (youtubeUrl.isNotBlank()) {
                val intent = Intent(this, NextActivity2::class.java)
                intent.putExtra("youtube_url", youtubeUrl)
                startActivity(intent)
            } else {
                Toast.makeText(this, "유튜브 링크를 입력해주세요.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
