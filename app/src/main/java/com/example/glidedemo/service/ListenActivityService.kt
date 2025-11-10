package com.example.glidedemo.service

import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.example.glidedemo.App
import com.example.glidedemo.activity.MainActivity
import com.example.glidedemo.base.BaseService
import com.example.glidedemo.extensions.toast
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

/**
 * 普通service
 * 应用在最近列表 -可以启动activity(10秒内)--代表应用在前台
 * 应用不在最近列表 -不可以启动activity --代表应用在后台
 * 如何测试 应用列表划掉app
 */

class ListenActivityService : BaseService() {

    private val TAG = "ListenActivityService"

    companion object {
        const val START_ACTIVITY_MESSAGE_IN_FOREGROUND = "start_activity_message_in_foreground"
        const val START_ACTIVITY_MESSAGE_IN_BACKGROUND = "start_activity_message_in_background"

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
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val delay = intent?.getLongExtra("delay", 1000L) ?: 1000L
        val clickTime = intent?.getLongExtra("clickTime", 0L) ?: 0L
        when (intent?.action) {
            START_ACTIVITY_MESSAGE_IN_FOREGROUND -> {
                delayStartMainActivity(delay, clickTime)
            }

            START_ACTIVITY_MESSAGE_IN_BACKGROUND -> {
                App.getInstance().finishAllActivity()
                goHome()
                Log.d(TAG, "onStartCommand: START_ACTIVITY_MESSAGE_IN_BACKGROUND")
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
                val intent = Intent(this@ListenActivityService, MainActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                toast("启动activity成功--")
                try {
                    val currentTime = System.currentTimeMillis()
                    Log.d(TAG, "从点击到启动activity耗时:${currentTime - clickTime}  ")
                    this@ListenActivityService.startActivity(intent)
                } catch (e: Exception) {
                    toast("启动失败--${e.message}")
                }
            }
        }
    }

    /**
     * 回到桌面
     */
    private fun goHome() {
        val intent = Intent(Intent.ACTION_MAIN)
        intent.addCategory(Intent.CATEGORY_HOME)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
    }


}