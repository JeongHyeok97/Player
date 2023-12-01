package com.gi.hybridplayer.db


import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.gi.hybridplayer.db.dao.PortalDao
import com.gi.hybridplayer.model.Portal

@Database(entities = [Portal::class], version = 1)
abstract class PortalDatabase : RoomDatabase() {
    abstract fun portalDao(): PortalDao

    companion object {
        @Volatile
        private var INSTANCE: PortalDatabase? = null
        private val LOCK = Any()

        fun getDatabase(context: Context): PortalDatabase {
            return INSTANCE ?: synchronized(LOCK) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PortalDatabase::class.java,
                    "portal_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}