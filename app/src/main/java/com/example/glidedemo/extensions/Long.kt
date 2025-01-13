package com.example.glidedemo.extensions

import java.text.DecimalFormat
import java.util.Calendar
import java.util.Locale

fun Long.getDayStartTS(): String {
    val calendar = Calendar.getInstance(Locale.ENGLISH).apply {
        timeInMillis = this@getDayStartTS
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    return (calendar.timeInMillis / 1000).toString()
}

fun Long.getLastDayTS(): String {
    val calendar = Calendar.getInstance(Locale.ENGLISH).apply {
        timeInMillis = this@getLastDayTS
        set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
        set(Calendar.HOUR_OF_DAY, 23)
        set(Calendar.MINUTE, 59)
        set(Calendar.SECOND, 59)
        set(Calendar.MILLISECOND, 999)
    }

    return (calendar.timeInMillis / 1000).toString()
}


fun Long.formatSize(): String {
    if (this <= 0) {
        return "0 B"
    }

    val units = arrayOf("B", "kB", "MB", "GB", "TB")
    val digitGroups = (Math.log10(toDouble()) / Math.log10(1024.0)).toInt()
    return "${
        DecimalFormat("#,##0.#").format(
            this / Math.pow(
                1024.0,
                digitGroups.toDouble()
            )
        )
    } ${units[digitGroups]}"
}





fun Long.getMonthStartTS(): String {
    val calendar = Calendar.getInstance().apply {
        timeInMillis = this@getMonthStartTS
        set(Calendar.DAY_OF_MONTH, 1)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    return calendar.timeInMillis.toString()
}


const val KB = 1000
const val MB = 1000 * 1000
const val GB = 1000 * 1000 * 1000
fun Long.byte2FitMemorySizeToString() = when {
    this <= 0 -> "0B"
    this < KB -> "${String.format(Locale.ENGLISH, "%." + 1 + "f", this.toDouble())}B"
    this < MB -> "${String.format(Locale.ENGLISH, "%." + 1 + "f", this.toDouble() / KB)}KB"
    this < GB -> "${String.format(Locale.ENGLISH, "%." + 1 + "f", this.toDouble() / MB)}MB"
    else -> "${String.format(Locale.ENGLISH, "%." + 1 + "f", this.toDouble() / GB)}GB"
}

fun Long.byte2FitMemorySizeToStringAndUnit(callback: (String, String) -> Unit) {
    when {
        this <= 0 -> callback("0", "B")
        this < KB -> callback(String.format(Locale.ENGLISH, "%." + 1 + "f", this.toDouble()), "B")
        this < MB -> callback(
            String.format(Locale.ENGLISH, "%." + 1 + "f", this.toDouble() / KB),
            "KB"
        )

        this < GB -> callback(
            String.format(Locale.ENGLISH, "%." + 1 + "f", this.toDouble() / MB),
            "MB"
        )

        else -> callback(String.format(Locale.ENGLISH, "%." + 1 + "f", this.toDouble() / GB), "GB")

    }
}




