package com.example.glidedemo.activity

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.glidedemo.databinding.ActivityPermissionSettingBinding
import com.example.glidedemo.databinding.ViewPermissionGuideBinding
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class PermissionGuideActivity : AppCompatActivity() {
    private val binding by lazy {
        ActivityPermissionSettingBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            SystemBarStyle.dark(Color.TRANSPARENT),
            SystemBarStyle.dark(Color.TRANSPARENT)
        )
        setContentView(binding.root)
        initView()
        initActions()
    }

    private fun initActions() {
        binding.root.setOnClickListener {
            finish()
        }
    }


    private fun initView() {
        MainScope().launch {
            delay(200)
            val guideBinding =
                ViewPermissionGuideBinding.inflate(layoutInflater, binding.root, false)
            addContentView(guideBinding.root, ViewGroup.LayoutParams(MATCH_PARENT, MATCH_PARENT))
        }
    }
}