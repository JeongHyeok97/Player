package com.gi.hybridplayer.db.repository

import android.annotation.SuppressLint
import android.content.Context
import com.gi.hybridplayer.db.ChannelDatabase
import com.gi.hybridplayer.db.dao.ChannelsDao
import com.gi.hybridplayer.model.Category
import com.gi.hybridplayer.model.Category.Companion.FAVORITE_ID
import com.gi.hybridplayer.model.Category.Companion.HISTORY_ID
import com.gi.hybridplayer.model.Channel
import com.gi.hybridplayer.model.enums.GroupChannelNumbering
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TvRepository(context: Context) {

    private val mDBScope = CoroutineScope(Dispatchers.IO)
    private val mListHashMap: MutableMap<String, MutableList<Channel>> = linkedMapOf()


    companion object{
        private var instance: TvRepository? = null
        fun getInstance(context: Context): TvRepository{
            if (instance == null){
                instance = TvRepository(context)
            }
            return instance!!
        }
    }

    private val channelsDao: ChannelsDao by lazy {
        val database = ChannelDatabase.getDatabase(context)
        database.channelsDao()
    }
    private var mCachedAllChannel = mutableListOf<Channel>()
    init {
        mDBScope.launch {
            mCachedAllChannel.addAll(channelsDao.getChannels())
        }
    }

    suspend fun insert(list:List<Channel>){
        channelsDao.applyList(list)
    }
    suspend fun clear(){
        channelsDao.clear()
    }


    suspend fun getChannels(): List<Channel> {
        return channelsDao.getChannels()
    }

    suspend fun getChannel(originalNetworkId: Long): Channel?{
        return channelsDao.getChannel(originalNetworkId)
    }


    suspend fun findListByChannel(tvGenreId: String,
                                  groupChannelNumbering: GroupChannelNumbering? = GroupChannelNumbering.Off): List<Channel> {
        val channels = when (tvGenreId) {
            "*" -> {
                mCachedAllChannel.ifEmpty {
                    channelsDao.getChannels()
                }
            }
            FAVORITE_ID -> {
                getFavoriteChannels()
            }
            else -> channelsDao.getGroup(tvGenreId)
        }
        if (groupChannelNumbering == GroupChannelNumbering.On && tvGenreId != "*"){
            channels.forEach {
                it.displayNumber = "${channels.indexOf(it) + 1}"
            }
        }
        return channels
    }
    suspend fun getFavoriteChannels(): List<Channel> {
        return channelsDao.getFavoriteChannels()
    }

    suspend fun updateChannel(channel: Channel){
        channelsDao.update(channel)
    }


    suspend fun search(keyword: String): List<Channel>{
        val list = channelsDao.search("%$keyword%")
        return list.sortedBy {
            getSimilarity(keyword, it.displayName!!)
        }
    }

    private fun getSimilarity(keyword: String, target: String): Int {
        val dp = Array(keyword.length + 1) { IntArray(target.length + 1) }

        for (i in 0..keyword.length) {
            for (j in 0..target.length) {
                when {
                    i == 0 -> dp[i][j] = j
                    j == 0 -> dp[i][j] = i
                    else -> {
                        dp[i][j] = if (keyword[i - 1] == target[j - 1]) {
                            dp[i - 1][j - 1]
                        } else {
                            1 + minOf(dp[i][j - 1], dp[i - 1][j], dp[i - 1][j - 1])
                        }
                    }
                }
            }
        }

        return dp[keyword.length][target.length]
    }


}