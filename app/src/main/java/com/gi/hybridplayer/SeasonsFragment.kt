package com.gi.hybridplayer

import android.os.Bundle
import androidx.leanback.widget.ArrayObjectAdapter
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.gi.hybridplayer.model.Vod
import com.gi.hybridplayer.view.CategoryPresenter
import com.gi.hybridplayer.view.SingleLineVerticalFragment
import com.gi.hybridplayer.viewmodel.SeriesViewModel

class SeasonsFragment : SingleLineVerticalFragment(){

    private lateinit var mRootActivity: SeriesActivity
    private lateinit var mAdapter: ArrayObjectAdapter
    private lateinit var mSeriesViewModel: SeriesViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mRootActivity = activity as SeriesActivity
        mSeriesViewModel = mRootActivity.getViewModel()
        mAdapter = ArrayObjectAdapter(CategoryPresenter())
        adapter = mAdapter
        setOnItemViewClickedListener { _, item, _, _ ->
        }
        setOnItemViewSelectedListener { _, item, _, _ ->
            if (item is Vod){
                mSeriesViewModel.select(item)
            }
        }
        val epData = mRootActivity.getEpisodeData()
        setupData(epData)
    }



    private fun setupData(epData: String){
        val om = ObjectMapper()
        val js = om.readTree(epData).get("js")
//        val totalItem = js.get("total_items").asInt()
        val data: JsonNode? = js.get("data")
        try {
            data?.forEach {
                val season = om.treeToValue(it, Vod::class.java)
                mAdapter.add(season)
            }

        }
        catch (e:Exception){
            e.printStackTrace()
        }
    }


}