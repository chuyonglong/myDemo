package com.example.glidedemo.base

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.os.Build

abstract class BaseService : Service() {


    fun createNotificationChannel(channelId: String, name: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                channelId,
                name,
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(NotificationManager::class.java)
//            manager.getNotificationChannel()
            manager.createNotificationChannel(serviceChannel)
        }

    }

}