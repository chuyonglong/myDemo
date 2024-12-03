package com.example.glidedemo.activity

import android.os.Bundle
import android.os.Environment
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.example.glidedemo.base.BaseActivity
import com.example.glidedemo.databinding.ActivityBackgroundCameraBinding
import com.example.glidedemo.extensions.toast
import com.example.glidedemo.permission.GalleryPermissionUtils
import com.example.glidedemo.utils.Camera2BackgroundUtil1
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class BackgroundCameraActivity : BaseActivity() {

    private val binding by lazy {
        ActivityBackgroundCameraBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        initActions()
    }


    private val requestCameraPermission: ActivityResultLauncher<String> = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // 权限被授予，可以调用相机
            toast("相机权限已授予")
            backgroundTakePicture()
        } else {
            // 权限被拒绝，显示提示信息
            toast("相机权限被拒绝")
        }
    }


    private fun initActions() {
        binding.takePhotoButton.setOnClickListener {
            if (!GalleryPermissionUtils.hasCameraPermissions(this)) {
                GalleryPermissionUtils.requestCameraPermissions(requestCameraPermission)
            } else {
                backgroundTakePicture()
            }
        }
    }

    private fun backgroundTakePicture() {
        val timeStamp: String =
            SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val imageFilename = "my_photo_${timeStamp}.jpeg"

        val storageDir =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        val imageFile = File(storageDir, imageFilename)

        Camera2BackgroundUtil1(this).startTakePicture(imageFile.path)
    }


}