package com.example.glidedemo.permission

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.result.ActivityResultLauncher
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

object GalleryPermissionUtils {
    fun requestMediaPermissions(permissionLauncher: ActivityResultLauncher<Array<String>>) {
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
            permissionLauncher.launch(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE))
        }
    }


    /**
     * 检查图片视频权限
     */
    fun checkMediaPermissionResult(activity: FragmentActivity): PermissionEnum {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && (ContextCompat.checkSelfPermission(
                activity,
                Manifest.permission.READ_MEDIA_IMAGES
            ) == PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                activity,
                Manifest.permission.READ_MEDIA_VIDEO
            ) == PackageManager.PERMISSION_GRANTED)
        ) {
            // Android 13及以上完整照片和视频访问权限
            return PermissionEnum.FULL_PERMISSIONS
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE && ContextCompat.checkSelfPermission(
                activity,
                Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            return PermissionEnum.PARTIAL_PERMISSIONS
        } else if (ContextCompat.checkSelfPermission(
                activity,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            // Android 12及以下完整本地读写访问权限
            return PermissionEnum.FULL_PERMISSIONS
        } else {
            // 无本地读写访问权限
            return PermissionEnum.NO_PERMISSIONS
        }
    }

    /**
     * 完整权限：Full Permissions
     * 部分权限：Partial Permissions
     * 没有权限：No Permissions
     */
    enum class PermissionEnum {
        FULL_PERMISSIONS, PARTIAL_PERMISSIONS, NO_PERMISSIONS
    }


    /**
     * 相机权限
     */
    fun requestCameraPermissions(permissionCameraLauncher: ActivityResultLauncher<String>) {
        permissionCameraLauncher.launch(Manifest.permission.CAMERA)
    }

    /**
     * 是否有相机权限
     */
    fun hasCameraPermissions(activity: FragmentActivity): Boolean {
        return ContextCompat.checkSelfPermission(
            activity,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }


}
