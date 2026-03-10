package com.phood.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [FoodEntry::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class PhoodDatabase : RoomDatabase() {

    abstract fun dailyLogDao(): DailyLogDao

    companion object {
        @Volatile
        private var INSTANCE: PhoodDatabase? = null

        fun getInstance(context: Context): PhoodDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    PhoodDatabase::class.java,
                    "phood_database"
                ).build().also { INSTANCE = it }
            }
        }
    }
}
