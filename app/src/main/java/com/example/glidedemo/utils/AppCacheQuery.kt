package com.example.glidedemo.utils

import android.app.usage.StorageStatsManager
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.storage.StorageManager
import com.example.glidedemo.entity.AppCacheData

object AppCacheQuery {

    fun getAllAppCache(context: Context): List<AppCacheData> {
        val pm = context.packageManager
        val apps =
            pm.getInstalledApplications(PackageManager.MATCH_ALL)
                .filterNot { isSystemApp(it) }
        return apps.mapNotNull { appInfo ->
            try {
                // 获取存储数据
                val stats = context.getSystemService(StorageStatsManager::class.java)
                    .queryStatsForUid(StorageManager.UUID_DEFAULT, appInfo.uid)
                AppCacheData(
                    appName = appInfo.loadLabel(pm).toString(),
                    packageName = appInfo.packageName,
                    cacheBytes = stats.cacheBytes,
                    appSize = stats.appBytes,
                    dataSize = stats.dataBytes,
                    totalSize = stats.appBytes + stats.dataBytes,
                    isSystemApp = isSystemApp(appInfo),
                ).apply {
                    formattedCacheSize = formatBytes(cacheBytes)
                }
            } catch (e: Exception) {
                null
            }
        }
    }

    /**
     * 判断是否是系统应用
     */
    private fun isSystemApp(appInfo: ApplicationInfo): Boolean {
        return (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0 ||
                (appInfo.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0
    }

    // 获取最后使用时间
    private fun getLastUsedTime(context: Context, packageName: String): Long {
        val usageStatsManager = context.getSystemService(UsageStatsManager::class.java)
        val endTime = System.currentTimeMillis()
        val startTime = endTime - 1000 * 60 * 60 * 24 * 30 // 过去30天
        val stats = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_BEST,
            startTime,
            endTime
        )
        return stats.find { it.packageName == packageName }?.lastTimeUsed ?: 0
    }

    // 字节格式化工具
    private fun formatBytes(bytes: Long): String {
        return when {
            bytes >= 1_000_000_000 -> "%.1f GB".format(bytes / 1e9)
            bytes >= 1_000_000 -> "%.1f MB".format(bytes / 1e6)
            bytes >= 1_000 -> "%.1f KB".format(bytes / 1e3)
            else -> "$bytes B"
        }
    }


    /**
     * 根据包名查询单个应用的缓存信息
     * @param packageName 目标应用包名
     * @return AppCacheData? 可空对象（查询失败返回 null）
     */
    fun getAppCacheByPackageName(context: Context, packageName: String): AppCacheData? {
        return try {
            val pm = context.packageManager
            // 获取目标应用信息
            val appInfo = pm.getApplicationInfo(packageName, PackageManager.MATCH_UNINSTALLED_PACKAGES)

            // 获取存储统计
            val storageStatsManager = context.getSystemService(StorageStatsManager::class.java)
            val stats = storageStatsManager.queryStatsForUid(
                StorageManager.UUID_DEFAULT,
                appInfo.uid
            )

            // 构建返回对象
            AppCacheData(
                appName = appInfo.loadLabel(pm).toString(),
                packageName = appInfo.packageName,
                cacheBytes = stats.cacheBytes,
                appSize = stats.appBytes,
                dataSize = stats.dataBytes,
                totalSize = stats.appBytes + stats.dataBytes,
                isSystemApp = isSystemApp(appInfo)
            ).apply {
                formattedCacheSize = formatBytes(cacheBytes)
            }
        } catch (e: PackageManager.NameNotFoundException) {
            null
        } catch (e: SecurityException) {
            null
        } catch (e: Exception) {
            null
        }
    }


}