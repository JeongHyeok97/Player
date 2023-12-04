package com.gi.hybridplayer


import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import com.gi.hybridplayer.model.Playback

class PlaybackActivity: FragmentActivity() {
    lateinit var playback: Playback

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        playback = intent.getSerializableExtra(Playback.PLAYBACK_INTENT_TAG) as Playback

        setContentView(R.layout.activity_playback)
       /* if (savedInstanceState == null) {
            val videoFragment = PlaybackVideoFragment()
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.playback_container, videoFragment)
                .commit()
        }*/
    }

    override fun onStop() {
        super.onStop()
        finish()
    }
}