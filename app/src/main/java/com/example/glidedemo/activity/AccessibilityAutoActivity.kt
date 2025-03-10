package com.example.glidedemo.activity

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.glidedemo.extensions.ACTION_ACTIVITY_TO_SERVICE
import com.example.glidedemo.extensions.ACTION_SERVICE_TO_ACTIVITY
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class AccessibilityAutoActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_PACKAGE_NAME = "target_package"
        const val EXTRA_SERVICE_DATA = "service_data"
    }

    private fun sendUpdateTargetPackageBroadcast(packageName: String) {
        val intent = Intent(ACTION_ACTIVITY_TO_SERVICE).apply {
            // 关键点1：显式指定接收器类（避免被其他应用拦截）
            putExtra(EXTRA_PACKAGE_NAME, packageName)
            setPackage("com.example.glidedemo.a")
        }

        // 关键点2：添加权限保护（可选）
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Log.d("223366", "sendUpdateTargetPackageBroadcast: TIRAMISU-发送广播")
            sendBroadcast(intent, "com.example.BROADCAST_PERMISSION")
        } else {
            Log.d("223366", "sendUpdateTargetPackageBroadcast: TIRAMISU-低版本发送广播")
            sendBroadcast(intent)
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val packageName = intent.getStringExtra("packageName")
        if (packageName == null) {
            finishAndSetReturn()
            return
        }
        goSetting(packageName)

    }


    private fun autoCheckPermission() {
        lifecycleScope.launch {
            while (true) {
                delay(1000)

            }
        }
    }


    override fun onResume() {
        super.onResume()
        registerServiceReceiver()
    }


    private val settingLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            finishAndSetReturn()
        }

    /**
     * 跳转设置
     */
    private fun goSetting(packageName: String) {
        try {
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", packageName, null)
                settingLauncher.launch(this)
            }
            sendUpdateTargetPackageBroadcast("com.android.settings")
        } catch (e: Exception) {
        }
    }


    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        finishAndSetReturn()
    }

    private fun finishAndSetReturn() {
        val resultIntent = Intent()
        setResult(Activity.RESULT_OK, resultIntent)
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(serviceReceiver)
    }


    /**
     * 跳转自己关掉栈上面的activity
     */
    private suspend fun jumpYourself() {
        if (isFinishing || isDestroyed) return
        delay(100)
        startActivity(
            Intent(
                this@AccessibilityAutoActivity, AccessibilityAutoActivity::class.java
            )
        )
    }


    /**
     * 接收服务端数据广播
     */
    private val serviceReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val data = intent.getStringExtra(EXTRA_SERVICE_DATA)
            Log.d("223366", "Activity 收到服务数据：$data")
            // 更新 UI 或执行操作（例如显示 Toast）
            lifecycleScope.launch {
                jumpYourself()
            }
        }
    }


    fun registerServiceReceiver() {
        val filter = IntentFilter(ACTION_SERVICE_TO_ACTIVITY)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(
                serviceReceiver, filter, "com.example.BROADCAST_PERMISSION", // 权限名
                null, RECEIVER_NOT_EXPORTED // Android 13+ 需防止导出
            )
        } else {
            registerReceiver(
                serviceReceiver, filter, "com.example.BROADCAST_PERMISSION", // 权限名
                null
            )
        }
    }


}