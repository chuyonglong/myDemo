package com.example.glidedemo.foreground.services

import android.app.Notification
import android.content.Intent
import android.os.CountDownTimer
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE
import com.example.glidedemo.R
import com.example.glidedemo.base.BaseService

/**
 * android.permission.FOREGROUND_SERVICE_CONNECTED_DEVICE
 * Android 12
 * 用于标识一个前台服务涉及与连接设备的交互。具体来说，它表示该服务需要维持与一个或多个外部设备的连接，例如通过蓝牙、USB、Wi-Fi 或其他方式连接的设备。
 */
class ConnectedDeviceService : BaseService() {
    private var timer: CountDownTimer? = null
    private var timeInSeconds: Long = 0

    companion object {
        private const val CHANNEL_ID = "ConnectedDeviceServiceChannel"
        private const val NOTIFICATION_ID = 2
        private const val NOTIFICATION_NAME = "ConnectedDevice通知"
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel(CHANNEL_ID, NOTIFICATION_NAME)
        val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setForegroundServiceBehavior(FOREGROUND_SERVICE_IMMEDIATE)
            .setContentTitle("ConnectedDevice Service")
            .setContentText("ConnectedDevice is running...")
            .setSmallIcon(R.drawable.ic_camera)
            .build()
        startForeground(NOTIFICATION_ID, notification)
//        startTimer()
    }


    private fun startTimer() {
        timer = object : CountDownTimer(Long.MAX_VALUE, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                timeInSeconds++
                Log.d("223366", "onCreate: ---${timeInSeconds}")
                val updatedNotification = NotificationCompat.Builder(
                    this@ConnectedDeviceService,
                    CHANNEL_ID
                )
                    .setContentTitle("ConnectedDevice Service")
                    .setContentText("ConnectedDevice running : $timeInSeconds seconds")
                    .setSmallIcon(R.drawable.ic_camera)
                    .build()
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