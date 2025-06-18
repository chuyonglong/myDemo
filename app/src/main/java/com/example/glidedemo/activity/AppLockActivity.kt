package com.example.glidedemo.activity

import android.app.Activity
import android.app.role.RoleManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.util.Log
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationManagerCompat
import androidx.core.net.toUri
import com.example.glidedemo.R
import com.example.glidedemo.adapter.LauncherListAdapter
import com.example.glidedemo.databinding.ActivityInstallBinding
import com.example.glidedemo.databinding.FlowlayoutTextBinding
import com.example.glidedemo.extensions.PERMISSION_STRING_TYPE
import com.example.glidedemo.extensions.goOverlayPermissionSetting
import com.example.glidedemo.extensions.goUsagePermissionSetting
import com.example.glidedemo.extensions.toast
import com.example.glidedemo.extensions.viewBindings
import com.example.glidedemo.receiver.AppInstallReceiver
import com.example.glidedemo.utils.PermissionUtil
import com.example.glidedemo.views.GalleryGridLayoutManager
import com.example.glidedemo.views.flowlayout.FlowLayout
import com.example.glidedemo.views.flowlayout.TagAdapter
import com.example.glidedemo.views.flowlayout.TagFlowLayout

class AppLockActivity : AppCompatActivity(), TagFlowLayout.OnTagClickListener,
    TagFlowLayout.OnSelectListener, LauncherListAdapter.OnLauncherClickListener {
    private val binding by viewBindings(ActivityInstallBinding::inflate)

    private val appInstallReceiver = AppInstallReceiver()

    private val launcherListAdapter by lazy {
        LauncherListAdapter(this)
    }

    // 检测厂商品牌
    private fun isXiaomi() = Build.MANUFACTURER.equals("xiaomi", ignoreCase = true)
    private fun isHuawei() = Build.MANUFACTURER.equals("huawei", ignoreCase = true)

    // 广播接收器是否注册
    private var isReceiverRegistered = false


    private val mVals by lazy {
        linkedMapOf(
            "0" to "0:查看使用情况权限",
            "1" to "1:应用安装/卸载广播",
            "2" to "2:请求通知监听权限",
            "3" to "3:忽略电池优化",
            "4" to "4:请求应用上层权限",
            "5" to "5:设置为默认启动器",
            "6" to "6:设置为默认启动器弹窗",
            "7" to "7:获取主屏幕应用",
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding
        initView()
        initFlowLayout()
    }

    private fun initFlowLayout() {
        binding.tagFlowLayout.apply {
            addAdapter(object : TagAdapter<Any>(mVals.values.toList()) {
                override fun getView(parent: FlowLayout?, position: Int, s: Any): View {
                    val tvBinding =
                        FlowlayoutTextBinding.inflate(layoutInflater, binding.tagFlowLayout, false)
                    tvBinding.root.text = s.toString()
                    return tvBinding.root
                }
            })
        }
        binding.tagFlowLayout.apply {
            setOnTagClickListener(this@AppLockActivity)
            setOnSelectListener(this@AppLockActivity)
        }
    }

    override fun onTagClick(view: View?, position: Int, parent: FlowLayout?): Boolean {
        when (position) {
            0 -> {
                val hasUsageAccess = PermissionUtil.checkUsageStats(this)
                if (hasUsageAccess) {
                    toast("查看使用情况权限已授权")
                } else {
                    toast("请求权限")
                    goUsagePermissionSetting(this, permissionUsageAccessActivityResultLauncher)
                }

            }

            1 -> {
                if (!isReceiverRegistered) {
                    toast("注册应用安装/卸载广播接收器")
                    registerPackageReceiver()
                    isReceiverRegistered = true
                } else {
                    toast("应用安装/卸载广播接收器已注册")
                }

            }

            2 -> {
                toast("请求通知监听权限")
                if (isNotificationPermissionGranted(this)) {
                    val textStr =
                        getString(R.string.permission_notification_request_result, "已授权")
                    toast(textStr)
                } else {
                    toast("通知监听未授权,请求权限")
                    openNotificationPermissionSettings(this)
                }
            }

            3 -> {
                requestDisableBatteryOptimization(this)
            }

            4 -> {
                val hasOverlayPermission = PermissionUtil.checkOverlays(this)
                if (hasOverlayPermission) {
                    toast("应用上层权限已授权")
                } else {
                    toast("请求应用上层权限")
                    goOverlayPermissionSetting(this, permissionOverlaysActivityResultLauncher)
                }
            }

            5 -> {
                setDefaultLauncher()
            }

            6 -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val roleManager = getSystemService(RoleManager::class.java)
                    val isRoleAvailable = roleManager.isRoleAvailable(RoleManager.ROLE_HOME)

                    if (isRoleAvailable) {
                        val intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_HOME)
                        activityResultLauncher.launch(intent)
                    }
                }

            }

            7 -> {
                val homeLaunchers = getAllHomeLaunchers(this)
                if (homeLaunchers.isNotEmpty()) {
                    val launcherNames = homeLaunchers.joinToString(", ") {
                        it.activityInfo.loadLabel(packageManager).toString()
                    }
                    toast("主屏幕应用: $launcherNames")

                    binding.launcherAppList.adapter = launcherListAdapter
                    binding.launcherAppList.layoutManager = GalleryGridLayoutManager(this, 3)
                    launcherListAdapter.setOnLauncherClickListener(this)
                    launcherListAdapter.addLauncherList(homeLaunchers.toMutableList())
                } else {
                    toast("没有找到主屏幕应用")
                }

            }

        }
        return true
    }


    private val activityResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        }


    private fun getAllHomeLaunchers(context: Context): List<ResolveInfo> {
        val intent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_HOME)
        }
        val list = context.packageManager.queryIntentActivities(intent, 0)
        val finalList =list.filter {
            it.activityInfo.packageName != "com.android.settings"
        }
        return finalList
    }

    private fun initView() {

    }


    private fun registerPackageReceiver() {
        unregisterReceiver(appInstallReceiver)
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
    }


    private val permissionUsageAccessActivityResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val hasUsagePermission = PermissionUtil.checkUsageStats(this)
                if (hasUsagePermission) {
                    toast(getString(R.string.permission_request_result, "已授权"))
                } else {
                    toast(getString(R.string.permission_request_result, "未授权"))
                }
            }

        }

    private val permissionOverlaysActivityResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val hasOverlayPermission = PermissionUtil.checkOverlays(this)
                if (hasOverlayPermission) {
                    toast(getString(R.string.permission_overlay_request_result, "已授权"))
                } else {
                    toast(getString(R.string.permission_overlay_request_result, "未授权"))
                }
            }

        }


    override fun onSelected(selectPosSet: Set<Int>?) {}


    // 检查通知监是否已授权
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
                toast("请到系统设置中开启通知监听权限")
            }
        }
    }


    /**
     * 检查当前应用是否已被排除在电池优化之外
     */
    private fun isIgnoringBatteryOptimizations(context: Context): Boolean {
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        return powerManager.isIgnoringBatteryOptimizations(context.packageName)
    }


    private fun requestDisableBatteryOptimization(activity: Activity) {
        if (!isIgnoringBatteryOptimizations(this)) {
            toast("请求禁用电池优化")
            val intent = Intent().apply {
                action = Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
                data = "package:$packageName".toUri()
            }
            activity.startActivity(intent)
        } else {
            toast("电池优化已禁用")
        }
    }

    private fun setDefaultLauncher() {
        try {
            // 先启用HomeBootActivity
            packageManager.setComponentEnabledSetting(
                ComponentName(packageName, "com.example.glidedemo.activity.HomeBootActivity"),
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP
            )

            // 跳转到默认启动器设置页面
            goDefaultLauncherSetting()


        } catch (e: Exception) {
            toast("无法打开启动器设置页面")
        }
    }


    private fun goDefaultLauncherSetting() {
        val intent = Intent(
            this@AppLockActivity, PermissionSettingActivity::class.java
        ).apply {
            putExtra(PERMISSION_STRING_TYPE, Settings.ACTION_HOME_SETTINGS)
        }
        permissionDefaultLauncherActivityResultLauncher.launch(intent)
    }

    private val permissionDefaultLauncherActivityResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->


        }

    override fun onLauncherClick(resolveInfo: ResolveInfo) {
        launchApp(resolveInfo)
    }


    private fun launchApp(resolveInfo: ResolveInfo) {
        val componentName = ComponentName(
            resolveInfo.activityInfo.packageName,
            resolveInfo.activityInfo.name
        )
        val intent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
            component = componentName
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        startActivity(intent)
    }

}