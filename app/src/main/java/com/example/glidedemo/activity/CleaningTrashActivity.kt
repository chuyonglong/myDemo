package com.example.glidedemo.activity

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.provider.Settings.SettingNotFoundException
import android.text.TextUtils.SimpleStringSplitter
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.glidedemo.adapter.AppCacheListAdapter
import com.example.glidedemo.databinding.ActivityCleaningTrashBinding
import com.example.glidedemo.entity.AppCacheData
import com.example.glidedemo.extensions.PERMISSION_STRING_TYPE
import com.example.glidedemo.extensions.getAppInfoList
import com.example.glidedemo.extensions.goUsagePermissionSetting
import com.example.glidedemo.extensions.toast
import com.example.glidedemo.extensions.viewBindings
import com.example.glidedemo.service.MyAccessibilityService
import com.example.glidedemo.utils.AppCacheQuery
import com.example.glidedemo.utils.PermissionUtil
import com.example.glidedemo.views.GalleryGridLayoutManager


class CleaningTrashActivity : AppCompatActivity(), AppCacheListAdapter.OnItemClickCallback {

    private val binding by viewBindings(ActivityCleaningTrashBinding::inflate)


    private val appCacheListAdapter by lazy {
        AppCacheListAdapter()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding
        initAdapter()
        initActions()

    }

    private fun initActions() {
        binding.usagePermissionButton.setOnClickListener {
            requestPermissions()
        }

        binding.getDataButton.setOnClickListener {
            getCacheData()
        }

        binding.getAccessibilityButton.setOnClickListener {
            if (isAccessibilitySettingsOn()) {
                toast("辅助功能已开启")
            } else {
                AlertDialog.Builder(this)
                    .setTitle("开启无障碍权限")
                    .setMessage("请在【设置】->【无障碍】->【你的应用】中开启权限")
                    .setPositiveButton("去开启") { _, _ ->
                        openAccessibilitySettings()
                    }
                    .setNegativeButton("取消", null)
                    .show()

            }

        }
    }

    override fun onResume() {
        super.onResume()
//        try {
//            getCacheData()
//        } catch (e: Exception) {
//        }
    }

    private fun getCacheData() {
        val appList = AppCacheQuery.getAllAppCache(this)
//        val list = getAppInfoList(this)
//        val appList = mutableListOf<AppCacheData>()
//        for (item in list) {
//            val data = item.activityInfo
//            val appCacheData = AppCacheData(data.packageName, data.name, 0L, 0L, 0L, 0L, false)
//            appList.add(appCacheData)
//        }
        if (appList.isEmpty()) {
            toast("没有缓存数据")
        }
        appCacheListAdapter.addCacheData(appList)
        appCacheListAdapter.setOnItemClickCallback(this)
    }

    private fun initAdapter() {
        binding.appCacheList.adapter = appCacheListAdapter
        binding.appCacheList.layoutManager = GalleryGridLayoutManager(this, 1)
    }

    private fun requestPermissions() {
        val hasUsagePermission = PermissionUtil.checkUsageStats(this)
        if (hasUsagePermission) {
            //权限已经获取
            toast("权限已经获取")
            binding.permissionText.text = "权限已经获取"
        } else {
            goUsagePermissionSetting(this, permissionUsageAccessActivityResultLauncher)
        }

    }

    private val permissionUsageAccessActivityResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val hasUsagePermission = PermissionUtil.checkUsageStats(this)
                if (hasUsagePermission) {
                    toast("权限已经获取")
                    binding.permissionText.text = "权限已经获取"
                } else {
                    toast("权限获取失败")
                }
            }

        }


    override fun onItemClick(packageName: String) {
        val intent = Intent(this, AccessibilityAutoActivity::class.java).apply {
            putExtra("packageName", packageName)
        }
        startActivity(intent)
    }

    /**
     * 判断是否开启辅助功能
     */
    private fun isAccessibilitySettingsOn(): Boolean {
        var accessibilityEnabled = 0
        val service = packageName + "/" + MyAccessibilityService::class.java.getCanonicalName()
        try {
            accessibilityEnabled = Settings.Secure.getInt(
                applicationContext.contentResolver,
                Settings.Secure.ACCESSIBILITY_ENABLED
            )
        } catch (e: SettingNotFoundException) {

        }
        val mStringColonSplitter = SimpleStringSplitter(':')

        if (accessibilityEnabled == 1) {
            val settingValue = Settings.Secure.getString(
                applicationContext.contentResolver,
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            )
            if (settingValue != null) {
                mStringColonSplitter.setString(settingValue)
                while (mStringColonSplitter.hasNext()) {
                    val accessibilityService = mStringColonSplitter.next()


                    if (accessibilityService.equals(service, ignoreCase = true)) {

                        return true
                    }
                }
            }
        }

        return false
    }

    /**
     * 跳转到辅助功能设置页面
     */
    private fun openAccessibilitySettings() {
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
        startActivity(intent)
    }


}