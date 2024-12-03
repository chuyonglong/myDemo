package com.example.glidedemo.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.glidedemo.databinding.ActivityGuideViewBinding
import com.example.glidedemo.extensions.showGuideView
import com.example.glidedemo.views.SimpleComponent


class GuideViewActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityGuideViewBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        initView()
    }

    private fun initView() {
        binding.imageGuide.post {
            binding.imageGuide.showGuideView(this, 0, SimpleComponent()) {
                binding.textGuide.showGuideView(this, 0, SimpleComponent()) {}
            }
        }
    }
}