package com.gi.hybridplayer.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.gi.hybridplayer.model.Channel

class SearchResultViewModel: ViewModel() {

    private val mResult = MutableLiveData<List<Channel>>()
    val result :LiveData<List<Channel>>
        get() {
            return mResult
        }

    fun setResult(list: List<Channel>){
        this.mResult.value = list
    }
}