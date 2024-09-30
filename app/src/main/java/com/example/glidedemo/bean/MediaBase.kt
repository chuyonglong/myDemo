package com.example.glidedemo.bean

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


open class MediaBase


data class MediaData(
    var id: Long?,
    var path: String,
    var modified: Long,
    var taken: Long,
    var size: Long,
) : MediaBase()

data class MediaTitle(val title: String) : MediaBase()


