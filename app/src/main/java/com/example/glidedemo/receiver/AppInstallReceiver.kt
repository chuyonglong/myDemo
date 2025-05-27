package com.example.glidedemo.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.glidedemo.App
import com.example.glidedemo.activity.AppInstallDialogActivity
import com.example.glidedemo.extensions.toast

// 自定义广播接收器
class AppInstallReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val packageName = intent.data?.schemeSpecificPart
        Log.d("AppInstall", "intent.action--: ${intent.action}")
        when (intent.action) {
            Intent.ACTION_PACKAGE_ADDED -> {
                Log.d("AppInstall", "开始安装--: $packageName")
                val isReplacing = intent.getBooleanExtra(Intent.EXTRA_REPLACING, false)
                if (!isReplacing) {
                    Log.d("AppInstall", "新安装: $packageName")
                    App.getContext().toast("应用 $packageName 已安装")
                    if (packageName == null) return

                    Log.d("AppInstall", "准备启动TransparentActivity")
                    try {
                        val activityIntent =
                            Intent(context, AppInstallDialogActivity::class.java).apply {
                                putExtra("packageName", packageName)
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                addFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
                            }
                        Log.d("AppInstall", "启动TransparentActivity: $activityIntent")
                        context.startActivity(activityIntent)
                        Log.d("AppInstall", "TransparentActivity已启动")
                    } catch (e: Exception) {
                        Log.e("AppInstall", "启动TransparentActivity失败: ${e.message}")
                        e.printStackTrace()
                    }


                }
            }

            Intent.ACTION_PACKAGE_REMOVED -> {
                val isReplacing = intent.getBooleanExtra(Intent.EXTRA_REPLACING, false)
                Log.d("AppInstall", "开始卸载: $packageName")
                if (!isReplacing) {
                    if (packageName != null) {
                        Log.d("AppInstall", "完全卸载: $packageName")
                        context.toast("应用 $packageName 已卸载")
                    }
                }
            }
        }
    }
}