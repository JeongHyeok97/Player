package com.gi.hybridplayer


import android.graphics.Color
import android.graphics.drawable.Drawable
import android.media.tv.TvTrackInfo
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.leanback.app.VideoSupportFragment
import androidx.leanback.app.VideoSupportFragmentGlueHost
import androidx.leanback.media.PlaybackGlue
import androidx.leanback.widget.ObjectAdapter
import androidx.leanback.widget.PlaybackControlsRow
import androidx.leanback.widget.PlaybackSeekDataProvider
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.gi.hybridplayer.conf.CloudDBManager
import com.gi.hybridplayer.model.History
import com.gi.hybridplayer.model.Playback
import com.gi.hybridplayer.model.Portal
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.ext.leanback.LeanbackPlayerAdapter
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.text.CueGroup
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.trackselection.TrackSelector
import com.google.android.exoplayer2.ui.CaptionStyleCompat
import com.google.android.exoplayer2.ui.SubtitleView
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource
import com.gi.hybridplayer.model.Vod
import com.phoenix.phoenixplayer2.util.MultiSoundAction
import com.phoenix.phoenixplayer2.util.VideoPlayerGlue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

@Suppress("DEPRECATION")
class PlaybackVideoFragment : VideoSupportFragment(), Player.Listener {

    private var mPlayer: Player? = null
    private var mTrackSelector: TrackSelector? = null
    private var mPlayerGlue: VideoPlayerGlue? = null
    private var mPlayerAdapter: LeanbackPlayerAdapter? = null
    private var mPlaylistActionListener: PlaylistActionListener? = null
    private var mSubtitleView: SubtitleView? = null
    private var mPortal: Portal? = null
    private var mHistoryRepository: HistoryRepository? = null
    private var mPlayback: Playback? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = super.onCreateView(inflater!!, container, savedInstanceState) as ViewGroup?
        mSubtitleView = SubtitleView(requireContext())
        val style = CaptionStyleCompat(
            Color.WHITE,
            ContextCompat.getColor(requireContext(), R.color.caption_background),
            Color.TRANSPARENT,
            CaptionStyleCompat.EDGE_TYPE_NONE,
            Color.WHITE, null
        )
        mSubtitleView?.setStyle(style)
        view!!.addView(mSubtitleView)
        return view
    }

    override fun onStart() {
        super.onStart()
        initPlayer()
    }

    override fun onStop() {
        super.onStop()
        releasePlayer()
    }

    override fun onPause() {
        super.onPause()
        if (mPlayerGlue != null && mPlayerGlue!!.isPlaying) {
            mPlayerGlue!!.pause()
        }
        releasePlayer()
    }

    override fun onDestroy() {
        super.onDestroy()
        releasePlayer()
    }

    override fun onResume() {
        super.onResume()
        if (mPlayer == null){
            initPlayer()
        }
    }

    fun initPlayer(){
        mTrackSelector = DefaultTrackSelector(requireContext())
        val builder = SimpleExoPlayer.Builder(requireContext())
        builder.setTrackSelector(mTrackSelector as TrackSelector)
        mPlayer = builder.build()
        mPlayer?.addListener(this)
        mPlayerAdapter = LeanbackPlayerAdapter(requireActivity(),
            mPlayer as Player, 18)
        mPlaylistActionListener = PlaylistActionListener()
        mPlayerGlue = VideoPlayerGlue(activity, mPlayerAdapter, mPlaylistActionListener!!)
        mPlayerGlue!!.host = VideoSupportFragmentGlueHost(this)
        mPlayerGlue!!.playWhenPrepared()
        mPlayerGlue!!.isControlsOverlayAutoHideEnabled = true
        mPlayerGlue!!.seekProvider = PlaybackSeekDataProvider()
        mPlayerGlue!!.addPlayerCallback(object : PlaybackGlue.PlayerCallback() {
            override fun onPlayCompleted(glue: PlaybackGlue) {
                super.onPlayCompleted(glue)
            }
        })
        val playback = (activity as PlaybackActivity).playback
        mPlayback = playback
        mPortal = playback.portal
        mHistoryRepository = HistoryRepository(requireContext(), mPortal?.id!!)
        play(playback)
    }
    private fun play(playback: Playback) {
        val vod = playback.vod
        val detail = playback.detail
        val url = playback.url
        val id = vod?.id
        mPlayerGlue!!.title = detail?.title
        mPlayerGlue!!.subtitle = detail?.overView
        val posterUrl = detail?.posterPath
        Glide.with(requireContext())
            .load(CloudDBManager.TMDB_IMAGE_SERVER_PATH + posterUrl)
            .error(ContextCompat.getDrawable(requireContext(), R.drawable.ic_vod))
            .into(object : SimpleTarget<Drawable?>() {
                override fun onResourceReady(
                    resource: Drawable,
                    transition: Transition<in Drawable?>?
                ) {
                    mPlayerGlue!!.art = resource
                }
            })
        val factory = DefaultHttpDataSource.Factory()
        val mediaSource: MediaSource = ProgressiveMediaSource.Factory(factory)
            .createMediaSource(MediaItem.fromUri(url!!))

        (mPlayer as SimpleExoPlayer).prepare(mediaSource)
//        val position: Long = getWatched(id)
//        mPlayer.seekTo(position)
        if (mPlayback?.vod?.history != null){
            mPlayer?.seekTo(mPlayback?.vod?.history!!.endTime)
        }
        mPlayerGlue!!.play()

    }
    private fun releasePlayer(){

        if (mPlayer != null) {
            setNotEnded(mPlayer?.currentPosition!!)
            mPlayer?.release()
            mPlayer = null
            mTrackSelector = null
            mPlayerGlue = null
            mPlayerAdapter = null
            mPlaylistActionListener = null

        }
    }

    fun setNotEnded(endTime: Long){
        if (mPortal != null && mHistoryRepository != null){
            val position = mPlayer?.currentPosition!!
            val videoId = mPlayback?.vod?.id?.toLongOrNull()
            if (videoId != null){
                CoroutineScope(Dispatchers.IO).launch {
                    val history = History(portalId = mPortal?.id!!, videoId = videoId!!,
                        position)
                    mHistoryRepository?.insert(history)
                }
            }

        }

    }

    fun selectTrack(track: TvTrackInfo?) {
        val parameter = mTrackSelector?.parameters
        val param =
            if (track?.type == TvTrackInfo.TYPE_AUDIO){
                parameter?.buildUpon()?.setPreferredAudioLanguage(track.language)?.build()!!
            }
        else{
                parameter?.buildUpon()?.setPreferredTextLanguage(track?.language)?.build()!!
            }
        (mTrackSelector as DefaultTrackSelector).setParameters(param)
        mPlayer?.prepare()
        mPlayer?.addListener(object : Player.Listener{
            override fun onCues(cueGroup: CueGroup) {
                super.onCues(cueGroup)
                mSubtitleView?.setCues(cueGroup.cues)
            }
        })
    }

    inner class PlaylistActionListener : VideoPlayerGlue.OnActionClickedListener{


        override fun onCaption(
            closedCaptioningAction: PlaybackControlsRow.ClosedCaptioningAction,
            secondaryActionsAdapter: ObjectAdapter?
        ) {
            select(TvTrackInfo.TYPE_SUBTITLE)
        }
        override fun onSoundTrack(
            soundAction: MultiSoundAction?,
            secondaryActionsAdapter: ObjectAdapter?
        ) {
            select(TvTrackInfo.TYPE_AUDIO)
        }
        override fun onPrevious() {
        }
        override fun onNext() {

        }


        @RequiresApi(Build.VERSION_CODES.M)
        fun select(type: Int){
            val trackSelector = mTrackSelector as DefaultTrackSelector
            val info = trackSelector.currentMappedTrackInfo
            if (info != null) {
                val rendererCount = info.rendererCount
                val langList: MutableList<TvTrackInfo?> = ArrayList()
                for (i in 0 until rendererCount) {
                    val rendererType = info.getRendererType(i)
                    val trackType = when (type) {
                        TvTrackInfo.TYPE_SUBTITLE -> {
                            C.TRACK_TYPE_TEXT
                        }
                        TvTrackInfo.TYPE_AUDIO -> {
                            C.TRACK_TYPE_AUDIO
                        }
                        else -> {
                            C.TRACK_TYPE_VIDEO
                        }
                    }

                    if (rendererType == trackType) {
                        val trackGroupArray = info.getTrackGroups(i)

                        for (j in 0 until trackGroupArray.length) {
                            val trackGroup = trackGroupArray[j]

                            for (k in 0 until trackGroup.length) {
                                val builder = TvTrackInfo.Builder(type, "$j-$k")
                                val format = trackGroup.getFormat(k)

                                val descr = Locale(format.language!!).displayLanguage
                                builder.setLanguage(format.language!!)
                                builder.setDescription(descr)
                                langList.add(builder.build())
                            }
                        }
                    }
                }
                val dialog = PlaybackDialog(langList)
                if (langList.size>0){
                    if (activity?.supportFragmentManager?.findFragmentById(R.id.playback_container)
                                !is PlaybackDialog
                    ){
                        activity?.supportFragmentManager?.beginTransaction()!!
                            .replace(R.id.playback_dialog_container, dialog)
                            .addToBackStack(null)
                            .commit()
                    }
                }
            }
        }

    }
}