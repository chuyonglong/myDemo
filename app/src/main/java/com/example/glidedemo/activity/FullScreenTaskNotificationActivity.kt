package com.example.glidedemo.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.glidedemo.databinding.ActivityFullScreenTaskNotificationBinding

class FullScreenTaskNotificationActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityFullScreenTaskNotificationBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
    }


}