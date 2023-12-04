package com.phoenix.phoenixplayer2.fragments

import android.os.Bundle
import android.view.View
import androidx.leanback.widget.ArrayObjectAdapter
import com.gi.hybridplayer.model.DayWeek
import com.gi.hybridplayer.view.CategoryPresenter
import com.gi.hybridplayer.view.SingleLineVerticalFragment
import com.gi.hybridplayer.viewmodel.SingleEpgViewModel
import com.phoenix.phoenixplayer2.components.SingleEpgActivity

class WeekFragment: SingleLineVerticalFragment() {
    private lateinit var mRootActivity: SingleEpgActivity
    private lateinit var mAdapter: ArrayObjectAdapter
    private lateinit var mDayViewModel: SingleEpgViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mRootActivity = activity as SingleEpgActivity
        mDayViewModel = mRootActivity.getViewModel()
        mAdapter = ArrayObjectAdapter(CategoryPresenter())
        adapter = mAdapter

        setOnItemViewClickedListener { itemViewHolder, item, rowViewHolder, row ->
        }

        setOnItemViewSelectedListener { itemViewHolder, item, rowViewHolder, row ->
            if (item is DayWeek){
                mDayViewModel.setDay(item)
            }
        }

    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val week = mRootActivity.getWeek()
        mAdapter.addAll(0, week)
        week.forEach {
            if (it.isToday == "1"){
                setSelectedPositionSmooth(week.indexOf(it))
            }
        }
    }
}