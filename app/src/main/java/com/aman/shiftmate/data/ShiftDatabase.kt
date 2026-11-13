package com.aman.shiftmate.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.aman.shiftmate.model.Shift

@Database(entities = [Shift::class], version = 1, exportSchema = false)
abstract class ShiftDatabase : RoomDatabase() {
    abstract fun shiftDao(): ShiftDao

    companion object {
        @Volatile
        private var instance: ShiftDatabase? = null

        // Single shared database instance for the whole app
        fun getInstance(context: Context): ShiftDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    ShiftDatabase::class.java,
                    "shiftmate.db"
                ).build().also { instance = it }
            }
    }
}