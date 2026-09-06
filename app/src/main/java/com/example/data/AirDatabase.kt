package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.model.AirRecord

@Database(entities = [AirRecord::class], version = 1, exportSchema = false)
abstract class AirDatabase : RoomDatabase() {
    abstract fun airRecordDao(): AirRecordDao

    companion object {
        @Volatile
        private var INSTANCE: AirDatabase? = null

        fun getDatabase(context: Context): AirDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AirDatabase::class.java,
                    "aeroscan_pm25.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
