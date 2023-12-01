package com.gi.hybridplayer.db.repository

import android.annotation.SuppressLint
import android.content.Context
import com.gi.hybridplayer.db.ChannelDatabase
import com.gi.hybridplayer.db.dao.ChannelsDao
import com.gi.hybridplayer.model.Channel
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

    suspend fun findListByChannel(tvGenreId: String): List<Channel> {
        return if (tvGenreId == "*"){
            if (mCachedAllChannel.isNotEmpty()){
                mCachedAllChannel
            }
            else{
                channelsDao.getChannels()
            }
        } else channelsDao.getGroup(tvGenreId)
    }
    suspend fun getFavoriteChannels(): List<Channel> {
        return channelsDao.getFavoriteChannels()
    }




}