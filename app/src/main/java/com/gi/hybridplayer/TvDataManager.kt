package com.gi.hybridplayer

import android.content.ComponentName
import android.content.ContentProviderOperation
import android.content.Context
import android.content.OperationApplicationException
import android.media.tv.TvContract
import android.media.tv.TvInputInfo
import android.os.RemoteException
import android.util.Log
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.gi.hybridplayer.conf.ConnectManager
import com.gi.hybridplayer.db.repository.TvRepository
import com.gi.hybridplayer.model.Channel
import com.gi.hybridplayer.model.Portal
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class TvDataManager(context: Context, portal: Portal) {

    companion object{
        const val TAG = "TvDataManager"
    }
    private val objectMapper = ObjectMapper()
    private val mContext: Context
    private val mPortal: Portal
    init {
        this.mContext = context
        this.mPortal = portal
    }
    interface StateListener{
        fun onStarted()
        fun onProgress(percent:Int)
        suspend fun onConnect(portal: Portal)

    }

    suspend fun insertAllChannels(allChannelsData: String,
                                  blockedChannelsData: List<Channel>,
                                  stateListener:StateListener){
        val repository = TvRepository.getInstance(context = mContext)
        repository.clear()
        try {
            val dataNode = objectMapper.readTree(allChannelsData)[ConnectManager.NODE_JS][ConnectManager.NODE_DATA]
            val totalChannels = dataNode.size()
            val batchSize = 100
            for (startIndex in 0 until totalChannels step batchSize) {
                val percent = startIndex*100/totalChannels
                withContext(Dispatchers.Main){
                    stateListener.onProgress(percent)
                }
                val endIndex = minOf(startIndex + batchSize, totalChannels)
                val channelList = mutableListOf<Channel>()
                for (i in startIndex until endIndex) {
                    val node = dataNode.get(i)
                    val channel = objectMapper.treeToValue(node, Channel::class.java)

                    channel.packageName = mContext.packageName
                    channelList.add(channel)
                }
                repository.insert(channelList)
            }
        }
        catch (e:Exception){
            e.printStackTrace()
        }
        repository.insert(blockedChannelsData)
        mPortal.connected = true
        stateListener.onConnect(portal = mPortal)
    }


}