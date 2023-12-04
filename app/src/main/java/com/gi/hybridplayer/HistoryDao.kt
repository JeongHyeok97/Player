package com.gi.hybridplayer

import androidx.lifecycle.LiveData
import androidx.room.*
import androidx.room.Update
import com.gi.hybridplayer.model.History

@Dao
interface HistoryDao {
    @Query("SELECT * FROM vod_history WHERE portalId = :portalId")
    fun getLiveHistory(portalId: Long): LiveData<List<History>>
    @Query("SELECT * FROM vod_history WHERE portalId = :portalId")
    suspend fun getHistory(portalId: Long): List<History>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(history: History)

    @Update
    suspend fun update(history: History)

    @Delete
    suspend fun delete(history: History): Int

    @Query("DELETE FROM vod_history")
    suspend fun clear()
}