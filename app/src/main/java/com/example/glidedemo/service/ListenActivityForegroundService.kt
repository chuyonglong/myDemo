package com.example.glidedemo.service

import android.app.Notification
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.glidedemo.App
import com.example.glidedemo.R
import com.example.glidedemo.activity.MainActivity
import com.example.glidedemo.base.BaseService
import com.example.glidedemo.extensions.toast
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.text.toLong

/**
 * 类型为 dataSyncService 的服务
 */
class ListenActivityForegroundService : BaseService() {

    private val TAG = "ListenActivityForegroundService"
    private val CHANNEL_ID = "ListenActivityForegroundServiceChannel"
    private val NOTIFICATION_ID = 1234
    private val NOTIFICATION_NAME = "ListenActivityForegroundService通知"


    companion object {
        const val START_IN_FOREGROUND = "start_in_foreground"
        const val START_IN_BACKGROUND = "start_in_background"

        fun startByAction(context: Context, actionMessage: String, delay: String, clickTime: Long) {
            try {
                val intent = Intent(context, ListenActivityService::class.java).apply {
                    action = actionMessage
                    putExtra("delay", delay.toLong() * 1000L)
                    putExtra("clickTime", clickTime)
                }

                context.startService(intent)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel(
            CHANNEL_ID, NOTIFICATION_NAME
        )
        val notification: Notification = NotificationCompat.Builder(
            this, CHANNEL_ID
        ).setContentTitle("ListenActivityForegroundService Service")
            //立即出通知
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_DEFAULT)
            .setContentText("ListenActivityForegroundService is running...")
            .setSmallIcon(R.drawable.ic_camera).build()
        startForeground(NOTIFICATION_ID, notification)
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val delay = intent?.getLongExtra("delay", 1000L) ?: 1000L
        val clickTime = intent?.getLongExtra("clickTime", 0L) ?: 0L

        Log.d(TAG, "onStartCommand: delay=$delay, clickTime=$clickTime")
        when (intent?.action) {
            START_IN_FOREGROUND -> {
                delayStartMainActivity(delay, clickTime)
            }

            START_IN_BACKGROUND -> {
                App.getInstance().finishAllActivity()
                delayStartMainActivity(delay, clickTime)
            }
        }

        return super.onStartCommand(intent, flags, startId)
    }


    private fun delayStartMainActivity(delay: Long, clickTime: Long) {
        MainScope().launch {
            var logTime = 0
            // BAL_ALLOW_GRACE_PERIOD  宽限期  10秒
            withTimeoutOrNull(delay) {
                while (true) {
                    delay(1000)
                    logTime += 1000
                    Log.d(TAG, "delayStartMainActivity:----logTime=$logTime ")
                }
            } ?: also {
                val intent =
                    Intent(this@ListenActivityForegroundService, MainActivity::class.java).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                toast("启动activity成功--")
                try {
                    val currentTime = System.currentTimeMillis()
                    Log.d(TAG, "从点击到启动activity耗时:${currentTime - clickTime}  ")
                    this@ListenActivityForegroundService.startActivity(intent)
                } catch (e: Exception) {
                    toast("启动失败--${e.message}")
                }
            }
        }
    }
}