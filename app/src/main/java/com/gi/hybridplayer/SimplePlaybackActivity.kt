package com.gi.hybridplayer

import android.R
import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import com.phoenix.phoenixplayer2.fragments.SimplePlaybackFragment

class SimplePlaybackActivity: FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val bundle = intent.getBundleExtra(SimplePlayback.SIMPLE_PLAYBACK_BUNDLE) as Bundle
        val title = bundle.getString(SimplePlayback.SIMPLE_PLAYBACK_TITLE)!!
        val subtitle = bundle.getString(SimplePlayback.SIMPLE_PLAYBACK_SUBTITLE)!!
        val url = bundle.getString(SimplePlayback.SIMPLE_PLAYBACK_URL)


        if (savedInstanceState == null) {
            val videoFragment = SimplePlaybackFragment(title, subtitle, url.toString())
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.content, videoFragment)
                .commit()
        }

    }
    object SimplePlayback {
        const val SIMPLE_PLAYBACK_BUNDLE = "simple_playback"
        const val SIMPLE_PLAYBACK_TITLE = "simple_playback_title"
        const val SIMPLE_PLAYBACK_SUBTITLE = "simple_playback_subtitle"
        const val SIMPLE_PLAYBACK_URL = "simple_playback_url"
        const val SIMPLE_PLAYBACK_VIDEO_TYPE = "video_type"
    }
}