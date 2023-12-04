package com.gi.hybridplayer.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.gi.hybridplayer.conf.ConnectManager
import com.gi.hybridplayer.model.Vod
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class VodViewModel
    (private val connectManager: ConnectManager)
    : ViewModel() {

    private val category = MutableLiveData<String>()
    private val vodList = MutableLiveData<Pair<Boolean, MutableList<Vod>>>()
    private val mAddList = MutableLiveData<MutableList<Vod>>()
    private val mType = MutableLiveData<String>()
    private val portalHost = connectManager.getHost()


    val selectedCategory:LiveData<String>
        get() {
            return category
        }

    val selectedVodList:LiveData<Pair<Boolean, MutableList<Vod>>>
        get() {
            return vodList
        }

    val addedList : LiveData<MutableList<Vod>>
        get() {
            return mAddList
        }

    val type : LiveData<String>
        get() {
            return mType
        }

    init {
        vodList.value = Pair(false, mutableListOf())
    }



    fun setTitle(title:String){
        this.category.value = title
    }

    fun add(id:String, type: String, page:Int){
        viewModelScope.launch(Dispatchers.IO){
            val addedPage: MutableList<Vod> = mutableListOf()
            val orderedListApi : String =
                connectManager.getOrderedList(
                    type = type,
                    categoryID = id,
                    page = page)
            val om = ObjectMapper()
            try {
                val listNode: JsonNode = om
                    .readTree(orderedListApi)
                    .get("js")
                    .get("data")
                listNode.forEach {
                    val vod = om.treeToValue(it, Vod::class.java)
                    addedPage.add(vod)
                }
                withContext(Dispatchers.Main){
                    this@VodViewModel.mAddList.value = addedPage
                }
            }
            catch (exception: NullPointerException){
                exception.printStackTrace()
            }

        }
    }
    fun attach(list:MutableList<Vod>){
        this.vodList.value = Pair(false, list)
    }

    fun select(id: String, type:String, censored: Boolean? = false) {
        viewModelScope.launch(Dispatchers.IO){

            val firstPage: MutableList<Vod> = mutableListOf()
            val orderedListApi : String =
                connectManager.getOrderedList(
                    type = type,
                    categoryID = id,
                    page = 1)

            val om = ObjectMapper()
            try {
                val listNode: JsonNode = om
                    .readTree(orderedListApi)
                    .get("js")
                    .get("data")
                listNode.forEach {
                    val vod = om.treeToValue(it, Vod::class.java)
                    if (vod.screenshotUri?.startsWith("/") == true){
                        vod.screenshotUri = portalHost + vod.screenshotUri
                    }
                    firstPage.add(vod)
                }
                withContext(Dispatchers.Main){
                    this@VodViewModel.mType.value = type
                    this@VodViewModel.vodList.value = Pair(censored!!, firstPage)
                }
            }
            catch (exception: NullPointerException){
                exception.printStackTrace()
            }

        }


//        Log.d("TAG ", "$listNode")
    }
}