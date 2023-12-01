package com.gi.hybridplayer.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.gi.hybridplayer.model.Category
import com.gi.hybridplayer.model.Channel

class TvViewModel : ViewModel() {
    private val mCurrentCategory = MutableLiveData<Pair<Category, List<Channel>>>()
    private val mCurrentChannel = MutableLiveData<Channel>()
    val currentCategory: LiveData<Pair<Category, List<Channel>>>
        get() {
            return mCurrentCategory
        }
    val currentChannel: LiveData<Channel>
        get() {
            return mCurrentChannel
        }

    fun setCurrentCategory(category: Pair<Category, List<Channel>>?){
        mCurrentCategory.value = category
    }
    fun setCurrentChannel(channel: Channel?){
        mCurrentChannel.value = channel
    }
}