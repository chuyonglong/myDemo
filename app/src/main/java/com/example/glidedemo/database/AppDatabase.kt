package com.example.glidedemo.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.glidedemo.App
import com.example.glidedemo.bean.SimilarImagesData
import com.example.glidedemo.dao.TextDao
import com.example.glidedemo.entity.TextData
import com.example.glidedemo.interfaces.SimilarImagesDao

@Database(entities = [TextData::class, SimilarImagesData::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun TextDao(): TextDao

    abstract fun SimilarImagesDao(): SimilarImagesDao

    companion object {
        private var db: AppDatabase? = null

        fun getDataInstance(): AppDatabase {
            if (db == null) {
                synchronized(AppDatabase::class) {
                    if (db == null) {
                        db = Room.databaseBuilder(
                            App.getContext(),
                            AppDatabase::class.java,
                            "app_data.db"
                        ).fallbackToDestructiveMigration().build()
                    }
                }
            }
            return db!!
        }

        fun destroyDataInstance() {
            if (db?.isOpen == true) {
                db?.close()
            }
            db = null
        }
    }
}