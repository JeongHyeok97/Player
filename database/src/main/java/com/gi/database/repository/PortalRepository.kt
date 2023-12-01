package com.gi.database.repository


import android.content.Context
import androidx.lifecycle.LiveData
import com.gi.database.PortalDatabase
import com.gi.database.dao.PortalDao
import com.gi.database.model.Portal


class PortalRepository(context: Context) {
    private val portalDao: PortalDao by lazy {
        val database = PortalDatabase.getDatabase(context)
        database.portalDao()
    }

    suspend fun insert(portal: Portal) {
        portalDao.insert(portal)
    }

    fun getPortals(): LiveData<List<Portal>> {
        return portalDao.getPortals()
    }

    suspend fun connect(){
        val list = portalDao.getAll()
        list.forEach {
            val portal = it.copy(connected = false)
            portal.id = it.id
            portalDao.update(portal)

        }

    }

    suspend fun getConnectedPortal():Portal?{
        val list = portalDao.getConnectedPortal()
        list.forEach {
            if (it.connected){
                return it
            }
        }
        return null
    }

    suspend fun update(portal: Portal) {
        portalDao.update(portal)
    }

    suspend fun delete(portal: Portal): Int {
        return portalDao.delete(portal)
    }

    suspend fun clear(){
        portalDao.clear()
    }
}