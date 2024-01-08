package com.gi.hybridplayer.db.dao

import androidx.room.*
import com.gi.hybridplayer.model.Channel


@Dao
interface ChannelsDao {
    @Query("SELECT * FROM channel WHERE isLock = 0")
    suspend fun getChannels():List<Channel>


    @Query("SELECT * FROM channel WHERE originalNetworkId = :originalNetworkId")
    suspend fun getChannel(originalNetworkId: Long):Channel?


    @Insert
    suspend fun insert(channel: Channel)

    @Insert
    suspend fun applyList(channels:List<Channel>)

    @Delete
    suspend fun delete(channel: Channel)

    @Query("DELETE FROM channel")
    suspend fun clear()


    @Query("SELECT COUNT(*) FROM channel")
    suspend fun getChannelCount(): Int

    @Query("SELECT * FROM channel WHERE genreId = :genreId")
    suspend fun getGroup(genreId:String): List<Channel>


    @Query("SELECT * FROM channel WHERE isFavorite = 1")
    suspend fun getFavoriteChannels():List<Channel>


    @Update
    suspend fun update(channel: Channel)


    @Query("SELECT * FROM channel WHERE displayName LIKE :searchQuery COLLATE NOCASE AND isLock = 0")
    suspend fun search(searchQuery: String): List<Channel>

}