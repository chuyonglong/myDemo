package com.example.glidedemo.foreground.services

import android.app.Notification
import android.content.Intent
import android.os.CountDownTimer
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.glidedemo.R
import com.example.glidedemo.base.BaseService

/**
 * android.permission.FOREGROUND_SERVICE_CAMERA
 * android12
 *用于标识一个前台服务涉及摄像头操作。它适用于需要持续使用摄像头进行图像捕捉或视频录制的应用程序。
 */
class CameraService : BaseService() {
    private var timer: CountDownTimer? = null
    private var timeInSeconds: Long = 0

    companion object {
        private const val CHANNEL_ID = "CameraServiceChannel"
        private const val NOTIFICATION_ID = 1
        private const val NOTIFICATION_NAME = "camera通知"
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel(CHANNEL_ID, NOTIFICATION_NAME)
        val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Camera Service")
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_DEFAULT)//立即出通知
            .setContentText("Camera is running...")
            .setSmallIcon(R.drawable.ic_camera)
            .build()
        startForeground(NOTIFICATION_ID, notification)
//        val manager = getSystemService(NotificationManager::class.java)
//        manager.notify(123, notification)
////        startTimer()
    }


    private fun startTimer() {
        timer = object : CountDownTimer(Long.MAX_VALUE, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                timeInSeconds++
                Log.d("223366", "onCreate: ---${timeInSeconds}")
                val updatedNotification = NotificationCompat.Builder(
                    this@CameraService,
                    CHANNEL_ID
                )
                    .setContentTitle("Camera Service")
                    .setContentText("Camera running: $timeInSeconds seconds")
                    .setSmallIcon(R.drawable.ic_camera)
                    .build()
//                val manager = getSystemService(NotificationManager::class.java)
//                manager.notify(123, updatedNotification)
                startForeground(NOTIFICATION_ID, updatedNotification)
            }

            override fun onFinish() {
            }
        }

        timer?.start()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()

    }


}
