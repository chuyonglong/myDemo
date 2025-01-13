package com.example.glidedemo.bean

import androidx.annotation.Keep
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import java.io.Serializable

/**
 * 相似图片记录表
 */
@Keep
@Entity(
    tableName = "similar_images_data",
    primaryKeys = ["media_store_id"],
    indices = [Index(value = ["media_store_id"], unique = true)]
)
data class SimilarImagesData(
    //media_store_id 主键
    @ColumnInfo(name = "media_store_id") var mediaStoreId: Long,
    //图片名
    @ColumnInfo(name = "image_name") var imageName: String,
    //图片地址
    @ColumnInfo(name = "image_path") var imagePath: String,
    //图片创建时间
    @ColumnInfo(name = "create_time") var createTime: Long,
    //图片最后修改时间
    @ColumnInfo(name = "update_time") var updateTime: Long,
    //图片hashcode
    @ColumnInfo(name = "bitmap_hash") var bitmapHash: String,
    //图片大小
    @ColumnInfo(name = "size") var size: Long,
    //分组uuid
    @ColumnInfo(name = "group_id") var groupId: String?
) : Serializable, BaseData() {
    companion object {
        private const val serialVersionUID = -6553149366975654L
    }


}
