package com.gi.hybridplayer

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.gi.hybridplayer.model.History

@Database(entities = [History::class], version = 1)
abstract class VideoHistoryDatabase : RoomDatabase() {
    abstract fun historyDao(): HistoryDao

    companion object {
        @Volatile
        private var INSTANCE: VideoHistoryDatabase? = null
        private val LOCK = Any()

        fun getDatabase(context: Context): VideoHistoryDatabase {
            return INSTANCE ?: synchronized(LOCK) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    VideoHistoryDatabase::class.java,
                    "history_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}