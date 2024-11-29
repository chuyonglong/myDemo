package com.example.glidedemo.activity

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.lifecycleScope
import com.example.glidedemo.R
import com.example.glidedemo.databinding.ActivityFullNotificationBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class FullscreenNotificationActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityFullNotificationBinding.inflate(layoutInflater)
    }

    private val CHANNEL_ID = "Lock_Service_CHANNEL_ID"
    private val NOTIFICATION_TITLE = "20241128"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        initActions()
    }

    private fun initActions() {
        binding.fullNotificationTitle.setOnClickListener {
            lifecycleScope.launch {
                delay(2000) // 模拟延迟
                createNotificationChannelIfNeeded(CHANNEL_ID, NOTIFICATION_TITLE)
                sendFullscreenNotification()
            }
        }
    }

    private fun createNotificationChannelIfNeeded(channelId: String, name: String) {
        val manager = getSystemService(NotificationManager::class.java)
        if (manager.getNotificationChannel(channelId) == null) {
            val serviceChannel = NotificationChannel(
                channelId,
                name,
                NotificationManager.IMPORTANCE_HIGH // 设置为高优先级
            )
            manager.createNotificationChannel(serviceChannel)
        }
    }

    private fun sendFullscreenNotification() {
        Log.d("FullscreenNotification", "Creating notification with FullScreenIntent.")

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.cat)
            .setContentTitle("全屏通知")
            .setContentText("这是一条全屏通知的内容。")
            .setPriority(NotificationCompat.PRIORITY_HIGH) // 设置高优先级
            .setFullScreenIntent(fullscreenPendingIntent, true) // 设置全屏意图

        val notificationManager = NotificationManagerCompat.from(this)
        notificationManager.notify(0, builder.build()) // 使用ID 0发送通知
    }

    private val fullscreenPendingIntent: PendingIntent
        get() {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            return PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)
        }


//    private val CHANNEL_ID = "Lock_Service_CHANNEL_ID"
//
//    private val binding by lazy {
//        ActivityFullNotificationBinding.inflate(layoutInflater)
//    }
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(binding.root)
//
//        // 在 Activity 中初始化操作
//        sendFullscreenNotification()
//    }
//
//    private fun sendFullscreenNotification() {
//        // 创建通知渠道（如果需要）
//        createNotificationChannel(CHANNEL_ID, "全屏通知")
//
//        // 创建 PendingIntent，启动全屏的 Activity
//        val fullscreenIntent = PendingIntent.getActivity(
//            this,
//            0,
//            Intent(this, MainActivity::class.java),  // 启动的 Activity
//            PendingIntent.FLAG_IMMUTABLE
//        )
//
//        // 创建通知
//        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
//            .setSmallIcon(R.drawable.cat)
//            .setContentTitle("全屏通知")
//            .setContentText("这是一个全屏通知")
//            .setPriority(NotificationCompat.PRIORITY_HIGH)  // 设置高优先级
//            .setFullScreenIntent(fullscreenIntent, true)    // 设置全屏意图
//            .setAutoCancel(true)                            // 点击后自动取消
//            .setCategory(NotificationCompat.CATEGORY_ALARM) // 设置为警报类通知
//
//        // 获取通知管理器并发送通知
//        val notificationManager = NotificationManagerCompat.from(this)
//        notificationManager.notify(0, builder.build()) // 使用ID 0发送通知
//    }
//
//    // 创建通知渠道
//    private fun createNotificationChannel(channelId: String, channelName: String) {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            val channel = NotificationChannel(
//                channelId,
//                channelName,
//                NotificationManager.IMPORTANCE_HIGH
//            ).apply {
//                description = "用于展示全屏通知"
//            }
//
//            // 注册通知渠道
//            val notificationManager: NotificationManager =
//                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//            notificationManager.createNotificationChannel(channel)
//        }
//    }
}


