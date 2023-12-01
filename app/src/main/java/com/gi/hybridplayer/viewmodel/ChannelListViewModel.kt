package com.gi.hybridplayer.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.gi.hybridplayer.model.Category

class ChannelListViewModel : ViewModel(){
    private val mSelectedCategory= MutableLiveData<Category>()
    val selectedCategory : LiveData<Category>
        get() {
            return mSelectedCategory
        }

    fun setSelectedCategory(category: Category){
        this.mSelectedCategory.value = category
    }
}