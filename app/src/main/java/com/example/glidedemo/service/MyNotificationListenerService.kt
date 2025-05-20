package com.example.glidedemo.service

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.example.glidedemo.App

class MyNotificationListenerService : NotificationListenerService() {

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        // 处理收到的通知
        val packageName = sbn.packageName
        val extras = sbn.notification.extras
        val title = extras.getString(Notification.EXTRA_TITLE)
        val text = extras.getString(Notification.EXTRA_TEXT)
        Log.d("Notification", "来自 $packageName 的通知: $title - $text")


        // 2.取消原始通知
        cancelNotification(sbn.key)

    }

    override fun onListenerConnected() {
        Log.d("Notification", "通知监听服务已连接")
    }
}