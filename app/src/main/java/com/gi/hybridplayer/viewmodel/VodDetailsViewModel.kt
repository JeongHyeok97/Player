package com.gi.hybridplayer.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.gi.hybridplayer.model.Credits
import com.gi.hybridplayer.model.Vod

class VodDetailsViewModel: ViewModel() {

    private val mSelectedVod = MutableLiveData<Vod>()
    private val mSelectedDetail=MutableLiveData<Vod.Detail>()
    private val mCast = MutableLiveData<List<Credits.Cast>>()
    private val mCrew = MutableLiveData<List<Credits.Crew>>()


    val selectedVod :LiveData<Vod>
        get() {
            return mSelectedVod
        }

    val selectedDetail:LiveData<Vod.Detail>
        get() {
            return mSelectedDetail
        }

    val cast :LiveData<List<Credits.Cast>>
        get() {
            return mCast
        }
    val crew : LiveData<List<Credits.Crew>>
        get() {
            return mCrew
        }


    fun select(vod: Vod){
        this.mSelectedVod.value = vod
    }


    fun setDetail(detail: Vod.Detail){
        this.mSelectedDetail.value = detail
    }

    fun setCredits(cast: List<Credits.Cast>, crew: List<Credits.Crew>){
        this.mCast.value=cast
        this.mCrew.value=crew
    }

}