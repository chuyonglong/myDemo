package com.example.glidedemo.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.example.glidedemo.entity.TextData

@Dao
interface TextDao {

    @Query("SELECT * FROM text_data")
    fun getAll(): List<TextData>

    @Insert
    fun insertAll(vararg textData: TextData)

    @Insert
    fun insert(textData: TextData)

    @Delete
    fun delete(textData: TextData)
}