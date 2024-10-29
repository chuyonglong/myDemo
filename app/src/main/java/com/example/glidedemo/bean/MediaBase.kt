package com.example.glidedemo.bean

import java.io.Serializable

//sealed class MediaBase(val type:Int = 0){
//    data class MediaData(
//        var id: Long?,
//        var path: String,
//        var modified: Long,
//        var taken: Long,
//        var size: Long,
//    ) : MediaBase(1)
//
//    data class MediaTitle(val title: String) : MediaBase(2)
//}


open class MediaBase :Serializable


data class MediaData(
    var id: Long?,
    var path: String,
    var modified: Long,
    var taken: Long,
    var size: Long,
) : MediaBase()

data class MediaTitle(val title: String) : MediaBase()


