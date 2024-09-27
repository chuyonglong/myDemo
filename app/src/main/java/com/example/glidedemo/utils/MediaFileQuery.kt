package com.example.glidedemo.utils

import android.content.Context
import android.provider.MediaStore.Images
import android.provider.MediaStore.Video
import com.example.glidedemo.bean.Medium
import java.util.Calendar
import java.util.Locale


object MediaFileQuery {
    fun queryAllData(context: Context): ArrayList<Medium> {
        val media = ArrayList<Medium>()
        val contentResolver = context.contentResolver
        val imagesUri = Images.Media.EXTERNAL_CONTENT_URI
        val videosUri = Video.Media.EXTERNAL_CONTENT_URI
        val selectionArgs: Array<String> = emptyArray()
        val selection: String? = null
        val photoOrderBy = Images.Media.DATE_ADDED
        val projectionImages = arrayOf(
            Images.Media.DATA,
            Images.Media.DATE_ADDED,
            Images.Media.SIZE,
            Images.Media.DATE_MODIFIED,
            Images.Media._ID,
        )
        val cursorImages = contentResolver.query(
            imagesUri,
            projectionImages,
            selection,
            selectionArgs,
            "$photoOrderBy DESC"
        )
        cursorImages?.use { cursor ->
            val pathColumnIndex = cursor.getColumnIndexOrThrow(Images.Media.DATA)
            val dateAddedColumnIndex = cursor.getColumnIndexOrThrow(Images.Media.DATE_ADDED)
            val lastModifiedColumnIndex =
                cursor.getColumnIndexOrThrow(Images.Media.DATE_MODIFIED)
            val sizeColumnIndex = cursor.getColumnIndexOrThrow(Images.Media.SIZE)
            val idColumnIndex = cursor.getColumnIndexOrThrow(Images.Media._ID)

            while (cursor.moveToNext()) {
                val path = cursor.getString(pathColumnIndex)
                val dateAdded = cursor.getLong(dateAddedColumnIndex) * 1000
                val lastModified = cursor.getLong(lastModifiedColumnIndex) * 1000
                val size = cursor.getLong(sizeColumnIndex)
                val id: Long = cursorImages.getLong(idColumnIndex)
                val medium = Medium(
                    id,
                    path,
                    lastModified,
                    dateAdded,
                    size
                )
                media.add(medium)
            }
        }
        return media
    }

    const val DATE_FORMAT_NINE = "MM/yyyy"

    private fun dateFormat(time: Long): String? {
        val cal = Calendar.getInstance(Locale.ENGLISH)
        cal.timeInMillis = time
        android.text.format.DateFormat.format(DATE_FORMAT_NINE, cal).toString()

        return null
    }

}
