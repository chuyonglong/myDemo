package com.example.glidedemo.entity

import androidx.annotation.Keep

@Keep
data class AppCacheData(
    // 基础信息
    val appName: String,
    val packageName: String,
    // 缓存数据
    val cacheBytes: Long,      // 缓存字节数（核心字段）
    val appSize: Long,         // 应用安装大小
    val dataSize: Long,        // 用户数据大小
    val totalSize: Long,       // 总占用空间 = appSize + dataSize
    // 辅助信息
    val isSystemApp: Boolean,  // 是否是系统应用
    // 格式化后的显示文本（可选）
    var formattedCacheSize: String = "" // 如 "256 MB"
)