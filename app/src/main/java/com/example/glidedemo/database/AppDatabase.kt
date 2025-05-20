package com.example.glidedemo.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.glidedemo.App
import com.example.glidedemo.bean.SimilarImagesData
import com.example.glidedemo.dao.TextDao
import com.example.glidedemo.entity.NotificationEntity
import com.example.glidedemo.entity.TextData
import com.example.glidedemo.interfaces.NotificationEntityDao
import com.example.glidedemo.interfaces.SimilarImagesDao

@Database(
    entities = [TextData::class, SimilarImagesData::class, NotificationEntity::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun TextDao(): TextDao

    abstract fun SimilarImagesDao(): SimilarImagesDao

    abstract fun NotificationEntityDao(): NotificationEntityDao

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
                        ).fallbackToDestructiveMigration()
                            .addMigrations(COMPATIBLE_MIGRATION_1_2)
                            .build()
                    }
                }
            }
            return db!!
        }

        private val COMPATIBLE_MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS notification_entity (" +
                            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                            "title TEXT," +
                            "message TEXT," +
                            "postTime INTEGER NOT NULL," +
                            "packageName TEXT NOT NULL," +
                            "isRead INTEGER DEFAULT 0" + ")"
                )
            }
        }


        fun destroyDataInstance() {
            if (db?.isOpen == true) {
                db?.close()
            }
            db = null
        }
    }
}