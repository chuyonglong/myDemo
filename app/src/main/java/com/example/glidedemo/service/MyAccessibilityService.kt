package com.example.glidedemo.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.accessibilityservice.AccessibilityServiceInfo.FEEDBACK_GENERIC
import android.accessibilityservice.AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS
import android.accessibilityservice.AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS
import android.accessibilityservice.AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.example.glidedemo.activity.AccessibilityAutoActivity.Companion.EXTRA_PACKAGE_NAME
import com.example.glidedemo.activity.AccessibilityAutoActivity.Companion.EXTRA_SERVICE_DATA
import com.example.glidedemo.extensions.ACTION_ACTIVITY_TO_SERVICE
import com.example.glidedemo.extensions.ACTION_SERVICE_TO_ACTIVITY

class MyAccessibilityService : AccessibilityService() {

    private val lastClickTimes = mutableMapOf<String, Long>() // 存储不同按钮的上次点击时间
    private var currentPackages = mutableSetOf<String>() // 本地维护当前监听的包名


    /**
     * 接收来自Activity的更新广播
     */
    private val activityReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val packageName = intent.getStringExtra(EXTRA_PACKAGE_NAME) ?: return
            addTargetPackage(packageName)
            // 执行来自Activity的命令
            Log.d("223366", "onTargetPackageUpdate: 就接到 $packageName 更新广播了")
        }
    }


    /**
     * 发送数据到Activity
     */
    private fun sendDataToActivity(data: String) {
        val intent = Intent(ACTION_SERVICE_TO_ACTIVITY).apply {
            putExtra(EXTRA_SERVICE_DATA, data)
            // 关键点：设置接收方包名（限制广播范围）
            setPackage("com.example.glidedemo.a") // 替换为你的应用包名
        }
        // 发送带权限的广播（需与接收端一致）
        sendBroadcast(intent, "com.example.BROADCAST_PERMISSION")
        Log.d("223366", "服务已发送数据：$data")
    }


    override fun onServiceConnected() {
        val filter = IntentFilter(ACTION_ACTIVITY_TO_SERVICE)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(
                activityReceiver,
                filter,
                "com.example.BROADCAST_PERMISSION",
                null,
                RECEIVER_NOT_EXPORTED
            )
        } else {
            registerReceiver(activityReceiver, filter, "com.example.BROADCAST_PERMISSION", null)
        }
        Log.d("223366", "onServiceConnected: ----onServiceConnected")
        // 配置服务信息（根据需要配置 event 类型、反馈等
        val info = AccessibilityServiceInfo().apply {
            eventTypes =
                AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED or AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
            feedbackType = FEEDBACK_GENERIC
            notificationTimeout = 100
            flags =
                FLAG_REPORT_VIEW_IDS or FLAG_INCLUDE_NOT_IMPORTANT_VIEWS or FLAG_RETRIEVE_INTERACTIVE_WINDOWS
        }
        serviceInfo = info
    }

    override fun onDestroy() {
        unregisterReceiver(activityReceiver)
        super.onDestroy()
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        val pkg = event.packageName?.toString() ?: return

        if (pkg !in currentPackages) return

        when (event.eventType) {
            AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED, AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> {
                findAndClickClearCacheButton(event.source)
            }
        }
    }

    override fun onInterrupt() {
        Log.d("Accessibility", "服务中断")
    }

    private fun findAndClickClearCacheButton(node: AccessibilityNodeInfo?) {
        node ?: return

        try {
            // 检查是否是“存储”入口
            if (isStorageEntry(node)) {
                Log.d("Accessibility", "进入存储设置")
                val name = node.text?.toString() ?: ""
                safePerformClick(node.parent, name)
                return
            }

            // 检查是否是“清除缓存”按钮
            if (isClearCacheButton(node)) {
                Log.d("Accessibility", "点击清除缓存")
                val name = node.text?.toString() ?: ""
                safePerformClick(node, name)
                removeTargetPackage("com.android.settings") // 操作完成后移除监听
                lastClickTimes.clear() // 清空上次点击时间
                sendDataToActivity("清除缓存成功")
                return
            }

            // 递归遍历子节点
            for (i in 0 until node.childCount) {
                findAndClickClearCacheButton(node.getChild(i))
            }
        } finally {
            try {
                node.recycle() // 释放资源
            } catch (e: Exception) {
                // 忽略已回收异常
            }
        }
    }

    private fun isStorageEntry(node: AccessibilityNodeInfo): Boolean {
        val nodeText = node.text?.toString() ?: ""
        return node.viewIdResourceName?.contains("storage_settings") == true || nodeText.contains("存储空间和缓存") || nodeText.contains(
            "存储占用"
        )
    }

    private fun isClearCacheButton(node: AccessibilityNodeInfo): Boolean {
        val nodeText = node.text?.toString() ?: ""
        return (node.className == "android.widget.Button" && nodeText.contains("清除缓存") || nodeText.contains(
            "缓存"
        )) || node.viewIdResourceName?.contains("clear_cache_button") == true
    }

    /**
     * 安全点击按钮，确保同一个按钮至少间隔 3 秒
     */
    private fun safePerformClick(node: AccessibilityNodeInfo?, key: String) {
        node?.let {
            val currentTime = System.currentTimeMillis()
            val lastClickTime = lastClickTimes[key] ?: 0
            if (lastClickTime == 0L || currentTime - lastClickTime > 3000) {  // 3 秒内不重复点击
                it.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                lastClickTimes[key] = currentTime // 更新点击时间
            }
        }
    }

    private fun updateTargetPackages(packages: List<String>) {
        currentPackages = packages.toMutableSet() // 更新本地集合
        serviceInfo = serviceInfo.apply {
            packageNames = packages.toTypedArray()
        }
        setServiceInfo(serviceInfo)
        Log.d("Accessibility", "当前监听包名: ${currentPackages.joinToString()}")
    }

    fun addTargetPackage(pkg: String) {
        val newList = serviceInfo.packageNames?.toMutableList() ?: mutableListOf()
        if (!newList.contains(pkg)) {
            newList.add(pkg)
            updateTargetPackages(newList)
        }
    }

    private fun removeTargetPackage(pkg: String) {
        currentPackages.remove(pkg)
        updateTargetPackages(currentPackages.toList())
    }


}
