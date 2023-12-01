package com.gi.database.dao


import androidx.lifecycle.LiveData
import androidx.room.*
import com.gi.database.model.Portal

@Dao
interface PortalDao {
    @Query("SELECT * FROM portals")
    fun getPortals(): LiveData<List<Portal>>

    @Query("SELECT * FROM portals")
    fun getAll(): List<Portal>

    @Query("SELECT * FROM portals WHERE connected = 1")
    suspend fun getConnectedPortal(): List<Portal>




    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(portal: Portal)

    @Update
    suspend fun update(portal: Portal)

    @Delete
    suspend fun delete(portal: Portal): Int

    @Query("DELETE FROM portals")
    suspend fun clear()
}