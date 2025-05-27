package com.example.glidedemo.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.glidedemo.databinding.ActivityAppInstallDialogBinding
import com.example.glidedemo.extensions.toast
import com.example.glidedemo.extensions.viewBindings
import com.example.glidedemo.utils.AppUtils

class AppInstallDialogActivity : AppCompatActivity() {

    private val binding by viewBindings(ActivityAppInstallDialogBinding::inflate)

    private var operatePackageName: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding
        initData()
        initView()
        initActions()
    }

    private fun initActions() {
        binding.appLockBtn.setOnClickListener {
            toast("加锁逻辑")
            finish()
        }
    }

    private fun initView() {
        val appIcon = AppUtils.getIconDrawable(this, operatePackageName)
        binding.appIcon.setImageDrawable(appIcon)
        val appName = AppUtils.getAppName(this, operatePackageName)
        binding.appName.text = appName
    }

    private fun initData() {
        operatePackageName = intent.getStringExtra("packageName")
    }
}