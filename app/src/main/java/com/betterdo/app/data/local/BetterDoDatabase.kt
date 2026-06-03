package com.betterdo.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [TodoEntity::class], version = 1, exportSchema = false)
abstract class BetterDoDatabase : RoomDatabase() {

    abstract fun todoDao(): TodoDao

    companion object {
        @Volatile
        private var instance: BetterDoDatabase? = null

        fun get(context: Context): BetterDoDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    BetterDoDatabase::class.java,
                    "betterdo.db",
                ).fallbackToDestructiveMigration().build().also { instance = it }
            }
    }
}
