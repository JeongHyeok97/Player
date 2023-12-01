package com.gi.hybridplayer

import android.content.Context
import android.net.Uri
import android.util.Log
import com.gi.hybridplayer.db.repository.TvRepository
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource


@Suppress("DEPRECATION")
class TvPlayer(context: Context) {
    private var player: ExoPlayer
    private val mContext: Context

    companion object{
    }

    init {
        this.mContext = context
        val factory = DefaultRenderersFactory(context)
        factory.setExtensionRendererMode(DefaultRenderersFactory.EXTENSION_RENDERER_MODE_ON)
        val parameters = DefaultTrackSelector.ParametersBuilder()
            .setPreferredAudioLanguage("mul")
            .build()
        val trackSelector = DefaultTrackSelector(context, parameters)
        val loadControl = DefaultLoadControl.Builder()
            .setBufferDurationsMs(10000,
                20000,
                DefaultLoadControl.DEFAULT_BUFFER_FOR_PLAYBACK_MS,
                DefaultLoadControl.DEFAULT_BUFFER_FOR_PLAYBACK_AFTER_REBUFFER_MS)
            .setTargetBufferBytes(DEFAULT_BUFFER_SIZE*DEFAULT_BUFFER_SIZE)
            .setPrioritizeTimeOverSizeThresholds(true)
            .build()
        val bandwidthMeter = DefaultBandwidthMeter.Builder(context).build()
        val builder = ExoPlayer.Builder(context)
        player = builder.setTrackSelector(trackSelector)
            .setRenderersFactory(factory)
            .setLoadControl(loadControl)
            .setBandwidthMeter(bandwidthMeter)
            .build()
    }
    fun getPlayerInstance():ExoPlayer{
        return player
    }

    fun prepare(){
        player.prepare()
    }
    fun setVideoUrl(url:String){
        try {
            val mediaSource = getMediaSource(url)
            player.setMediaSource(mediaSource)
            player.playWhenReady = true
            player.prepare()
        }
        catch (e:IllegalStateException){
            e.printStackTrace()
        }
    }
    private fun getMediaSource(
        streamUrl: String
    ): MediaSource {
        val factory = DefaultHttpDataSource.Factory()
        val upstreamFactory: DataSource.Factory =
            LiveDataSource.Factory(mContext, factory)
        Log.d("TAG ", streamUrl)
        val mediaItem = MediaItem.fromUri(Uri.parse(streamUrl))
        return if (streamUrl.contains(".m3u8")){
            val m3u8Factory = HlsMediaSource.Factory(upstreamFactory)
            m3u8Factory.createMediaSource(mediaItem)
        } else{
            val progressiveFactory =
                ProgressiveMediaSource.Factory(upstreamFactory)

            progressiveFactory.createMediaSource(mediaItem)
        }
    }
    fun release() {
        player.setVideoSurface(null)
        player.stop()
        player.release()
    }
    fun addListener(listener: Player.Listener?) {
        player.addListener(listener!!)
    }
    fun removeListener(listener: Player.Listener){
        player.removeListener(listener)
    }
    fun getFormats(): List<Format>? {
        val formats: MutableList<Format> = java.util.ArrayList()
        val groups = player.currentTracks.groups
        val size = player.currentTracks.groups.size
        for (i in 0 until size) {
            val trackGroup = groups[i].mediaTrackGroup
            val length = trackGroup.length
            for (j in 0 until length) {
                val format = trackGroup.getFormat(j)
                formats.add(format)
            }
        }
        return formats
    }

}