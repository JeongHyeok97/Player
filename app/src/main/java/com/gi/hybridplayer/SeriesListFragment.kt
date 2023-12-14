package com.gi.hybridplayer

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import androidx.leanback.widget.ArrayObjectAdapter
import androidx.leanback.widget.VerticalGridView
import com.fasterxml.jackson.databind.ObjectMapper
import com.gi.hybridplayer.conf.CloudDBManager
import com.gi.hybridplayer.conf.ConnectManager
import com.gi.hybridplayer.model.Episode
import com.gi.hybridplayer.model.Playback
import com.gi.hybridplayer.model.Vod
import com.gi.hybridplayer.view.SeriesItemPresenter
import com.gi.hybridplayer.view.SingleLineVerticalFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.Serializable

class SeriesListFragment(val season: Int? = -1) : SingleLineVerticalFragment(){
    private lateinit var mRootActivity: SeriesActivity
    private lateinit var mAdapter: ArrayObjectAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mRootActivity = activity as SeriesActivity
        val pb = mRootActivity.getPlayback()
        var connectManager: ConnectManager? = null
        if (pb.portal != null){
            connectManager = ConnectManager(pb.portal!!)
        }
        mAdapter = ArrayObjectAdapter(SeriesItemPresenter())
        adapter = mAdapter
        setOnItemViewClickedListener { _, item, _, _ ->
            if (item is Episode){
                CoroutineScope(Dispatchers.IO).launch {
                    if (connectManager != null){
                        val url = connectManager.createLink(
                            Vod.TYPE_VOD,
                        item.cmd!!,
                        item.episodeNumber.toString())
                        if (url != null){
                            val list = arrayListOf<Episode>()
                            for (i in 0 until mAdapter.size()){
                                val ep = mAdapter.get(i)
                                list.add(ep as Episode)
                            }
                            val playback = pb.copy(url = url)
                            withContext(Dispatchers.Main){
                                val intent = Intent(requireActivity(), PlaybackActivity::class.java)
                                intent.putExtra(Playback.PLAYBACK_INTENT_TAG, playback)
                                intent.putExtra("list", list as Serializable)
                                startActivity(intent)
                            }
                        }
                    }
                }
            }
        }

    }

    val handler: Handler = Handler(Looper.getMainLooper())
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val gridView = view.findViewById<VerticalGridView>(androidx.leanback.R.id.browse_grid)
        val om = ObjectMapper()
        mRootActivity.getViewModel().selectedSeason.observe(viewLifecycleOwner){
                series ->
            gridView.visibility = INVISIBLE
            handler.removeCallbacksAndMessages(null)
            mAdapter.clear()
            val cloudDBManager = CloudDBManager()
            CoroutineScope(Dispatchers.IO).launch {
                val seasonId: String = series.id!!
                val parse = seasonId.indexOf(":") + 1
                val season = seasonId.substring(parse)
                val epNode = om.readTree(cloudDBManager.getEpisodes(series.tmdbId!!, season))
                    .get("episodes")
                handler.postDelayed({
                    epNode.forEach {
                            ep->
                        val episode = om.treeToValue(ep, Episode::class.java)
                        episode.cmd = series.cmd
                        mAdapter.add(episode)
                    }
                    gridView.visibility = VISIBLE
                }, 750)
            }
        }
    }
}