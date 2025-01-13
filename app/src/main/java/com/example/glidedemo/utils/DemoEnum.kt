package com.example.glidedemo.utils

enum class ItemTypeEnum {
    DATA, TITLE
}

/**
 * 查找状态
 */
enum class QueryStateEnum {
    LOADING, PAUSE, FINISH
}

enum class CleanTypeEnum {
    TYPE_SIMILAR, TYPE_SCREENSHOT
}

/**
 * 文件类型
 */
enum class MediaTypeEnum {
    TYPE_IMAGES, TYPE_VIDEOS, TYPE_GIF, TYPE_RAW, TYPE_SVG
}

/**
 * 弹窗点击类型
 */
enum class DialogClickEnum {
    OK, CANCEL, CLOSE
}