package com.gi.hybridplayer.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class EditGroupViewModel: ViewModel() {
    private val mGroupId: MutableLiveData<String> = MutableLiveData()
    val groupId:LiveData<String>
        get() {
            return mGroupId
        }

    fun setGroupId(groupId: String){
        this.mGroupId.value = groupId
    }
}