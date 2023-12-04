package com.gi.hybridplayer

import android.content.Context
import com.gi.hybridplayer.model.History

class HistoryRepository(context:Context,
                        private val portalId: Long) {

    private val historyDao: HistoryDao by lazy {
        val database = VideoHistoryDatabase.getDatabase(context)
        database.historyDao()
    }
    suspend fun insert(history: History) {
        historyDao.insert(history)
    }


    suspend fun getHistory(): List<History>{
        return historyDao.getHistory(portalId)
    }
}