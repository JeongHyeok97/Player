package com.gi.hybridplayer.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.gi.hybridplayer.model.Channel


@Dao
interface ChannelsDao {
    @Query("SELECT * FROM channel")
    suspend fun getChannels():List<Channel>


    @Query("SELECT * FROM channel WHERE originalNetworkId = :originalNetworkId")
    suspend fun getChannel(originalNetworkId: Long){
    }
    @Insert
    suspend fun insert(channel: Channel)

    @Insert
    suspend fun applyList(channels:List<Channel>)

    @Delete
    suspend fun delete(channel: Channel)

    @Query("DELETE FROM channel")
    suspend fun deleteAll()


    @Query("SELECT COUNT(*) FROM channel")
    suspend fun getChannelCount(): Int

}