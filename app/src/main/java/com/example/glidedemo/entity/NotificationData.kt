package com.example.glidedemo.entity

data class NotificationData(
    var title: String,
    var content: String,
    var confirmStr: String? = null,
    var cancelStr: String? = null,
    var showBigImage: Boolean = false,
)
