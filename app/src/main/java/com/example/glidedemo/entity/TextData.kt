package com.example.glidedemo.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlin.random.Random

@Entity(tableName = "text_data", indices = [Index(value = ["id"], unique = true)])
data class TextData(
    @PrimaryKey(autoGenerate = true) var id: Long?,
    @ColumnInfo(name = "text_str") val textStr: String?,
    @ColumnInfo(name = "text_start_color") val textStartColor: Int = Random.nextInt(),
    @ColumnInfo(name = "text_end_color") val textEndColor: Int = Random.nextInt(),
)

