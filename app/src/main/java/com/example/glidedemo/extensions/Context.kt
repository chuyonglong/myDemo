package com.example.glidedemo.extensions

import android.content.Context
import com.example.glidedemo.dao.TextDao
import com.example.glidedemo.database.AppDatabase


val Context.textDB: TextDao
    get() = AppDatabase.getDataInstance().TextDao()