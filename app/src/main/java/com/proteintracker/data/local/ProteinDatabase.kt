package com.proteintracker.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [ProteinEntry::class],
    version = 1,
    exportSchema = false,
)
abstract class ProteinDatabase : RoomDatabase() {

    abstract fun proteinDao(): ProteinDao

    companion object {
        private const val NAME = "protein.db"

        @Volatile
        private var instance: ProteinDatabase? = null

        fun getInstance(context: Context): ProteinDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    ProteinDatabase::class.java,
                    NAME,
                ).build().also { instance = it }
            }
    }
}
