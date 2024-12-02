package com.example.glidedemo.activity

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.activity.SystemBarStyle
import androidx.appcompat.app.AppCompatActivity
import com.example.glidedemo.databinding.ActivityTransparentBinding
import com.example.glidedemo.extensions.onDemoBackPressed
import com.example.glidedemo.extensions.viewBindings


class TransparentActivity : AppCompatActivity() {

    private val binding by viewBindings(
        ActivityTransparentBinding::inflate, SystemBarStyle.dark(Color.TRANSPARENT)
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding
        initView()
        onDemoBackPressed(true) {
            finish()
            startActivity(Intent(this, MainActivity::class.java))
        }
    }

    private fun initView() {
        // 显示壁纸
        window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER)
        // 隐藏通知栏
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        // 隐藏导航栏
        val decorView = window.decorView
        val uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_FULLSCREEN
        decorView.systemUiVisibility = uiOptions
    }

}