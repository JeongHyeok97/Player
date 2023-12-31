package com.gi.hybridplayer.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.gi.hybridplayer.model.Portal

class PortalViewModel: ViewModel() {

    companion object {
        private const val TAG: String = "PortalViewModel"
    }

    private val portals = MutableLiveData<List<Portal>>()

    val data : LiveData<List<Portal>>
        get() {
            return portals
        }

    init {
        portals.value = listOf()
    }

    fun set(portalList: List<Portal>){
        portals.value = portalList
    }
}