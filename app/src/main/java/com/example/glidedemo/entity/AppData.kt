package com.example.glidedemo.entity

import android.graphics.drawable.Drawable
import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

/**
 * @param pkg app pkg
 * @param name app name
 * @param type 1,系统应用 2，用户应用 3，特殊保护应用 4，常用保护应用列表
 * @param isLocked lock or unlock
 * */

@Keep
@Entity(tableName = "app_data")
data class AppData(
    @PrimaryKey var packageName: String,
    var appName: String? = null,
    var type: Int = 0,
    var isLocked: Boolean = false,
) {
    @Ignore
    var iconDrawable: Drawable? = null

    fun copyWithIconDrawable(
        icon: Drawable? = iconDrawable, lockState: Boolean
    ): AppData {
        val newData = this.copy()
        newData.iconDrawable = icon
        newData.isLocked = lockState
        return newData
    }
}