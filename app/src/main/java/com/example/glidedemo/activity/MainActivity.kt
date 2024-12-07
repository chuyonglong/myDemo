package com.example.glidedemo.activity

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.View
import android.view.WindowManager
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.glidedemo.base.BaseActivity
import com.example.glidedemo.bean.MediaBase
import com.example.glidedemo.bean.MediaData
import com.example.glidedemo.databinding.ActivityMainBinding
import com.example.glidedemo.databinding.FlowlayoutTextBinding
import com.example.glidedemo.extensions.PERMISSION_STRING_TYPE
import com.example.glidedemo.extensions.toast
import com.example.glidedemo.foreground.services.CameraService
import com.example.glidedemo.foreground.services.ConnectedDeviceService
import com.example.glidedemo.foreground.services.HealthService
import com.example.glidedemo.permission.GalleryPermissionUtils
import com.example.glidedemo.utils.MediaQueryForPermission
import com.example.glidedemo.views.flowlayout.FlowLayout
import com.example.glidedemo.views.flowlayout.TagAdapter
import com.example.glidedemo.views.flowlayout.TagFlowLayout
import com.tencent.mmkv.MMKV
import io.appmetrica.analytics.AppMetrica
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.ArrayList


class MainActivity : BaseActivity(), TagFlowLayout.OnTagClickListener,
    TagFlowLayout.OnSelectListener {
    private val binding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    private val mVals by lazy {
        linkedMapOf(
            "0" to "相机",
            "1" to "液晶时钟",
            "2" to "健康",
            "3" to "主题切换",
            "4" to "跳转外部activity",
            "5" to "媒体",
            "6" to "点赞",
            "7" to "room数据库",
            "8" to "list adapter ",
            "9" to "media list adapter",
            "10" to "setresult",
            "11" to "临时权限",
            "12" to "全屏显示图片(未完成)",
            "13" to "密码锁",
            "14" to "后台拍照无预览相机",
            "15" to "权限引导",
            "16" to "吸顶",
            "17" to "新手引导",
            "18" to "ShapeableImageView",
            "19" to "字体大小测试",
            "20" to "Flow 布局 ",
            "21" to "全屏通知",
            "22" to "使用桌面背景",
            "23" to "使tabLayout优化",
            "24" to "透明activity",
            "25" to "图像分类Interpreter",
            "26" to "图像分类",
            "27" to "水波纹",
            "28" to "主题，进入退出动画",
            "29" to "FaceDetector",
            "30" to "今日头条宽度适配",

            )
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 显示壁纸
        window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER)
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


    private val moveVaultLauncher =
        registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                toast("请求权限成功")
            }
        }

    /**
     * 获取 数据跳转PhotoDetailActivity
     */

    private fun goPhotoDetailActivity() {
        lifecycleScope.launch(Dispatchers.IO) {
            val list: MutableList<MediaBase> =
                MediaQueryForPermission.queryAllData(this@MainActivity)

            if (list.isEmpty()) {
                withContext(Dispatchers.Main) {
                    toast("没有数据,别点了!!!")
                }
                return@launch
            }

            val finalLt: ArrayList<MediaBase> = if (list.size < 10) {
                ArrayList(list)
            } else {
                ArrayList(list.subList(0, 10))
            }
            Intent(this@MainActivity, PhotoDetailActivity::class.java).apply {
                putExtra("detail_list_data", finalLt)
                startActivity(this)
            }
        }
    }

    /**
     * 媒体权限回调
     */
    private val mediaPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            if (it.isNotEmpty()) {
                goPhotoDetailActivity()
            }
        }


    override fun onTagClick(view: View?, position: Int, parent: FlowLayout?): Boolean {
        Log.d("223366", "onTagClick:---onTagClick ----:${position}")

        when (position) {
            0 -> {
                val eventParameters = "{\"name\":\"相机\", \"age\":\"18\"}"
                AppMetrica.reportEvent("相机", eventParameters)
//            startCameraService()
                // 请求相机权限
                requestCameraPermission.launch(Manifest.permission.CAMERA)
            }

            1 -> {
                val eventParameters = "{\"name\":\"Alice\", \"age\":\"18\"}"
                AppMetrica.reportEvent("New person", eventParameters)

                startActivity(Intent(this, LedClockActivity::class.java))

            }

            2 -> {
                checkPermissions()
            }

            3 -> {
                startActivity(Intent(this, ThemeActivity::class.java))
            }

            4 -> {
                //自定义权限启动外部activity
                toast("测试用,没有外部activity")
//                startActivity(Intent("'study.intent.action.main"))
            }

            5 -> {
                startActivity(Intent(this, MediaActivity::class.java))
            }

            6 -> {
                startActivity(Intent(this, LikeActivity::class.java))
            }

            7 -> {
                startActivity(Intent(this, RoomActivity::class.java))
            }

            8 -> {
                startActivity(Intent(this, ListAdapterActivity::class.java))
            }

            9 -> {
                startActivity(Intent(this, MediaListActivity::class.java))
            }

            10 -> {
                //setresult
                startActivityForResult(Intent(this, LedClockActivity::class.java), 1001)
            }

            11 -> {
                toast("如点击没反应,检查媒体权限,申请权限点击 '媒体' 按钮")
                // :    这里的问题是之前 MediaStore.Images.Media.EXTERNAL_CONTENT_URI 选错了
//                val path = "/storage/emulated/0/svg/Cat.svg"
                lifecycleScope.launch(Dispatchers.IO) {
                    val list: MutableList<MediaBase> =
                        MediaQueryForPermission.queryAllData(this@MainActivity)
                    list.getOrNull(0)?.let {
                        withContext(Dispatchers.Main) {
                            val uri = Uri.withAppendedPath(
                                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                                ((it as MediaData).id).toString()
                            )
                            val writeRequest = MediaStore.createWriteRequest(
                                contentResolver, mutableListOf<Uri>(uri)
                            ).intentSender
                            moveVaultLauncher.launch(
                                IntentSenderRequest.Builder(writeRequest).build()
                            )
                        }
                    }

                }
            }

            12 -> {
                val permissionEnum = GalleryPermissionUtils.checkMediaPermissionResult(this)
                if (permissionEnum == GalleryPermissionUtils.PermissionEnum.NO_PERMISSIONS) {
                    toast("没有权限----,请求权限!!!")
                    GalleryPermissionUtils.requestMediaPermissions(mediaPermissionLauncher)
                } else {
                    goPhotoDetailActivity()
                }

            }

            13 -> {
                startActivity(Intent(this, VaultActivity::class.java))
            }

            14 -> {
                startActivity(Intent(this, BackgroundCameraActivity::class.java))
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
                // TODO:  ShapeableImageView
            }

            19 -> {
                // TODO:  字体大小测试
            }

            20 -> {
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
                startActivity(Intent(this, ImageClassificationInterpreterActivity::class.java))
            }

            26 -> {
                startActivity(Intent(this, ImageClassificationActivity::class.java))
            }

            27 -> {
                // TODO: 水波纹 WaveView
            }

            28 -> {
                // TODO: 主题，进入退出动画
            }

            29 -> {
                // TODO: FaceDetector
            }

        }
        return true
    }


}