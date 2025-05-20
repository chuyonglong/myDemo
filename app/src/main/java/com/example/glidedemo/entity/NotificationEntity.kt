package com.example.glidedemo.entity

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.PrimaryKey

@Keep
@Entity(tableName = "notification_entity")
data class NotificationEntity(
    @PrimaryKey(autoGenerate = true) val id: Int,
    /**
     * 标题
     */
    var title: String? = null,
    /**
     * 内容
     */
    var message: String? = null,
    /**
     * 发送时间
     */
    var postTime: Long,
    /**
     * 包名
     */
    var packageName: String,
    /**
     * 是否已读
     */
    var isRead: Boolean = false
)
