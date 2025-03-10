package com.example.glidedemo.utils

import com.example.glidedemo.App
import com.tencent.mmkv.MMKV

object MMKVUtils {

    /**
     * 清理文件大小,首页显示
     */
    private const val CLEANUP_SIZE: String = "mmkv_c90a53be2420a5ec"

    /**
     * 相似图片数量
     */
    private const val SIMILAR_COUNT: String = "mmkv_956a8b33ecf8e6e3"


    private val mmkv = mmkv()


    private fun mmkv(): MMKV {
        val rootDir = MMKV.getRootDir()
        if (rootDir == null) {
            MMKV.initialize(App.getContext())
        }
        return MMKV.defaultMMKV()
    }

    var cleanupSize: String
        get() = mmkv.getString(CLEANUP_SIZE, "").toString()
        set(value) {
            mmkv.putString(CLEANUP_SIZE, value)
        }


    var similarCount: String
        get() = mmkv.getString(SIMILAR_COUNT, "").toString()
        set(value) {
            mmkv.putString(SIMILAR_COUNT, value)
        }


}