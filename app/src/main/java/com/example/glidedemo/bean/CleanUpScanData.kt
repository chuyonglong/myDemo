package com.example.glidedemo.bean

import androidx.annotation.Keep
import com.example.glidedemo.utils.CleanTypeEnum
import java.io.Serializable

/**
 * 清理页面列表数据
 */
@Keep
data class CleanUpScanData(
    //类型
    var type: CleanTypeEnum,
    //标题
    var title: String,
    //显示大小
    var sizeStr: String,
    //内容
    var content: String,
    //数量
    var count: Int,
    //大小
    var size: Long,
    //文件显示
    var pathList: ArrayList<ScanMediaData>
) : Serializable

