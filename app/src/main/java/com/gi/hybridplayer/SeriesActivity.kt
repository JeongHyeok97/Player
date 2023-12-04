package com.gi.hybridplayer

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import com.gi.hybridplayer.model.Episode
import com.gi.hybridplayer.model.Playback
import com.gi.hybridplayer.viewmodel.SeriesViewModel

class SeriesActivity : FragmentActivity(){


    private lateinit var mEpisodeData: String
    private lateinit var mSeriesViewModel: SeriesViewModel
    private lateinit var mPlayback: Playback

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_series)
        mSeriesViewModel = ViewModelProvider(this)[SeriesViewModel::class.java]
        mEpisodeData = intent.getStringExtra(Episode.EPISODE_INTENT).toString()

        mPlayback = intent.getSerializableExtra(Playback.PLAYBACK_INTENT_TAG) as Playback
        supportFragmentManager.beginTransaction()
            .replace(R.id.series_category_container, SeasonsFragment())
            .replace(R.id.series_item_container, SeriesListFragment())
            .commit()



    }


    fun getPlayback():Playback{
        return mPlayback
    }


    fun getEpisodeData(): String{
        return mEpisodeData
    }

    fun getViewModel(): SeriesViewModel {
        return mSeriesViewModel

    }

}