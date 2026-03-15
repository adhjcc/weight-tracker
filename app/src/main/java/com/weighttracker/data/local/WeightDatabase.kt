package com.weighttracker.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [WeightRecord::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class WeightDatabase : RoomDatabase() {
    abstract fun weightRecordDao(): WeightRecordDao

    companion object {
        @Volatile
        private var INSTANCE: WeightDatabase? = null

        fun getDatabase(context: Context): WeightDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    WeightDatabase::class.java,
                    "weight_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
