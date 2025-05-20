package com.example.glidedemo.interfaces

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.glidedemo.entity.NotificationEntity

@Dao
interface NotificationEntityDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(notificationEntity: NotificationEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(notificationEntity: List<NotificationEntity>)

    @Query("DELETE FROM notification_entity WHERE id = :deleteId")
    fun delete(deleteId: Long)

    @Query("UPDATE OR REPLACE notification_entity SET isRead = :isRead  WHERE id = :updateId ")
    fun updateFavorite(isRead: Boolean, updateId: Int)

    @Query("SELECT * FROM notification_entity order by postTime DESC")
    fun selectAllNotificationEntity(): List<NotificationEntity>
}
