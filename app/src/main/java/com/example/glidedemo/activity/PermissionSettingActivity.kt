package com.example.glidedemo.activity

import android.Manifest
import android.app.Activity
import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.lifecycle.lifecycleScope
import com.example.glidedemo.extensions.PERMISSION_RESULT
import com.example.glidedemo.extensions.PERMISSION_STRING
import com.example.glidedemo.extensions.PERMISSION_STRING_TYPE
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class PermissionSettingActivity : AppCompatActivity() {

    private val TAG = "PermissionSettingActivity"

    private var permissionType: String? = null
    private var settingType: String = Settings.ACTION_APPLICATION_DETAILS_SETTINGS


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        permissionType = intent.getStringExtra(PERMISSION_STRING)
        settingType = intent.getStringExtra(PERMISSION_STRING_TYPE) ?: settingType
        autoCheckPermission()
        goSetting()
        MainScope().launch {
            delay(200)
            Intent(this@PermissionSettingActivity, PermissionGuideActivity::class.java).apply {
                startActivity(this)
            }
        }

    }


    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy: -----onDestroy ")
    }


    private fun autoCheckPermission() {
        Log.d(TAG, "autoCheckPermission:-----autoCheckPermission ")
        lifecycleScope.launch {
            while (true) {
                Log.d(TAG, "autoCheckPermission:-----autoCheckPermission0000000000---$isActive ")
                delay(1000)
                Log.d(
                    TAG,
                    "autoCheckPermission:-----autoCheckPermission1111111111111111111111111---$isActive"
                )
                if (permissionType != null) {
                    when (permissionType) {
                        Manifest.permission.CAMERA -> {
                            val hasPermission = hasPermissions(
                                this@PermissionSettingActivity, permissionType!!
                            )
                            if (hasPermission) {
                                jumpYourself()
                                break
                            }
                        }

                        else -> {
                            finishAndSetReturn()
                        }
                    }
                } else {
                    Log.d(TAG, "autoCheckPermission: ------$settingType")
                    when (settingType) {
                        Settings.ACTION_USAGE_ACCESS_SETTINGS -> {
                            if (checkUsageStats(this@PermissionSettingActivity)) {
                                jumpYourself()
                                break

                            }
                        }

                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION -> {
                            if (checkOverlays(this@PermissionSettingActivity)) {
                                jumpYourself()
                                break
                            }
                        }

                        Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION -> {
                            if (checkAllFilePermission()) {
                                jumpYourself()
                                break
                            }
                        }

                        Settings.ACTION_HOME_SETTINGS -> {
                            Log.d(
                                TAG, "autoCheckPermission: isDefaultLauncher11111"
                            )
                            if (isDefaultLauncher()) {
                                Log.d(
                                    TAG, "autoCheckPermission: isDefaultLauncher22222"
                                )
                                jumpYourself()
                                break
                            }
                        }

                        else -> {
                            Log.d(TAG, "autoCheckPermission: ------finishAndSetReturn----------------------------------------------------1")
                            finishAndSetReturn()
                        }
                    }
                }
            }
        }
    }

    private fun finishAndSetReturn() {
        val hasLockPermission = permissionType?.let {
            hasPermissions(this@PermissionSettingActivity, it)
        }
        val resultIntent = Intent()
        resultIntent.putExtra(PERMISSION_RESULT, hasLockPermission)
        setResult(Activity.RESULT_OK, resultIntent)
        Log.d(TAG, "finishAndSetReturn: -----finishAndSetReturn ")
        finish()
    }


    private val settingLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            Log.d(TAG, "settingLauncher: -----settingLauncher: ---$result")
            finishAndSetReturn()
        }
    private val actionHomeSettingLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            Log.d(TAG, "settingLauncher: -----settingLauncher: ---$result")
        }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        Log.d(TAG, "onNewIntent: -----onNewIntent ")
        finishAndSetReturn()
    }

    /**
     * 跳转自己关掉栈上面的activity
     */
    private suspend fun jumpYourself() {
        if (isFinishing || isDestroyed) return
        delay(100)
        startActivity(
            Intent(
                this@PermissionSettingActivity, PermissionSettingActivity::class.java
            )
        )
    }


    /**
     * 跳转设置
     */

    private fun goSetting() {
        if (settingType == Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION) {
            try {
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                intent.data = "package:${packageName}".toUri()
                settingLauncher.launch(intent)
            } catch (e: Exception) {
                e.printStackTrace()
                val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                settingLauncher.launch(intent)
            }
        } else if (settingType == Settings.ACTION_HOME_SETTINGS) {
            try {
                val intent = Intent(Settings.ACTION_HOME_SETTINGS)
                intent.data = "package:${packageName}".toUri()
                actionHomeSettingLauncher.launch(intent)
            } catch (e: Exception) {
                e.printStackTrace()
                val intent = Intent(Settings.ACTION_HOME_SETTINGS)
                actionHomeSettingLauncher.launch(intent)
            }
        } else {
            try {
                Intent(settingType).apply {
                    data = "package:${packageName}".toUri()
                    settingLauncher.launch(this)
                }
            } catch (e: Exception) {
                Intent(settingType).apply {
                    settingLauncher.launch(this)
                }
            }
        }
    }


    /**
     * 是否有权限
     */
    private fun hasPermissions(context: Context, permissionType: String): Boolean {
        return ContextCompat.checkSelfPermission(
            context, permissionType
        ) == PackageManager.PERMISSION_GRANTED
    }


    /**
     * 允许查看使用情况
     */
    private fun checkUsageStats(context: Context): Boolean {
        val info = context.packageManager.getApplicationInfo(context.packageName, 0)
        val appOpsManager = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        return if (Build.VERSION.SDK_INT >= 33) {
            try {
                appOpsManager.unsafeCheckOpNoThrow(
                    AppOpsManager.OPSTR_GET_USAGE_STATS, info.uid, info.packageName
                ) == AppOpsManager.MODE_ALLOWED
            } catch (_: Exception) {
                false
            }
        } else {
            return try {
                appOpsManager.checkOpNoThrow(
                    AppOpsManager.OPSTR_GET_USAGE_STATS, info.uid, info.packageName
                ) == AppOpsManager.MODE_ALLOWED
            } catch (_: Exception) {
                false
            }
        }
    }

    private fun checkOverlays(context: Context): Boolean {
        return try {
            Settings.canDrawOverlays(context)
        } catch (e: Exception) {
            false
        }
    }

    /**
     * 是否有所有文件访问权限
     * */
    private fun checkAllFilePermission(): Boolean {
        if (Build.VERSION.SDK_INT >= 30) {
            return Environment.isExternalStorageManager()
        }
        return true
    }

    /**
     * 是否是默认主屏幕
     * @return true: 是默认主屏幕, false: 不是默认主屏幕
     */
    private fun isDefaultLauncher(): Boolean {
        val intent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_HOME)
        }
        val resolveInfo = packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY)
        return resolveInfo?.activityInfo?.packageName == packageName
    }

}