package com.example.glidedemo.bean

import android.net.Uri
import androidx.annotation.Keep

/**
 * 相似图片列表展示
 */
@Keep
data class CleanUpDetailData(
    var name: String,
    var path: String,
    var size: Long,
    var stamp: Long,
    var extra: Map<String, Any>? = null,
    var id: Long = -1L,
    var isSelected: Boolean = false,
    var uri: Uri? = null,
    //分组uuid
    var groupId: String? = null
) : BaseData()
