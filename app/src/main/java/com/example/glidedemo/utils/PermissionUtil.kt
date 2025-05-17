package com.example.glidedemo.utils

import android.Manifest
import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import androidx.activity.result.ActivityResultLauncher
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

object PermissionUtil {

    /**
     * 允许查看使用情况
     */
    fun checkUsageStats(context: Context): Boolean {
        val info = context.packageManager.getApplicationInfo(context.packageName, 0)
        val appOpsManager =
            context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        return if (Build.VERSION.SDK_INT >= 33) {
            try {
                appOpsManager.unsafeCheckOpNoThrow(
                    AppOpsManager.OPSTR_GET_USAGE_STATS,
                    info.uid,
                    info.packageName
                ) == AppOpsManager.MODE_ALLOWED
            } catch (_: Exception) {
                false
            }
        } else {
            return try {
                appOpsManager.checkOpNoThrow(
                    AppOpsManager.OPSTR_GET_USAGE_STATS,
                    info.uid,
                    info.packageName
                ) == AppOpsManager.MODE_ALLOWED
            } catch (_: Exception) {
                false
            }
        }
    }

    fun requestUsageStats(context: Context): Boolean {
        return try {
            val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
            intent.data = Uri.parse("package:${context.packageName}")
            context.startActivity(intent)
            true;
        } catch (_: Throwable) {
            val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
            context.startActivity(intent)
            false
        }
    }

    fun checkOverlays(context: Context): Boolean {
        return try {
            Settings.canDrawOverlays(context)
        } catch (e: Exception) {
            false
        }
    }

    /**
     * 覆盖上层权限
     */
    fun requestOverlays(context: Context): Boolean {
        return try {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:${context.packageName}")
            );
            context.startActivity(intent);
            true;
        } catch (e: Exception) {
            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
            context.startActivity(intent);
            false;
        }
    }

    /**
     * 是否有权限
     */
    fun hasPermissions(context: Context, permissionType: String): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            permissionType
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun startAppInfo(context: Context): Boolean {
        return try {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            intent.data = Uri.parse("package:${context.packageName}")
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            val intent = Intent(Settings.ACTION_APPLICATION_SETTINGS)
            context.startActivity(intent)
            true
        }
    }

    /**
     * 是否有所有文件访问权限
     * */
    fun checkAllFilePermission(): Boolean {
        if (Build.VERSION.SDK_INT >= 30) {
            return Environment.isExternalStorageManager()
        }
        return true
    }

    /**
     * 检查读权限
     * */
    fun checkReadPermission(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < 29) return true
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_EXTERNAL_STORAGE,
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * 检查写权限
     * */
    fun checkWritePermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
        ) == PackageManager.PERMISSION_GRANTED
    }


    /**
     * 判断是否有存储权限
     */

    fun hasStoragePermission(activity: FragmentActivity): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Environment.isExternalStorageManager()
        } else {
            ContextCompat.checkSelfPermission(
                activity,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                activity,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }
    }


    /**
     * 请求存储权限
     */
    fun requestPermissions(permissionLauncher: ActivityResultLauncher<Array<String>>) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.READ_MEDIA_IMAGES,
                    Manifest.permission.READ_MEDIA_VIDEO,
                    Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
                )
            )
        } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.TIRAMISU) {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.READ_MEDIA_VIDEO
                )
            )
        } else {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
            )
        }
    }
}

