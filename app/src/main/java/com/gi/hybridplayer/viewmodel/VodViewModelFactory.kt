package com.gi.hybridplayer.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.gi.hybridplayer.conf.ConnectManager

class VodViewModelFactory(private val connectManager: ConnectManager)
    : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {

        if (modelClass.isAssignableFrom(VodViewModel::class.java)){
            @Suppress("UNCHECKED_CAST")
            return VodViewModel(connectManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel Class")

    }
}