package com.example.glidedemo.activity

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.glidedemo.R
import com.example.glidedemo.databinding.ActivityFullNotificationBinding
import com.example.glidedemo.foreground.services.SpecialUseService
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class FullscreenNotificationActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityFullNotificationBinding.inflate(layoutInflater)
    }

    /**
     * 通知栏权限
     */
    private val postNotificationLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { _ ->

        }

    private val CHANNEL_ID = "Lock_Service_CHANNEL_ID"
    private val NOTIFICATION_TITLE = "20241128"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        initActions()
    }

    private fun initActions() {
        binding.fullNotificationActivity.setOnClickListener {
            val permissionGranted = ContextCompat.checkSelfPermission(
                this, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!permissionGranted) {
                postNotificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                lifecycleScope.launch {
                    delay(1000) // 模拟延迟
                    createNotificationChannelIfNeeded(CHANNEL_ID, NOTIFICATION_TITLE)
                    sendFullscreenNotification()
                }
            }

        }

        binding.fullNotificationService.setOnClickListener {
            val permissionGranted = ContextCompat.checkSelfPermission(
                this, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!permissionGranted) {
                postNotificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                lifecycleScope.launch {
                    delay(1000) // 模拟延迟
                    val serviceIntent =
                        Intent(this@FullscreenNotificationActivity, SpecialUseService::class.java)
                    ContextCompat.startForegroundService(
                        this@FullscreenNotificationActivity, serviceIntent
                    )
                }
            }
        }

        binding.fullNotificationPermission.setOnClickListener {
            if (!checkOverlays(this)) {
                goSetting()
            }
        }
    }

    private fun createNotificationChannelIfNeeded(channelId: String, name: String) {
        val manager = getSystemService(NotificationManager::class.java)
        if (manager.getNotificationChannel(channelId) == null) {
            val serviceChannel = NotificationChannel(
                channelId, name, NotificationManager.IMPORTANCE_HIGH // 设置为高优先级
            )
            manager.createNotificationChannel(serviceChannel)
        }
    }

    private fun sendFullscreenNotification() {
        Log.d("FullscreenNotification", "Creating notification with FullScreenIntent.")

        val builder = NotificationCompat.Builder(this, CHANNEL_ID).setSmallIcon(R.drawable.cat)
            .setContentTitle("全屏通知").setContentText("这是一条全屏通知的内容。")
            .setPriority(NotificationCompat.PRIORITY_HIGH) // 设置高优先级
            .setFullScreenIntent(fullscreenPendingIntent, true) // 设置全屏意图

        val notificationManager = NotificationManagerCompat.from(this)
        notificationManager.notify(0, builder.build()) // 使用ID 0发送通知
    }

    private val fullscreenPendingIntent: PendingIntent
        get() {
            val intent = Intent(this, TransparentActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            return PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)
        }


    fun checkOverlays(context: Context): Boolean {
        return try {
            Settings.canDrawOverlays(context)
        } catch (e: Exception) {
            false
        }
    }


    private fun goSetting() {
        try {
            Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION).apply {
                data = Uri.parse("package:${packageName}")
                settingLauncher.launch(this)
            }
        } catch (e: Exception) {

        }
    }

    private val settingLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            finish()
        }

}


