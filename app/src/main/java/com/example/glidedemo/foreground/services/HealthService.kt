package com.example.glidedemo.foreground.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.glidedemo.R
import com.example.glidedemo.base.BaseService
import com.tencent.mmkv.MMKV

/**
 * android.permission.FOREGROUND_SERVICE_HEALTH
 * Android 12
 * 用于标识一个前台服务涉及健康相关的任务。这类服务主要用于处理与健康监测或健康数据相关的功能。
 */


class HealthService : BaseService(), SensorEventListener {

    private var sensorManager: SensorManager? = null
    private var stepSensor: Sensor? = null
    private var stepsensortype_gyroscope: Sensor? = null
    private var stepCount: Int = 0

    override fun onCreate() {
        super.onCreate()
        Log.d("HealthService", "Service Created")

        createNotificationChannel()

        val notification = NotificationCompat.Builder(this, "health_service_channel")
            .setContentTitle("Health Service")
            .setContentText("等待: ---")
            .setSmallIcon(R.drawable.ic_camera)
            .build()

        startForeground(1, notification)

        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        stepSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

        if (stepSensor != null) {
            sensorManager?.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_FASTEST)
            Log.d("HealthService", "步数传感器已注册")
        } else {
            Log.e("HealthService", "步数传感器未找到")
        }

//        val sensorList = sensorManager?.getSensorList(Sensor.TYPE_ALL)
//        sensorList?.forEach { sensor ->
//            Log.d("HealthService", "传感器类型: ${sensor.type}")
//        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("HealthService", "Service Destroyed")
        sensorManager?.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor == stepSensor) {
            Log.d("HealthService", "传感器数据: ${event.values.joinToString()}")
            stepCount = event.values[0].toInt()
            Log.d("HealthService", "步数更新: $stepCount")
            MMKV.defaultMMKV().encode("step_count",stepCount)

            updateNotification(stepCount)
        }


    }


    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // 处理传感器精度变化
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = "health_service_channel"
            val channelName = "Health Service Channel"
            val channelDescription = "Channel for Health Service notifications"
            val importance = NotificationManager.IMPORTANCE_LOW

            val channel = NotificationChannel(channelId, channelName, importance).apply {
                description = channelDescription
            }

            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun updateNotification(stepCount: Int) {
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val notification = NotificationCompat.Builder(this, "health_service_channel")
            .setContentTitle("Health Service")
            .setContentText("当前步数: $stepCount")
            .setSmallIcon(R.drawable.ic_camera)
            .build()

        notificationManager.notify(1, notification)
    }
}

