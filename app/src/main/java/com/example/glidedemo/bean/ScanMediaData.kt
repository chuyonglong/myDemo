package com.example.glidedemo.bean

import androidx.annotation.Keep
import com.example.glidedemo.utils.MediaTypeEnum
import java.io.Serializable

/**
 * 首页列表展示的五张图片
 */
@Keep
data class ScanMediaData(
    var name: String,
    var path: String,
    var size: Long,
    var stamp: Long,
    var type: MediaTypeEnum,
) : Serializable
