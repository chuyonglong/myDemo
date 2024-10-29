package com.example.glidedemo.activity

import android.os.Bundle
import android.os.Environment
import com.example.glidedemo.base.BaseActivity
import com.example.glidedemo.databinding.ActivityBackgroundCameraBinding
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


    private fun initActions() {
        binding.takePhotoButton.setOnClickListener {
            val timeStamp: String =
                SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val imageFilename = "my_photo_${timeStamp}.jpeg"

            val storageDir =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            val imageFile = File(storageDir, imageFilename)

            Camera2BackgroundUtil1(this).startTakePicture(imageFile.path)

        }
    }


}