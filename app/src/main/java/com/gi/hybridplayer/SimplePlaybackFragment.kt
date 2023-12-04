package com.phoenix.phoenixplayer2.fragments

import android.annotation.TargetApi
import android.net.Uri
import android.os.Build
import androidx.leanback.app.VideoSupportFragment
import androidx.leanback.app.VideoSupportFragmentGlueHost
import androidx.leanback.media.PlaybackGlue
import androidx.leanback.media.PlaybackTransportControlGlue
import androidx.leanback.widget.PlaybackSeekDataProvider
import com.gi.hybridplayer.SimplePlaybackActivity
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.ext.leanback.LeanbackPlayerAdapter
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSource
import com.google.android.exoplayer2.util.Util
import java.io.File
import java.util.*

class SimplePlaybackFragment(private val title:String,
                             private val subtitle:String,
                             private val url:String) : VideoSupportFragment() {
    var mPlayer: ExoPlayer? = null
    var mPlayerGlue: PlaybackTransportControlGlue<LeanbackPlayerAdapter>? = null
    var mPlayerAdapter: LeanbackPlayerAdapter? = null
    var mTrackSelector: DefaultTrackSelector? = null
    override fun onStart() {
        super.onStart()
        if (Util.SDK_INT > 23) {
            initializePlayer()
        }
    }

    override fun onResume() {
        super.onResume()
        if (Util.SDK_INT <= 23 || mPlayer == null) {
            initializePlayer()
        }
    }

    /** Pauses the player.  */
    @TargetApi(Build.VERSION_CODES.N)
    override fun onPause() {
        super.onPause()
        if (mPlayerGlue != null && mPlayerGlue!!.isPlaying) {
            mPlayerGlue!!.pause()
        }
        if (Util.SDK_INT <= 23) {
            releasePlayer()
        }
    }

    override fun onStop() {
        super.onStop()
        if (Util.SDK_INT > 23) {
            releasePlayer()
        }
    }

    private fun initializePlayer() {
        val builder = ExoPlayer.Builder(requireContext())
        mTrackSelector = DefaultTrackSelector(requireContext())
        builder.setTrackSelector(mTrackSelector!!)
        mPlayer = builder.build()
        mPlayerAdapter = LeanbackPlayerAdapter(requireActivity(), mPlayer!!, 16)
        mPlayerGlue = PlaybackTransportControlGlue(context, mPlayerAdapter)
        mPlayerGlue!!.host = VideoSupportFragmentGlueHost(this)
        mPlayerGlue!!.playWhenPrepared()
        mPlayerGlue!!.isControlsOverlayAutoHideEnabled = true
        mPlayerGlue!!.seekProvider = PlaybackSeekDataProvider()

        mPlayerGlue!!.addPlayerCallback(object : PlaybackGlue.PlayerCallback() {
            override fun onPlayCompleted(glue: PlaybackGlue) {
                super.onPlayCompleted(glue)
            }
        })
        play()
    }

    private fun releasePlayer() {
        if (mPlayer != null) {
            mPlayer!!.release()
            mPlayer = null
            mTrackSelector = null
            mPlayerGlue = null
            mPlayerAdapter = null
        }
    }

    private fun play() {
        mPlayerGlue!!.subtitle = subtitle
        mPlayerGlue!!.title = title

        val mediaSource = prepareMediaForPlaying(url)
        mPlayer!!.setMediaSource(mediaSource)
        mPlayer!!.prepare()
        mPlayerGlue!!.play()
    }

    private fun prepareMediaForPlaying(url:String): MediaSource {

        val factory: DataSource.Factory = DefaultDataSource.Factory(
            requireContext()
        )
        val uri = if (activity?.intent?.getStringExtra(SimplePlaybackActivity.SimplePlayback.SIMPLE_PLAYBACK_VIDEO_TYPE) == "file"){
            Uri.fromFile(File(url))
        }
        else{
            Uri.parse(url)
        }

        val mediaItem = MediaItem.fromUri(uri)

        if (url.endsWith("m3u8")){
            val m3u8Factory = HlsMediaSource.Factory(factory)

            return m3u8Factory.createMediaSource(mediaItem)
        }
        else{
            val pF = ProgressiveMediaSource.Factory(factory)
            return pF.createMediaSource(mediaItem)
        }

    }

    companion object {
        private const val TAG = "RecordingPlayBack"

    }
}