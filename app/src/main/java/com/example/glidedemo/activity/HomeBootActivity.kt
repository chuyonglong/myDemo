package com.example.glidedemo.activity

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.glidedemo.R
import com.example.glidedemo.databinding.ActivityHomeBootBinding
import com.example.glidedemo.extensions.toast
import com.example.glidedemo.extensions.viewBindings
import com.example.glidedemo.foreground.services.SpecialUseService
import com.example.glidedemo.utils.AppUtils
import kotlinx.coroutines.launch

class HomeBootActivity : AppCompatActivity() {

    private val TAG = "HomeBootActivity"

    private val CHANNEL_ID = "Lock_Service_CHANNEL_ID"
    private val NOTIFICATION_TITLE = "20241128"

    private val binding by viewBindings(ActivityHomeBootBinding::inflate)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding
        Log.d(TAG, "onCreate: HomeBootActivity-onCreate")
//        sendNotificationByService()
//        switchLauncher()

        finish()
    }

    private fun switchLauncher() {
        val setPackageName = AppUtils.oldActivityInfo?.packageName
        val setLauncherName = AppUtils.oldActivityInfo?.name
        if (setPackageName == null || setLauncherName == null) {
            Log.d(TAG, "switchLauncher: oldActivityInfo is null")
            return
        }
        val componentName = ComponentName(
            setPackageName, setLauncherName
        )
        AppUtils.startWithComponent(this, componentName)
    }


    /**
     * 直接启动通知
     */
    private fun sendNotification() {
        val permissionGranted = ContextCompat.checkSelfPermission(
            this, Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
        if (!permissionGranted) {
            postNotificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            lifecycleScope.launch {
                createNotificationChannelIfNeeded(CHANNEL_ID, NOTIFICATION_TITLE)
                sendFullscreenNotification()
            }
        }
    }


    /**
     * 通知栏权限
     */
    private val postNotificationLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { _ ->

        }


    private fun createNotificationChannelIfNeeded(channelId: String, name: String) {
        val serviceChannel = NotificationChannel(
            channelId, name, NotificationManager.IMPORTANCE_HIGH////弹出全屏只跟这个优先级有关系
        )
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(serviceChannel)

    }


    private fun sendFullscreenNotification() {
        val builder = NotificationCompat.Builder(this, CHANNEL_ID).setSmallIcon(R.drawable.cat)
            .setContentTitle("主屏幕应用发送通知").setContentText("主屏幕应用finish之前发送通知。")
            .setFullScreenIntent(fullscreenPendingIntent, true) // 设置全屏意图

        val notificationManager = NotificationManagerCompat.from(this)
        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            postNotificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            return
        }
        notificationManager.notify(0, builder.build()) // 使用ID 0发送通知
    }


    private val fullscreenPendingIntent: PendingIntent
        get() {
            val intent = Intent(this, FullScreenTaskNotificationActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            return PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)
        }


    /**
     * 服务启动全屏通知
     */
    private fun sendNotificationByService() {
        // 检查通知权限
        val permissionGranted =
            Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU || ContextCompat.checkSelfPermission(
                this, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED

        if (!permissionGranted) {
            postNotificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            lifecycleScope.launch {
                try {
                    val serviceIntent = Intent(
                        this@HomeBootActivity, SpecialUseService::class.java
                    )
                    ContextCompat.startForegroundService(
                        this@HomeBootActivity, serviceIntent
                    )
                } catch (e: Exception) {
                    toast("启动服务失败: ${e.message}")
                }
            }
        }
    }


    fun startWithComponent(context: Context, componentName: ComponentName) {
        val intent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
            component = componentName
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }

}