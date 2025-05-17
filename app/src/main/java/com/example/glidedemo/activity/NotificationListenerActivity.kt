package com.example.glidedemo.activity

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationManagerCompat
import com.example.glidedemo.R
import com.example.glidedemo.databinding.ActivityNotificationListenerBinding
import com.example.glidedemo.extensions.toast
import com.example.glidedemo.extensions.viewBindings

class NotificationListenerActivity : AppCompatActivity() {

    // 检测厂商品牌
    private fun isXiaomi() = Build.MANUFACTURER.equals("xiaomi", ignoreCase = true)
    private fun isHuawei() = Build.MANUFACTURER.equals("huawei", ignoreCase = true)

    private val binding by viewBindings(ActivityNotificationListenerBinding::inflate)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding
        initView()
        initActions()

    }

    private fun initView() {
        if (isNotificationPermissionGranted(this)) {
            binding.permissionResultText.text =
                getString(R.string.permission_request_result, "通知监听已授权")
        } else {
            binding.permissionResultText.text =
                getString(R.string.permission_request_result, "无通知监听权限,请授予权限")
        }
    }

    private fun initActions() {
        binding.requestPermissionText.setOnClickListener {
            if (isNotificationPermissionGranted(this)) {
                val textStr = getString(R.string.permission_request_result, "通知监听已授权")
                binding.permissionResultText.text = textStr
                toast(textStr)
                return@setOnClickListener
            }
            toast("通知监听未授权,请求权限")
            openNotificationPermissionSettings(this)
        }

    }

    // 检查是否已授权
    private fun isNotificationPermissionGranted(context: Context): Boolean {
        return NotificationManagerCompat.getEnabledListenerPackages(context)
            .contains(context.packageName)
    }

    // 跳转到权限设置页
    private fun openNotificationPermissionSettings(context: Context) {
        try {
            // 通用跳转方式（原生 Android）
            context.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
        } catch (e: Exception) {
            // 处理厂商定制系统（如小米、华为）
            handleVendorSettings(context)
        }
    }

    // 处理厂商定制系统
    private fun handleVendorSettings(context: Context) {
        when {
            isXiaomi() -> {
                // 小米跳转到特定设置页
                Intent().apply {
                    component = ComponentName(
                        "com.android.settings",
                        "com.android.settings.Settings\$NotificationFilterActivity"
                    )
                    context.startActivity(this)
                }
            }

            isHuawei() -> {
                // 华为跳转到通知管理
                Intent().apply {
                    component = ComponentName(
                        "com.huawei.systemmanager",
                        "com.huawei.notificationmanager.ui.NotificationManagmentActivity"
                    )
                    context.startActivity(this)
                }
            }
            // 其他厂商处理...
            else -> {
                Toast.makeText(context, "请到系统设置中开启通知监听权限", Toast.LENGTH_LONG).show()
            }
        }
    }


}