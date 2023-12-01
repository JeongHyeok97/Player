package com.gi.hybridplayer.db.repository

import android.content.Context
import com.gi.hybridplayer.db.ChannelDatabase
import com.gi.hybridplayer.db.PortalDatabase
import com.gi.hybridplayer.db.dao.ChannelsDao
import com.gi.hybridplayer.db.dao.PortalDao

class TvRepository(context: Context) {
    private val mContext:Context
    private val channelsDao: ChannelsDao by lazy {
        val database = ChannelDatabase.getDatabase(context)
        database.channelsDao()
    }
    init {
        this.mContext = context
    }

}