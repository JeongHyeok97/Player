package com.gi.hybridplayer.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.gi.hybridplayer.db.dao.ChannelsDao
import com.gi.hybridplayer.model.Channel


@Database(entities = [Channel::class], version = 1)
abstract class ChannelDatabase : RoomDatabase() {
    abstract fun channelsDao(): ChannelsDao

    companion object {
        @Volatile
        private var INSTANCE: ChannelDatabase? = null
        private val LOCK = Any()

        fun getDatabase(context: Context): ChannelDatabase {
            return INSTANCE ?: synchronized(LOCK) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ChannelDatabase::class.java,
                    "channel_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}