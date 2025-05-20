package com.example.glidedemo.utils

import android.content.Context
import android.view.WindowManager
import com.example.glidedemo.database.AppDatabase
import com.example.glidedemo.interfaces.NotificationEntityDao
import com.example.glidedemo.interfaces.SimilarImagesDao

val similarImagesDB: SimilarImagesDao
    get() = AppDatabase.getDataInstance().SimilarImagesDao()

val notificationEntityDB: NotificationEntityDao
    get() = AppDatabase.getDataInstance().NotificationEntityDao()

val Context.windowManager: WindowManager get() = getSystemService(Context.WINDOW_SERVICE) as WindowManager

