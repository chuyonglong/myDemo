package com.example.glidedemo.interfaces

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.glidedemo.bean.SimilarImagesData

@Dao
interface SimilarImagesDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(similarImagesData: SimilarImagesData)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(similarImagesData: List<SimilarImagesData>)

    @Query("SELECT media_store_id,image_name,image_path,create_time,update_time,bitmap_hash,size,group_id FROM similar_images_data WHERE media_store_id = :mediaStoreId")
    fun getSimilarDataByMediaStoreId(mediaStoreId: Long): List<SimilarImagesData>

    @Query("SELECT media_store_id,image_name,image_path,create_time,update_time,bitmap_hash,size,group_id FROM similar_images_data where group_id is not null ")
    fun getAllGroupSimilar(): List<SimilarImagesData>

    @Query("DELETE FROM similar_images_data WHERE media_store_id = :mediaStoreId ")
    fun deleteSimilarByMediaStoreId(mediaStoreId: Long)

    @Query("SELECT group_id FROM similar_images_data WHERE media_store_id = :mediaStoreId")
    fun getGroupIdByMediaStoreId(mediaStoreId: Long): String?
}
