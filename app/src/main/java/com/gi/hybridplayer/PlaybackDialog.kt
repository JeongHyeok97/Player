package com.gi.hybridplayer

import android.media.tv.TvTrackInfo
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import androidx.leanback.widget.ArrayObjectAdapter
import androidx.leanback.widget.VerticalGridView
import com.gi.hybridplayer.view.SingleLineVerticalFragment
import com.gi.hybridplayer.view.TrackPresenter

class PlaybackDialog(val langList: MutableList<TvTrackInfo?>) : SingleLineVerticalFragment(){

    private val mAdapter: ArrayObjectAdapter = ArrayObjectAdapter(TrackPresenter())


    init {
        mAdapter.addAll(0, langList)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        adapter = mAdapter
        setOnItemViewClickedListener { itemViewHolder, item, rowViewHolder, row ->
            if (item is TvTrackInfo){
                val playbackFragment = activity?.supportFragmentManager?.findFragmentById(R.id.playback_container)
                if (playbackFragment is PlaybackVideoFragment){
                    playbackFragment.selectTrack(item)
                    activity?.supportFragmentManager?.popBackStack()
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val lp = view.layoutParams as FrameLayout.LayoutParams
        lp.topMargin = -120
        lp.width = resources.getDimensionPixelSize(R.dimen.dialog_width)
        lp.height = resources.getDimensionPixelSize(R.dimen.dialog_height)
        view.layoutParams= lp
        view.background = ContextCompat.getDrawable(requireContext(), R.drawable.default_background)
        if (mAdapter.size()>0){
            view.findViewById<VerticalGridView>(androidx.leanback.R.id.browse_grid).requestFocus()
        }
    }
}
