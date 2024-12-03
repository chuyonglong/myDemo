package com.example.glidedemo.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.example.glidedemo.databinding.ActivityThemeBinding
import com.example.glidedemo.extensions.viewBindings

class ThemeActivity : AppCompatActivity() {
    private val binding by viewBindings(ActivityThemeBinding::inflate)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding
        initViews()
        initActions()
    }

    private fun initViews() {
        val currentTheme = AppCompatDelegate.getDefaultNightMode()
        if (currentTheme == AppCompatDelegate.MODE_NIGHT_YES) {
            binding.themeTag.text = "当前为:黑暗模式"
        } else {
            binding.themeTag.text = "当前为:明亮模式"
        }
    }

    private fun initActions() {
        binding.themeSwitchButton.setOnClickListener {
            // 切换主题
            val currentTheme = AppCompatDelegate.getDefaultNightMode()
            if (currentTheme == AppCompatDelegate.MODE_NIGHT_YES) {
                binding.themeTag.text = "当前为:明亮模式"
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO) // 切换到明亮模式
            } else {
                binding.themeTag.text = "当前为:黑暗模式"
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES) // 切换到黑暗模式
            }
        }
    }

}