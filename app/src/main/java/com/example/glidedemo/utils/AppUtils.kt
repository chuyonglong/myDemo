package com.example.glidedemo.utils

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import com.example.glidedemo.R

object AppUtils {

    /**
     * 通过 pkg 获取应用图标
     * */
    fun getIconDrawable(context: Context?, pkg: String?): Drawable? {
        if (null == context || null == pkg || pkg == context.packageName) {
            return getDefaultIconDrawable(
                context
            )
        }
        return try {
            context.packageManager.getApplicationIcon(pkg)
        } catch (th: PackageManager.NameNotFoundException) {
            th.printStackTrace()
            getDefaultIconDrawable(context)
        }
    }

    /**
     * 获取默认图标
     */
    private fun getDefaultIconDrawable(context: Context?): Drawable? {
        if (null == context) return null
        return ContextCompat.getDrawable(
            context,
            R.mipmap.ic_launcher
        )
    }


    /**
     * 获取应用名称
     * */
    fun getAppName(context: Context, pkg: String?): String {
        if (null == pkg) {
            return context.getString(R.string.app_name)
        }
        return try {
            val pkgManager: PackageManager = context.packageManager
            pkgManager.getApplicationLabel(
                pkgManager.getApplicationInfo(
                    pkg,
                    PackageManager.GET_META_DATA
                )
            ).toString()
        } catch (_: Throwable) {
            ""
        }
    }
}