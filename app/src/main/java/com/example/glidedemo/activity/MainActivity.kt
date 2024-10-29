package com.example.glidedemo.activity

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.glidedemo.base.BaseActivity
import com.example.glidedemo.bean.MediaBase
import com.example.glidedemo.bean.MediaData
import com.example.glidedemo.bean.Medium
import com.example.glidedemo.databinding.ActivityMainBinding
import com.example.glidedemo.databinding.FlowlayoutTextBinding
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
        arrayOf(
            "相机",
            "液晶时钟",
            "健康",
            "连接",
            "外部activity",
            "媒体",
            "点赞",
            "room数据库",
            "list adapter",
            "media list adapter",
            "setresult",
            "临时权限",
            "全屏显示图片(未完成)",
            "密码锁",
            "后台拍照无预览相机",
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
                Toast.makeText(this, "权限被拒绝", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(
            "223366", "onCreate: ----onCreate---onCreate---onCreate"
        )
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

    override fun onStart() {
        super.onStart()
        Log.d(
            "223366", "onStart: ----onStart---onStart---onStart"
        )
    }

    override fun onResume() {
        super.onResume()
        Log.d(
            "223366", "onResume: ----onResume---onResume---onResume"
        )
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
//            openCamera()
//            startCameraService()

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                checkForNotificationPermission()
            }
        } else {
            // 权限被拒绝，显示提示信息
            Toast.makeText(this, "相机权限被拒绝", Toast.LENGTH_SHORT).show()
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
                Toast.makeText(this, "连接权限被拒绝", Toast.LENGTH_SHORT).show()
            }
        }


    private fun initFlowLayout() {
        binding.tagFlowLayout.apply {
            addAdapter(object : TagAdapter<Any>(mVals.toList()) {
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
                startConnectedDeviceService()
            }

            4 -> {
                //自定义权限启动外部activity
                startActivity(Intent("'study.intent.action.main"))
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
                startActivityForResult(Intent(this, LedClockActivity::class.java), 1001)
            }

            11 -> {
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
                    Toast.makeText(
                        this@MainActivity, "没有权限----,请求权限!!!", Toast.LENGTH_SHORT
                    ).show()
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
        }
        return true
    }


    private val moveVaultLauncher =
        registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                Toast.makeText(this, "请求权限成功", Toast.LENGTH_SHORT).show()
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
                    Toast.makeText(
                        this@MainActivity, "没有数据,别点了!!!", Toast.LENGTH_SHORT
                    ).show()
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

}