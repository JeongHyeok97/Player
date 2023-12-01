package com.gi.hybridplayer

import android.content.Context
import android.media.PlaybackParams
import android.media.tv.TvTrackInfo
import android.net.Uri
import android.os.Handler
import android.os.HandlerThread
import android.os.Message
import android.os.Process
import android.util.Log
import android.view.Surface
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.analytics.AnalyticsListener
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.trackselection.TrackSelector
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultAllocator
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource

@Suppress("DEPRECATION")
class TvPlayer(
    private val context: Context) {
    private var player: ExoPlayer
    private lateinit var mDataSource: LiveDataSource


    companion object {
        private const val BUFFER_SEGMENT_SIZE = 64 * 1024
        private const val BUFFER_SEGMENT_COUNT = 256
        private const val TAG = "Player"
    }

    interface RecordingListener {
        fun onStartRecord(){

        }
    }


    init {

        val factory = DefaultRenderersFactory(context)
//        factory.setEnableAudioOffload(true)
        factory.setExtensionRendererMode(DefaultRenderersFactory.EXTENSION_RENDERER_MODE_ON)
        val parameters = DefaultTrackSelector.ParametersBuilder()
            .setPreferredAudioLanguage("mul")



            /*.setTunnelingEnabled(true)*/
            // todo : if some audio track is unavailable , change Audio Track after player initialized
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
            .setUseLazyPreparation(true)
            .setBandwidthMeter(bandwidthMeter)
            .build()
    }

    fun init(url:String){

        try {
            val mediaSource = getMediaSource(context, url)
            player.setMediaSource(mediaSource!!)


            player.playWhenReady = true

            player.prepare()

        }

        catch (e:IllegalStateException){
            e.printStackTrace()
        }



    }



    private fun getMediaSource(
        context: Context,
        streamUrl: String
    ): MediaSource {
        val factory = DefaultHttpDataSource.Factory()
        val upstreamFactory: DataSource.Factory =
            LiveDataSource.Factory(context, factory)
        var mediaSource: MediaSource? = null
        val mediaItem = MediaItem.fromUri(Uri.parse(streamUrl))
        if (streamUrl.contains(".m3u8")){
            val m3u8Factory = HlsMediaSource.Factory(upstreamFactory)
            mediaSource = m3u8Factory.createMediaSource(mediaItem)
        }
        else{
            val progressiveFactory =
                ProgressiveMediaSource.Factory(upstreamFactory)

            mediaSource = progressiveFactory.createMediaSource(mediaItem)
        }


        return mediaSource
    }



    fun getVideoFormat(): Format? {
        return player.videoFormat
    }

    fun getAudioFormat(): Format? {
        return player.audioFormat
    }

    fun selectTrack(trackInfo: TvTrackInfo){
        if (trackInfo.type == TvTrackInfo.TYPE_AUDIO){
            if (trackInfo.language != null){

                val parameters = DefaultTrackSelector.ParametersBuilder()
                    .setPreferredAudioLanguage(trackInfo.language)

//                    .setTunnelingEnabled(true)
                    .build()
                (player.trackSelector as DefaultTrackSelector).parameters = parameters
//                player.trackSelectionParameters = parameters
                player.prepare()
            }
        }
        else if (trackInfo.type == TvTrackInfo.TYPE_VIDEO){

        }

    }

    fun setSurface(surface: Surface?) {
        player.setVideoSurface(surface)
    }

    fun stop() {
        player.stop()
    }

    fun release() {
        player.setVideoSurface(null)
        player.stop()
        player.release()
    }

    fun setVolume(volume: Float) {
        player.volume = volume
    }

    fun pause() {}

    fun play() {
        player.play()

    }

    fun addListener(listener: Player.Listener?) {
        player.addListener(listener!!)
    }

    fun addAnalyticsListener(listener:AnalyticsListener){
        player.addAnalyticsListener(listener)
    }


    fun getDuration(): Long {
        return player.duration
    }

    fun getCurrentPosition(): Long {
        return player.currentPosition
    }

    fun seekTo(position: Long) {
        player.seekTo(position)
    }

    fun seekTo(mediaItemIndex: Int, position: Long) {
        player.seekTo(mediaItemIndex, position)

    }

    fun setPlaybackParams(params: PlaybackParams) {
        val parameters: PlaybackParameters = PlaybackParameters(params.speed)
        player.playbackParameters = parameters
    }

    fun setPlayWhenReady(b: Boolean) {
        player.playWhenReady = b
    }



    fun removeListener(listener: Player.Listener?) {
        player.removeListener(listener!!)
    }

    fun setTrackSelector(selector: TrackSelector?) {}

    fun prepare() {
        player.prepare()

    }

    fun getPlaybackState(): Int {
        return player.playbackState
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



    fun getSelectedFormats(type: Int): List<Format>? {
        val formats: List<Format> = getFormats()!!
        val selectedFormat: MutableList<Format> = ArrayList()
        var trackType = "null"
        if (type == TvTrackInfo.TYPE_VIDEO) {
            trackType = "video/"
        } else if (type == TvTrackInfo.TYPE_AUDIO) {
            trackType = "audio/"
        } else if (type == TvTrackInfo.TYPE_SUBTITLE) {
            trackType = "text/"
        }
        for (format in formats) {
            val mimeType = format.sampleMimeType
            if (mimeType != null) {
                if (mimeType.startsWith(trackType)) {

                    selectedFormat.add(format)
                }
            }
        }
        return selectedFormat
    }


}