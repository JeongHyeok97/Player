package com.gi.hybridplayer.viewmodel


import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.gi.hybridplayer.model.DayWeek

class SingleEpgViewModel : ViewModel(){


    private val mDay = MutableLiveData<DayWeek>()
    private val mPage = MutableLiveData<Int>()

    val day : LiveData<DayWeek>
        get() {
            return mDay
        }

    val page : LiveData<Int>
        get() {
            return mPage
        }

    init {
        mDay.value = null
        mPage.value = 1
    }


    fun setDay(dayWeek: DayWeek){
        mDay.value = dayWeek
    }

    fun nextPage(){
        mPage.value = mPage.value!! +1
    }
    fun prevPage(){
        mPage.value = mPage.value!! -1
    }

}