package com.example.glidedemo.foreground.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.glidedemo.R
import com.example.glidedemo.activity.TransparentActivity
import com.example.glidedemo.base.BaseService
import java.lang.Thread.sleep
import kotlin.concurrent.thread

/**
 * android.permission.FOREGROUND_SERVICE_SPECIAL_USE
 * android 12
 * 如一些自定义的后台任务
 * 用于标识一个前台服务涉及特殊用途的任务，这些任务通常不适合归类到其他现有的 foregroundServiceType 中。它为那些需要持续运行但不适合常规分类的前台服务提供了一种灵活的声明方式。
 */
class SpecialUseService : BaseService() {
    private val CHANNEL_ID = "FULLSCREEN_SERVICE_CHANNEL_ID"
    private val CHANNEL_ID_NOTIFICATION = "FULLSCREEN_SERVICE_CHANNEL_ID_NOTIFICATION"
    private val NOTIFICATION_TITLE = "20241202"
    private val NOTIFICATION_ID = 112233
    private val NOTIFICATION_NAME = "特殊服务通知"

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onCreate()
        createNotificationChannel(CHANNEL_ID, NOTIFICATION_NAME)
        val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("特殊权限的服务正在运行中")
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_DEFAULT)//立即出通知
            .setContentText("特殊权限服务内容")
            .setSmallIcon(R.drawable.ic_camera)
            .build()
        startForeground(NOTIFICATION_ID, notification)
        thread {
            Log.d("223366", "onCreate: 特殊服务启动全屏通知")
            sleep(2000)
            createNotificationChannelIfNeeded(CHANNEL_ID_NOTIFICATION, NOTIFICATION_TITLE)
            sendFullscreenNotification()
        }
        return START_STICKY
    }

    private fun createNotificationChannelIfNeeded(channelId: String, name: String) {
        val manager = getSystemService(NotificationManager::class.java)
        if (manager.getNotificationChannel(channelId) != null) {
            manager.deleteNotificationChannel(channelId)
        }
        val serviceChannel = NotificationChannel(
            channelId,
            name,
            NotificationManager.IMPORTANCE_HIGH // 设置为高优先级
        )
        manager.createNotificationChannel(serviceChannel)
    }

    private fun sendFullscreenNotification() {
        val builder = NotificationCompat.Builder(this, CHANNEL_ID_NOTIFICATION)
            .setSmallIcon(R.drawable.cat)
            .setContentTitle("服务全屏通知")
            .setContentText("这是一条服务全屏通知的内容。")
            .setPriority(NotificationCompat.PRIORITY_MAX) // 设置高优先级
            .setFullScreenIntent(fullscreenPendingIntent, true) // 设置全屏意图

        val notificationManager = NotificationManagerCompat.from(this)
        notificationManager.notify(0, builder.build()) // 使用ID 0发送通知
    }

    private val fullscreenPendingIntent: PendingIntent
        get() {
            val intent = Intent(this, TransparentActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            return PendingIntent.getActivity(
                this,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }
}