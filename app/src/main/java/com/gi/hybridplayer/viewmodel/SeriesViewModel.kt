package com.gi.hybridplayer.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.gi.hybridplayer.model.Vod

class SeriesViewModel : ViewModel(){


    private val mSelectedSeason = MutableLiveData<Vod>()


    val selectedSeason : LiveData<Vod>
        get() {
            return mSelectedSeason
        }




    fun select(season: Vod){
        this.mSelectedSeason.value = season
    }
}