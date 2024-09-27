package com.example.glidedemo.activity

import android.os.Bundle
import com.example.glidedemo.R
import com.example.glidedemo.base.BaseActivity
import com.example.glidedemo.databinding.ActivityLedClockBinding

class LedClockActivity : BaseActivity() {

    private val binding by lazy {
        ActivityLedClockBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        binding.edgeLightView.apply {
            setDrawMode(false, R.drawable.cat)
            setWidth(100F)
        }
        binding.start.setOnClickListener {
            binding.ledClockTime.start()
        }
        binding.end.setOnClickListener {
            binding.ledClockTime.stop()
        }
        binding.countdownStart.setOnClickListener {
            binding.ledClockTime.timingType = 2
//            binding.ledClockTime.countdownTime = 6F
            binding.ledClockTime.start()
        }

        binding.countUpStart.setOnClickListener {
            binding.ledClockTime.countdownTime = 0F
            binding.ledClockTime.timingType = 1
            binding.ledClockTime.start()
        }
    }
}