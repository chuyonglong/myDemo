package com.example.glidedemo.activity

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.documentfile.provider.DocumentFile
import com.example.glidedemo.adapter.AppCacheListAdapter
import com.example.glidedemo.databinding.ActivityCleaningTrashFilePermissionBinding
import com.example.glidedemo.extensions.PERMISSION_STRING_TYPE
import com.example.glidedemo.extensions.toast
import com.example.glidedemo.extensions.viewBindings
import com.example.glidedemo.utils.AppCacheQuery
import com.example.glidedemo.utils.PermissionUtil
import com.example.glidedemo.views.GalleryGridLayoutManager
import java.io.File

class CleaningTrashFilePermissionActivity : AppCompatActivity(),
    AppCacheListAdapter.OnItemClickCallback {

    /**
     * 低版本
     * 请求权限次数
     */
    private var requestPermissionCount = 0

    private val binding by viewBindings(ActivityCleaningTrashFilePermissionBinding::inflate)


    private val appCacheListAdapter by lazy {
        AppCacheListAdapter()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding
        initAdapter()
        initActions()

    }

    override fun onResume() {
        super.onResume()
        val hasPermission = PermissionUtil.hasStoragePermission(this)
    }

    private fun initActions() {
        binding.usagePermissionButton.setOnClickListener {
            requestPermissions()
        }

        binding.getDataButton.setOnClickListener {
            getCacheData()
        }

        binding.getAccessibilityButton.setOnClickListener {


        }
    }


    private fun initAdapter() {
        binding.appCacheList.adapter = appCacheListAdapter
        binding.appCacheList.layoutManager = GalleryGridLayoutManager(this, 1)
    }

    private fun requestPermissions() {
        checkPermissionAndLoadData()

    }


    private fun getCacheData() {
        val list = AppCacheQuery.getAllAppCache(this)
        if (list.isEmpty()) {
            toast("没有缓存数据")
        }
        appCacheListAdapter.addCacheData(list)
        appCacheListAdapter.setOnItemClickCallback(this)
    }

    override fun onItemClick(packageName: String) {
        val targetDir = File("/sdcard/Android/data/$packageName")
        val canRead = targetDir.canRead()
        val canWrite = targetDir.canWrite()

        Log.d("223366", " $packageName ---可读: $canRead, 可写: $canWrite")
        if (canWrite) {


        } else {
            requestFolderAccess(packageName)
        }

    }

    private fun checkPermissionAndLoadData() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val intent = Intent(
                this@CleaningTrashFilePermissionActivity, PermissionSettingActivity::class.java
            ).apply {
                putExtra(
                    PERMISSION_STRING_TYPE, Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION
                )
            }
            manageExternalStoragePermissionRPlusSettingLauncher.launch(intent)
        } else {
            if (requestPermissionCount > 2) {
                goLowVersionPermissionSetting()
            } else {
                PermissionUtil.requestPermissions(permissionLauncher)
            }
        }
    }


    /**
     * android11  以及以上跳转设置
     */
    private val manageExternalStoragePermissionRPlusSettingLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val hasPermission =
                    PermissionUtil.hasStoragePermission(this@CleaningTrashFilePermissionActivity)
                if (hasPermission) {
                    // 权限已授予
                    binding.permissionText.text = "媒体权限已授予"
                }
            }
        }


    /**
     * android 11以下跳转设置
     */
    private val manageExternalStoragePermissionSettingLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val hasPermission = PermissionUtil.hasStoragePermission(this)
                if (hasPermission) {
                    binding.permissionText.text = "媒体权限已授予"
                }
            }
        }


    private fun goLowVersionPermissionSetting() {
        val intent = Intent(
            this@CleaningTrashFilePermissionActivity, PermissionSettingActivity::class.java
        ).apply {
            putExtra(
                PERMISSION_STRING_TYPE, Settings.ACTION_APPLICATION_DETAILS_SETTINGS
            )
        }
        manageExternalStoragePermissionSettingLauncher.launch(intent)
    }

    /**
     * 低版本权限回调
     */
    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val isGant = permissions.all { it.value }
            if (isGant) {
                binding.permissionText.text = "媒体权限已授予"
            } else {
                requestPermissionCount++
                goLowVersionPermissionSetting()
            }
        }


    /*******/



    // 声明 SAF 目录选择的 Launcher
    private val openDirectoryLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        uri?.let { handleSelectedDirectory(it) }
    }

    // 声明传统权限请求的 Launcher（用于 Android 10 及以下）
    private val requestLegacyStorageLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // 权限已授予
        }
    }

    // 声明 MANAGE_EXTERNAL_STORAGE 权限的 Launcher
    private val manageStorageLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R &&
            Environment.isExternalStorageManager()) {
            // 用户已授权管理所有文件
        }
    }


    fun requestFolderAccess(packageName: String) {

        val uriBuilder = Uri.Builder()
            .scheme("content")
            .authority("com.android.externalstorage.documents")
            .appendPath("tree")
            .appendPath("primary:A\u200Bndroid/data")
            .appendPath("document")
            .appendPath("primary:A\u200Bndroid/data/$packageName")
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
//            if (Environment.isExternalStorageManager()) {
//                // 已有权限
//            } else {
//                // 跳转到设置页请求 MANAGE_EXTERNAL_STORAGE
//                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
//                    data = Uri.parse("package:${packageName}")
//                }
//                manageStorageLauncher.launch(intent)
//            }
//        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        // 使用 SAF 选择目录（Android 5.0+）
        openDirectoryLauncher.launch(uriBuilder.build())
//        } else {
//            // Android 4.4 及以下请求传统存储权限
//        }
    }

    private fun handleSelectedDirectory(uri: Uri) {
        // 持久化权限（可选）
        contentResolver.takePersistableUriPermission(
            uri,
            Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        )

        // 使用 DocumentFile 操作文件
        val dir = DocumentFile.fromTreeUri(this, uri)
        dir?.listFiles()?.forEach { file ->
            Log.d("File", "文件名: ${file.name}")
        }
    }
}