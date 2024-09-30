package com.example.glidedemo.bean

import com.example.glidedemo.utils.ItemTypeEnum

data class MediaListData(
    var id: Long?,
    var path: String?,
    var modified: Long?,
    var taken: Long?,
    var size: Long?,
    val title: String?,
    val itemType: ItemTypeEnum?,
)