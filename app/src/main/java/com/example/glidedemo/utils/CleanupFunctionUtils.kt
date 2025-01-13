package com.example.glidedemo.utils

import android.content.ContentUris
import android.content.Context
import android.provider.MediaStore
import com.example.glidedemo.bean.CleanUpDetailData
import com.example.glidedemo.extensions.getDayStartTS
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.security.MessageDigest

class CleanupFunctionUtils {
    // 使用一个缓存来存储文件路径和对应的 MD5 值
    private val md5Cache = mutableMapOf<String, String>()

    @Volatile
    private var _isLoading = false

    @Volatile
    private var _shouldStop = false

    val shouldStop get() = _shouldStop

    private var lastCursorPosition: Int = -1
    private var itemCallback: ItemCallback? = null

    suspend fun querySimilarData(context: Context, date: Long = 0L) {
        _isLoading = true
        val contentResolver = context.contentResolver
        val imagesUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        var selectionArgs: Array<String> = emptyArray()
        var selection: String? = null
        if (date != 0L) {
            selectionArgs = arrayOf(date.getDayStartTS())
            selection = "${MediaStore.Images.Media.DATE_ADDED} >= ?"
        }

        val columns = arrayOf(
            MediaStore.Images.Media.DATA,
            MediaStore.Images.Media.SIZE,
            MediaStore.Images.Media.DATE_ADDED,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DATE_MODIFIED
        )

        var offset = 0
        val pageSize = 100
        var hasNextPage = true

        // 使用游标进行分页
        while (hasNextPage) {
            val cursorImages = contentResolver.query(
                imagesUri,
                columns,
                selection,
                selectionArgs,
                "${MediaStore.Images.Media.DATE_ADDED} DESC"
            ) ?: break

            cursorImages.use { cursor ->
                if (cursor.count == 0) {
                    hasNextPage = false
                    return@use
                }

                // 跳到上次的游标位置（分页控制）
                cursor.moveToPosition(offset)

                // 开始分页处理
                var currentPosition = 0
                while (cursor.moveToNext() && currentPosition < pageSize) {
                    if (shouldStop) {
                        _isLoading = false
                        itemCallback?.itemCallback(null, QueryStateEnum.PAUSE)
                        return
                    }

                    val path: String = cursor.getString(0)
                    if (!File(path).exists()) continue
                    val id: Long = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID))
                    val mediaUri = ContentUris.withAppendedId(
                        MediaStore.Images.Media.getContentUri("external"),
                        id
                    )
                    val md5 = md5Cache[path] ?: run {
                        val computedMD5 = getMD5FromFile(path) ?: ""
                        md5Cache[path] = computedMD5  // 缓存 MD5 值
                        computedMD5
                    }

                    val item = CleanUpDetailData(
                        cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)),
                        path,
                        cursor.getLong(1),
                        cursor.getLong(2) * 1000,
                        extra = mapOf("hash" to md5),
                        id,
                        uri = mediaUri
                    )

                    withContext(Dispatchers.IO) {
                        itemCallback?.itemCallback(item, QueryStateEnum.LOADING)
                    }

                    currentPosition++
                }

                // 如果返回的数据量小于请求的页面大小，说明没有更多数据
                if (currentPosition < pageSize) {
                    hasNextPage = false
                }

                // 更新偏移量
                offset += currentPosition
            }
        }

        _isLoading = false
        itemCallback?.itemCallback(null, QueryStateEnum.FINISH)
    }


    // 使用缓存的方式获取 MD5
    private suspend fun getMD5FromFile(path: String): String? {
        return withContext(Dispatchers.IO) {
            try {
                // 如果缓存中有值，直接返回
                md5Cache[path]?.let { return@withContext it }

                val file = File(path)
                if (!file.exists()) return@withContext null

                val digest = MessageDigest.getInstance("MD5")
                val buffer = ByteArray(8192)

                FileInputStream(file).use { inputStream ->
                    var bytesRead: Int
                    while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                        digest.update(buffer, 0, bytesRead)
                    }
                }

                val md5Bytes = digest.digest()
                val md5 = md5Bytes.joinToString("") { "%02x".format(it) }

                // 将计算出来的 MD5 值缓存起来
                md5Cache[path] = md5

                return@withContext md5
            } catch (e: Exception) {
                e.printStackTrace()
                return@withContext null
            }
        }
    }

    fun checkSimilar(
        item1: CleanUpDetailData?, item2: CleanUpDetailData?, similarity: Int
    ): Boolean {
        if (item1 == null || item2 == null) {
            return false
        }
        val hash1 = item1.extra!!["hash"] as String
        val hash2 = item2.extra!!["hash"] as String

        return hash1 == hash2
    }

    fun setItemCallback(listener: ItemCallback?) {
        this.itemCallback = listener
    }

    interface ItemCallback {
        suspend fun itemCallback(item: CleanUpDetailData?, state: QueryStateEnum)
    }

    @Synchronized
    fun stopQuery() {
        _shouldStop = true
    }

    @Synchronized
    fun reSetQuery() {
        _shouldStop = false
    }

    fun clearCursor() {
        lastCursorPosition = -1
    }
}

