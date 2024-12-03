package com.example.glidedemo.activity

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.glidedemo.R
import com.example.glidedemo.databinding.ActivityFullNotificationBinding
import com.example.glidedemo.extensions.toast
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
            // 检查通知权限
            val permissionGranted =
                Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU || ContextCompat.checkSelfPermission(
                    this, Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED

            if (!permissionGranted) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    postNotificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            } else {
                lifecycleScope.launch {
                    try {
                        delay(1000) // 模拟延迟
                        val serviceIntent = Intent(
                            this@FullscreenNotificationActivity, SpecialUseService::class.java
                        )
                        ContextCompat.startForegroundService(
                            this@FullscreenNotificationActivity, serviceIntent
                        )
                    } catch (e: Exception) {
                        toast("启动服务失败: ${e.message}")
                    }
                }
            }
        }


        binding.fullNotificationPermission.setOnClickListener {
            if (!checkOverlays(this)) {
                goSetting()
            }
        }

        binding.fullNotificationCustom.setOnClickListener {
            val channelId = "custom_notification_channel"
            val notificationId = 1234
            // 创建通知渠道（如果还没创建）
            createNotificationChannelIfNeeded(channelId, "Custom Notifications")
            // 创建一张大图片的样式
            val bigPictureStyle = NotificationCompat.BigPictureStyle()
                .bigPicture(BitmapFactory.decodeResource(resources, R.drawable.ic_lite_photo)) // 替换为你的图片资源
                .bigLargeIcon(BitmapFactory.decodeResource(resources, R.drawable.cat)) // 可以设置大图标为null

            // 创建通知
            val builder = NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.cat) // 设置图标
                .setContentTitle("自定义通知") // 设置标题
                .setContentText("这是一个自定义通知内容") // 设置内容
                .setStyle(bigPictureStyle)
                .setPriority(NotificationCompat.PRIORITY_HIGH) // 设置优先级
                .setDefaults(Notification.DEFAULT_ALL) // 设置默认通知声音、振动等

            // 显示通知
            val notificationManager = NotificationManagerCompat.from(this)
            notificationManager.notify(notificationId, builder.build())
        }

    }

    private fun createNotificationChannelIfNeeded(channelId: String, name: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                channelId, name, NotificationManager.IMPORTANCE_HIGH
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }

    }

    private fun sendFullscreenNotification() {
        val builder = NotificationCompat.Builder(this, CHANNEL_ID).setSmallIcon(R.drawable.cat)
            .setContentTitle("全屏通知").setContentText("这是一条全屏通知的内容。")
            .setPriority(NotificationCompat.PRIORITY_HIGH) // 设置高优先级
            .setFullScreenIntent(fullscreenPendingIntent, true) // 设置全屏意图

        val notificationManager = NotificationManagerCompat.from(this)
        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            toast("没有通知栏权限!")
            return
        }
        notificationManager.notify(0, builder.build()) // 使用ID 0发送通知
    }

    private val fullscreenPendingIntent: PendingIntent
        get() {
            val intent = Intent(this, TransparentActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            return PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)
        }


    private fun checkOverlays(context: Context): Boolean {
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


