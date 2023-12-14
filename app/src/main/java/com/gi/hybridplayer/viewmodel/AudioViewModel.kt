package com.gi.hybridplayer.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.exoplayer2.Format

class AudioViewModel: ViewModel() {
    private val mTracks = MutableLiveData<List<Format>>()
    val currentTracks: LiveData<List<Format>>
        get() {
            return mTracks
        }

    fun setCurrentTracks(tracks: List<Format>){
        this.mTracks.value = tracks
    }
}