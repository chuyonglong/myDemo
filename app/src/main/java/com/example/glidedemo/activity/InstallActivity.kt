package com.example.glidedemo.activity

import android.app.Activity
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.glidedemo.R
import com.example.glidedemo.databinding.ActivityInstallBinding
import com.example.glidedemo.extensions.goUsagePermissionSetting
import com.example.glidedemo.extensions.toast
import com.example.glidedemo.extensions.viewBindings
import com.example.glidedemo.receiver.AppInstallReceiver
import com.example.glidedemo.utils.PermissionUtil

class InstallActivity : AppCompatActivity() {
    private val binding by viewBindings(ActivityInstallBinding::inflate)

    private val appInstallReceiver = AppInstallReceiver()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding
        initView()
        initActions()
    }

    private fun initActions() {
        binding.requestPermissionText.setOnClickListener {
            toast("请求权限")
            goUsagePermissionSetting(this, permissionUsageAccessActivityResultLauncher)
        }
        binding.registerReceiverButton.setOnClickListener {
            toast("注册应用安装/卸载广播接收器")
            registerPackageReceiver()
        }
    }

    private fun initView() {
        val hasUsageAccess = PermissionUtil.checkUsageStats(this)
        if (hasUsageAccess) {
            binding.permissionResultText.text =
                getString(R.string.permission_request_result, "查看使用情况权限已授权")
        } else {
            binding.permissionResultText.text =
                getString(R.string.permission_request_result, "无查看使用情况权限,请授予权限")
        }
    }


    private fun registerPackageReceiver() {
        val intentFilter = IntentFilter().apply {
            addAction(Intent.ACTION_PACKAGE_ADDED)
            addAction(Intent.ACTION_PACKAGE_REMOVED)
            addAction(Intent.ACTION_PACKAGE_REPLACED)
            addDataScheme("package")
        }

        // 注册接收器
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(appInstallReceiver, intentFilter, RECEIVER_EXPORTED)
        } else {
            registerReceiver(appInstallReceiver, intentFilter)
        }

        Log.d("AppInstall", "应用安装/卸载广播接收器已注册")
    }


    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(appInstallReceiver)
    }


    private val permissionUsageAccessActivityResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val hasUsagePermission = PermissionUtil.checkUsageStats(this)
                if (hasUsagePermission) {
                    binding.permissionResultText.text =
                        getString(R.string.permission_request_result, "查看使用情况权限已授权")
                } else {
                    binding.permissionResultText.text =
                        getString(
                            R.string.permission_request_result,
                            "无查看使用情况权限,请授予权限"
                        )
                }
            }

        }
}