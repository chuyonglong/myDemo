package com.example.glidedemo.activity

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.glidedemo.base.BaseActivity
import com.example.glidedemo.databinding.ActivityMainBinding
import com.example.glidedemo.databinding.FlowlayoutTextBinding
import com.example.glidedemo.extensions.PERMISSION_STRING_TYPE
import com.example.glidedemo.extensions.toast
import com.example.glidedemo.foreground.services.CameraService
import com.example.glidedemo.foreground.services.ConnectedDeviceService
import com.example.glidedemo.foreground.services.HealthService
import com.example.glidedemo.permission.GalleryPermissionUtils
import com.example.glidedemo.utils.notificationEntityDB
import com.example.glidedemo.views.flowlayout.FlowLayout
import com.example.glidedemo.views.flowlayout.TagAdapter
import com.example.glidedemo.views.flowlayout.TagFlowLayout
import com.tencent.mmkv.MMKV
import io.appmetrica.analytics.AppMetrica
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar
import kotlin.jvm.java


class MainActivity : BaseActivity(), TagFlowLayout.OnTagClickListener,
    TagFlowLayout.OnSelectListener {
    private val binding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    private val mVals by lazy {
        linkedMapOf(
            "0" to "0:相册相关",
            "1" to "1:液晶时钟",
            "2" to "2:健康",
            "3" to "3:主题切换",
            "4" to "4:跳转外部activity",
            "5" to "5:跳转lawnchiar",
            "6" to "6:点赞",
            "7" to "7:room数据库",
            "8" to "8:主屏幕应用相关",
            "9" to "9:前台服务和activity的关系",
            "10" to "10:setresult",
            "11" to "11:",
            "12" to "12:",
            "13" to "13:密码锁",
            "14" to "14:",
            "15" to "15:权限引导",
            "16" to "16:吸顶",
            "17" to "17:新手引导",
            "18" to "18:应用锁相关",
            "19" to "19:字体大小测试",
            "20" to "20:Flow 布局 ",
            "21" to "21:全屏通知",
            "22" to "22:使用桌面背景",
            "23" to "23:使tabLayout优化",
            "24" to "24:透明activity",
            "25" to "25",
            "26" to "26:",
            "27" to "27:水波纹",
            "28" to "28:主题，进入退出动画",
            "29" to "29:",
            "30" to "30:",
            "31" to "31:",
            "32" to "32:自定义view",
            "33" to "33:缓存清理",
            "34" to "34:缓存清理-文件夹权限",
        )
    }

    override fun onTagClick(view: View?, position: Int, parent: FlowLayout?): Boolean {
        Log.d("223366", "onTagClick:---onTagClick ----:${position}")

        when (position) {
            0 -> {
                startActivity(Intent(this, AlbumsActivity::class.java))
            }

            1 -> {
                val eventParameters = "{\"name\":\"Alice\", \"age\":\"999\"}"
                AppMetrica.reportEvent("New person", eventParameters)

                startActivity(Intent(this, LedClockActivity::class.java))

            }

            2 -> {
                checkPermissions()
            }

            3 -> {
                // 切换主题
                val currentTheme = AppCompatDelegate.getDefaultNightMode()
                if (currentTheme == AppCompatDelegate.MODE_NIGHT_YES || currentTheme == AppCompatDelegate.MODE_NIGHT_UNSPECIFIED) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO) // 切换到明亮模式
                    toast("切换到明亮模式")
                } else {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES) // 切换到黑暗模式
                    toast("切换到黑暗模式")
                }
            }

            4 -> {
                //自定义权限启动外部activity
                toast("测试用,没有外部activity")
                //                startActivity(Intent("'study.intent.action.main"))
            }

            5 -> {

            }

            6 -> {
                startActivity(Intent(this, LikeActivity::class.java))
            }

            7 -> {
                startActivity(Intent(this, RoomActivity::class.java))
            }

            8 -> {
            }

            9 -> {
                startActivity(Intent(this, ListenActActivity::class.java))
            }

            10 -> {
                //setresult
                startActivityForResult(Intent(this, LedClockActivity::class.java), 1001)
            }

            11 -> {

            }

            12 -> {


            }

            13 -> {
                startActivity(Intent(this, VaultActivity::class.java))
            }

            14 -> {

            }

            15 -> {
                //权限引导
                goUsagePermissionSetting()
            }

            16 -> {
                //协调布局--吸顶
                startActivity(Intent(this, CeilingActivity::class.java))
            }

            17 -> {
                startActivity(Intent(this, GuideViewActivity::class.java))

            }

            18 -> {
                //监听应用安装卸载
                startActivity(Intent(this, AppLockActivity::class.java))
            }

            19 -> {
                toast("未完成")
                // TODO:  字体大小测试
            }

            20 -> {
                toast("未完成")
                // TODO:  Flow 布局
            }

            21 -> {
                // TODO: 全屏通知
                startActivity(Intent(this, FullscreenNotificationActivity::class.java))
            }

            22 -> {
                //  https://blog.csdn.net/zhyooo123/article/details/6698567
            }

            23 -> {
                startActivity(Intent(this, TabLayoutActivity::class.java))
            }

            24 -> {
                finish()
                startActivity(Intent(this, TransparentActivity::class.java))
            }

            25 -> {

            }

            26 -> {
            }

            27 -> {
                // TODO: 水波纹 WaveView
                toast("未完成")
            }

            28 -> {
                // TODO: 主题，进入退出动画
                startActivity(Intent(this, ThemeActivity::class.java))
                toast("未完成")
            }

            29 -> {
            }

            30 -> {
            }

            31 -> {

            }

            32 -> {
                startActivity(Intent(this, CustomViewActivity::class.java))
            }

            33 -> {
                startActivity(Intent(this, CleaningTrashActivity::class.java))
            }

            34 -> {
                startActivity(Intent(this, CleaningTrashFilePermissionActivity::class.java))
            }

        }
        return true
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        // 显示壁纸
//        window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER)
        MMKV.initialize(this)
        setContentView(binding.root)
        initFlowLayout()
        //        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        //            checkForNotificationPermission()
        //        }
        binding.tagFlowLayout.apply {
            setOnTagClickListener(this@MainActivity)
            setOnSelectListener(this@MainActivity)
        }


        lifecycleScope.launch(Dispatchers.IO) {
            val list = notificationEntityDB.selectAllNotificationEntity()
            toast("notificationEntityDB.selectAllNotificationEntity() size:${list.size}")
        }

    }

    override fun onResume() {
        super.onResume()
        initView()
    }

    private fun initView() {
        val permissionGranted = ContextCompat.checkSelfPermission(
            this, Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
        val notificationStr = if (permissionGranted) {
            "有通知权限\n"
        } else {
            "无通知权限\n"
        }

        val permissionEnum = GalleryPermissionUtils.checkMediaPermissionResult(this)
        val galleryStr = when (permissionEnum) {
            GalleryPermissionUtils.PermissionEnum.NO_PERMISSIONS -> {
                "无媒体权限"
            }

            GalleryPermissionUtils.PermissionEnum.PARTIAL_PERMISSIONS -> {
                "部分媒体权限"
            }

            else -> {
                "全部媒体权限"
            }
        }
        binding.isHasNotificationPermission.text = notificationStr + galleryStr
    }


    private fun checkPermissions() {
        val fineLocationPermission =
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
        val coarseLocationPermission =
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)

        if (fineLocationPermission != PackageManager.PERMISSION_GRANTED || coarseLocationPermission != PackageManager.PERMISSION_GRANTED) {
            // 权限未被授予，进行请求
            requestPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        } else {
            // 权限已被授予，可以进行传感器操作
            startHealthService()
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1001) {
            Log.d(
                "223366",
                "onActivityResult: ----onActivityResult---onActivityResult---onActivityResult"
            )
        }


    }

    /**
     * 相机权限
     */
    private val requestCameraPermission: ActivityResultLauncher<String> = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            openCamera()
            //            startCameraService()

            //            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            //                checkForNotificationPermission()
            //            }
        } else {
            // 权限被拒绝，显示提示信息
            toast("相机权限被拒绝")
        }
    }

    /**
     * 蓝牙权限
     */
    private val requestConnectedPermission: ActivityResultLauncher<String> =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (isGranted) {
                startConnectedDeviceService()
            } else {
                // 权限被拒绝，显示提示信息
                toast("连接权限被拒绝")
            }
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

    }


    private fun startCameraService() {
        val serviceIntent = Intent(this, CameraService::class.java)
        //        startService(serviceIntent)
        ContextCompat.startForegroundService(this, serviceIntent)
    }


    private fun startConnectedDeviceService() {
        val serviceIntent = Intent(this, ConnectedDeviceService::class.java)
        ContextCompat.startForegroundService(this, serviceIntent)
    }

    private fun startHealthService() {
        val serviceIntent = Intent(this, HealthService::class.java)

        ContextCompat.startForegroundService(this, serviceIntent)
    }


    private fun openCamera() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        takePictureLauncher.launch(takePictureIntent)
    }

    private val takePictureLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        }


    /**
     * 通知栏权限
     */
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun checkForNotificationPermission() {
        val permissionGranted = ContextCompat.checkSelfPermission(
            this, Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
        if (!permissionGranted) {
            mPermReceiver.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    private val mPermReceiver =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { result ->
            val serviceIntent = Intent(this, CameraService::class.java)
            //        startService(serviceIntent)
            ContextCompat.startForegroundService(this, serviceIntent)
        }

    override fun onSelected(selectPosSet: Set<Int>?) {
        Log.d("223366", "onSelected:---${selectPosSet} ")
    }

    private fun goUsagePermissionSetting() {
        val intent = Intent(
            this@MainActivity, PermissionSettingActivity::class.java
        ).apply {
            putExtra(PERMISSION_STRING_TYPE, Settings.ACTION_USAGE_ACCESS_SETTINGS)
        }
        permissionUsageAccessActivityResultLauncher.launch(intent)
    }

    private val permissionUsageAccessActivityResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->


        }


    fun onPerformSync(
        lastExitTime: Long
    ) {
        val currentTimeMillis = System.currentTimeMillis()
        //不在通知显示时间内 返回
        if (!isTimeInRangesNoon(currentTimeMillis) && !isTimeInRangesMorning(currentTimeMillis)) {
            Log.d("223366", "onPerformSync: 不在通知显示时间内 返回")
            return
        }

        //当前时间在 14.30之前 且退出时间在8.00之后不发通知
        if (isNoonTime(currentTimeMillis) && !isMorningTime(lastExitTime)) {
            Log.d("223366", "当前时间在 14.30之前  且退出时间在 12.30-14.30")
            return
        }
        //当前时间在 14.30之后 退出时间也在 14.30之后不发通知
        if (!isNoonTime(currentTimeMillis) && !isNoonTime(lastExitTime)) {
            Log.d("223366", "onPerformSync: --当前时间在 14.30之后 且退出时间在 22.00-8.00")
            return
        }
        //退后台至少1小时后发通知
        if (currentTimeMillis - lastExitTime < 1000 * 60 * 60 * 1) {
            Log.d("223366", "onPerformSync: --退后台至少1小时后发通知")
            return
        }

        val permissionGranted = ContextCompat.checkSelfPermission(
            this, Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
        //没权限
        if (!permissionGranted) {
            Log.d("223366", "onPerformSync: --没权限")
            return
        }
    }


    /**
     * 判断当前时间是否在指定的时间范围内
     * 显示通知时间  14.30-22.00
     */
    private fun isTimeInRangesNoon(time: Long): Boolean {
        val currentCalendar = Calendar.getInstance()
        currentCalendar.timeInMillis = time
        val currentTimeInMinutes =
            currentCalendar.get(Calendar.HOUR_OF_DAY) * 60 + currentCalendar.get(Calendar.MINUTE)

        val range2Start = 14 * 60 + 30
        val range2End = 22 * 60

        // 检查当前时间是否在指定的时间范围内
        return currentTimeInMinutes in range2Start..range2End
    }


    /**
     * 判断当前时间是否在指定的时间范围内
     * 显示时间 8.00-12.30
     */
    private fun isTimeInRangesMorning(time: Long): Boolean {
        val currentCalendar = Calendar.getInstance()
        currentCalendar.timeInMillis = time
        val currentTimeInMinutes =
            currentCalendar.get(Calendar.HOUR_OF_DAY) * 60 + currentCalendar.get(Calendar.MINUTE)

        // 定义时间范围的分钟数
        val range1Start = 8 * 60
        val range1End = 12 * 60 + 30

        // 检查当前时间是否在指定的时间范围内
        return currentTimeInMinutes in range1Start..range1End
    }

    /**
     *判断是在一天的 14.30 之前还是之后
     *  14.30之前返回true
     */
    private fun isNoonTime(time: Long): Boolean {
        val currentCalendar = Calendar.getInstance()
        currentCalendar.timeInMillis = time
        val currentTimeInMinutes =
            currentCalendar.get(Calendar.HOUR_OF_DAY) * 60 + currentCalendar.get(Calendar.MINUTE)
        val range2End = 14 * 60 + 30   // 14:30 -> 870 分钟
        // 检查当前时间是否在指定的时间范围内
        return currentTimeInMinutes in 0..range2End
    }

    /**
     *判断是一天的 8.00之前还是之后
     *  8.30之前返回true
     */
    private fun isMorningTime(time: Long): Boolean {
        val currentCalendar = Calendar.getInstance()
        currentCalendar.timeInMillis = time
        val currentTimeInMinutes =
            currentCalendar.get(Calendar.HOUR_OF_DAY) * 60 + currentCalendar.get(Calendar.MINUTE)
        val range2End = 8 * 60 + 30   // 14:30 -> 870 分钟
        // 检查当前时间是否在指定的时间范围内
        return currentTimeInMinutes in 0..range2End
    }

    private val requestPermissionLauncher: ActivityResultLauncher<Array<String>> =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            Log.d(
                "223366",
                "registerForActivityResult: ----registerForActivityResult---registerForActivityResult---registerForActivityResult"
            )
            // 处理权限请求结果
            val fineLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
            val coarseLocationGranted =
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false

            if (fineLocationGranted || coarseLocationGranted) {
                // 权限被授予
                startHealthService()
            } else {
                // 权限被拒绝
                toast("权限被拒绝")
            }
        }


}