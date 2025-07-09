package com.example.glidedemo.utils

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import com.example.glidedemo.R

object AppUtils {

    var  oldActivityInfo : ActivityInfo? = null




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

    fun startWithComponent(context: Context, componentName: ComponentName) {
        val intent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
            component = componentName
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }

    /**
     * 获取当前主屏幕的包名
     */
    fun getCurrentLauncher(context: Context): ActivityInfo? {
        val intent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_HOME)
        }
        val resolveInfo =
            context.packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY)
        return resolveInfo?.activityInfo
    }
}