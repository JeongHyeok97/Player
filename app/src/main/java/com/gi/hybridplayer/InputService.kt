package com.gi.hybridplayer

import android.content.Context
import android.media.tv.*
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Surface
import android.widget.Toast
import com.gi.hybridplayer.conf.ConnectManager
import com.gi.hybridplayer.conf.DeviceManager
import com.google.android.exoplayer2.ExoPlaybackException
import com.google.android.exoplayer2.Format
import com.google.android.exoplayer2.PlaybackException
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.analytics.AnalyticsListener

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

class InputService : TvInputService(){

    private lateinit var mInputSession: InputSession
    private var mChannelUrl: String? = null

    companion object{
        private const val TAG = "InputService"
    }

    override fun onCreateSession(inputId: String): Session {
        val session = InputSession(this)
        mInputSession = session
        session.setOverlayViewEnabled(true)
        return session
    }

    override fun onCreateRecordingSession(inputId: String): RecordingSession? {
        return RecordSession(this, inputId)
    }

    inner class InputSession(context: Context?) :
        TvInputService.Session(context), Player.Listener, AnalyticsListener{
        private var mPlayer: TvPlayer? = null
        private var mSurface:Surface? = null
        private val handler:Handler = Handler(Looper.getMainLooper())
        private var mConnectManager: ConnectManager? = null
        init {
            initPlayer()
        }


        fun initPlayer(){
            mPlayer = TvPlayer(this@InputService)
            mPlayer?.addListener(this)
            mPlayer?.addAnalyticsListener(this)
        }



        override fun onRelease() {
            mPlayer?.release()
            mPlayer?.removeListener(this)
        }

        override fun onSetSurface(surface: Surface?): Boolean {
            if (mPlayer == null){
                return false
            }
            if (surface == null){
                mPlayer?.stop()
                return true
            }
            setTvSurface(surface)
            return true
        }

        override fun onSetStreamVolume(volume: Float) {
            if (mPlayer != null){
                mPlayer?.setVolume(volume)
            }
        }
        private fun releasePlayer(){
            if (mPlayer != null) {
                mPlayer!!.setSurface(null)
                mPlayer!!.stop()
                mPlayer!!.release()
                mPlayer = null
            }
        }



        override fun onPlayerError(error: PlaybackException) {
            super<Player.Listener>.onPlayerError(error)

            if (error is ExoPlaybackException){

                releasePlayer()
                initPlayer()

                setTvSurface(mSurface)
                mPlayer?.init(mChannelUrl!!)
                mPlayer?.prepare()
                notifyVideoUnavailable(TvInputManager.VIDEO_UNAVAILABLE_REASON_UNKNOWN)
            }



        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            super<Player.Listener>.onPlaybackStateChanged(playbackState)
            if (playbackState == Player.STATE_READY) {
                notifyTracksChanged(getTracks())
                for (trackInfo in getTracks()!!) {
                    notifyTrackSelected(trackInfo.type, trackInfo.id)
                }
                notifyVideoAvailable()


            }
            if (playbackState == Player.STATE_BUFFERING){
                notifyVideoUnavailable(TvInputManager.VIDEO_UNAVAILABLE_REASON_BUFFERING)
            }
            if (playbackState == Player.STATE_ENDED){
                notifyVideoUnavailable(TvInputManager.VIDEO_UNAVAILABLE_REASON_UNKNOWN)
            }
            if (playbackState == Player.STATE_IDLE){

            }

}


        override fun onTune(channelUri: Uri?, params: Bundle?): Boolean {
            playChannel(channelUri!!, params)
            return true
        }

        override fun onTune(channelUri: Uri?): Boolean {
            notifyVideoUnavailable(TvInputManager.VIDEO_UNAVAILABLE_REASON_TUNING)
//            playChannel(channelUri!!)
            return true
        }

        private fun playChannel(channelUri: Uri, bundle: Bundle? = null){
//            CoroutineScope(Dispatchers.IO).launch {
//                val channel = Repository.getChannel(this@InputService, channelUri)
//                if (channel != null){
//                    if (channel.isLocked()){
//                        if (bundle != null){
//                            if (bundle.getBoolean("auth", false)){
//                                val url = channel.getVideoUrl()
//                                play(url, bundle)
//                            }
//                            else{
//                                notifyContentBlocked(TvContentRating.UNRATED)
//                            }
//                        }
//                    }
//                    else{
//                        val url = channel.getVideoUrl()
//                        play(url, bundle)
//                    }
//                }
//            }
        }


        private fun play(url: String, bundle: Bundle? = null){
//            var channelUrl = ""
//            if (url.contains("localhost") && bundle != null){
//                if (mConnectManager == null){
//                    val portal = Portal(serverUrl = bundle.getString("server")!!,
//                        macAddress = bundle.getString("mac")!!,
//                        token = bundle.getString("token")!!,
//                        url = bundle.getString("url")!!
//                    )
//                    mConnectManager = ConnectManager(portal)
//
//                }
//                channelUrl = mConnectManager!!.createLink(
//                    type = ConnectManager.TYPE_ITV,
//                    cmd = url,
//                )!!
//            }
//            else{
//                channelUrl = url.substring(url.indexOf("http"), url.length)
//            }
//            mChannelUrl = channelUrl
//
//
//            handler.post {
//
//                mPlayer?.init(mChannelUrl!!)
//            }
        }

        override fun onSetCaptionEnabled(enabled: Boolean) {
            if (mPlayer != null){

            }
        }

        override fun onSelectTrack(type: Int, trackId: String?): Boolean {
            getTracks()?.forEach {
                if (it.type == type && it.id == trackId){
                    mPlayer?.selectTrack(trackInfo = it)
                }
            }
            return super.onSelectTrack(type, trackId)

        }

        private fun setTvSurface(surface: Surface?){
            if (mPlayer != null){
                mPlayer?.setSurface(surface)
                mSurface = surface
            }
        }











        private fun getTrackId(trackType: Int, trackIndex: Int): String? {
            return "$trackType-$trackIndex"
        }
        private fun getTracks(): List<TvTrackInfo>? {

            val trackInfos: MutableList<TvTrackInfo> = ArrayList()
//            val trackTypes = intArrayOf(
//                TvTrackInfo.TYPE_AUDIO,
//                TvTrackInfo.TYPE_VIDEO,
//                TvTrackInfo.TYPE_SUBTITLE
//            )
//            for (trackType in trackTypes) {
//                val formats: List<Format> = mPlayer?.getSelectedFormats(trackType)!!
//                for (format in formats) {
//                    val trackId: String = getTrackId(trackType, formats.indexOf(format))!!
//                    val builder = TvTrackInfo.Builder(trackType, trackId)
//                    if (trackType == TvTrackInfo.TYPE_AUDIO) {
//                        builder.setAudioChannelCount(format.channelCount)
//                        builder.setAudioSampleRate(format.sampleRate)
//
//                        builder.setDescription("audio/${TrackInfo.getCodecName(format.sampleMimeType.toString())}")
//                        if (format.language != null && format.language != "und") {
//                            builder.setLanguage(format.language!!)
//                        }
//                    } else if (trackType == TvTrackInfo.TYPE_VIDEO) {
//                        if (format.width != Format.NO_VALUE) {
//                            builder.setVideoWidth(format.width)
//                        }
//                        if (format.height != Format.NO_VALUE) {
//                            builder.setVideoHeight(format.height)
//                        }
//
//
//                        builder.setDescription("video/${TrackInfo.getCodecName(format.sampleMimeType.toString())}")
//                    } else if (trackType == TvTrackInfo.TYPE_SUBTITLE) {
//                        if (format.language != null && format.language != "und") {
//                            builder.setLanguage(format.language!!)
//                        }
//                    }
//                    trackInfos.add(builder.build())
//                }
//            }
            return trackInfos
        }

        override fun onAudioCodecError(
            eventTime: AnalyticsListener.EventTime,
            audioCodecError: Exception
        ) {
            super.onAudioCodecError(eventTime, audioCodecError)
            audioCodecError.printStackTrace()
            notifyVideoUnavailable(TvInputManager.VIDEO_UNAVAILABLE_REASON_AUDIO_ONLY)
        }

        override fun onPositionDiscontinuity(
            oldPosition: Player.PositionInfo,
            newPosition: Player.PositionInfo,
            reason: Int
        ) {
            super<Player.Listener>.onPositionDiscontinuity(oldPosition, newPosition, reason)
            if (reason == Player.DISCONTINUITY_REASON_INTERNAL){
                mPlayer?.seekTo(newPosition.mediaItemIndex, newPosition.positionMs)
            }
        }
        fun getTvPlayer():TvPlayer?{
            return mPlayer
        }

    }


    inner class RecordSession(private val mContext: Context,
                              private val inputId: String)
        : TvInputService.RecordingSession(mContext){

        private var mStartTime:Long? = -1
//        private var mRecordChannel:Channel? = null
        private var mFile:File? = null
        private var mVideoType: String? = "mp4"



        override fun onTune(channelUri: Uri?) {
            notifyTuned(channelUri)
        }

        override fun onStartRecording(programUri: Uri?, params: Bundle) {
            startRecording(programUri, params)
        }

        override fun onStartRecording(channelUri: Uri?) {
            startRecording(channelUri)

        }
        private fun startRecording(channelUri: Uri?, params: Bundle? = null){
//            val usbPath = DeviceManager.getUsbStorage(mContext)
//            if (usbPath == null){
//                notifyError(TvInputManager.RECORDING_ERROR_UNKNOWN)
//                notifyRecordingStopped(null)
//            }
//            else{
//                val startTime = System.currentTimeMillis()
//                if (params == null){
//                    val channel = Repository.getChannel(mContext, channelUri!!)
//                    mRecordChannel = channel
//
//
//                    val id = channel?.id!!
//                    val filePath = "${id}_${startTime}.$mVideoType"
//                    mFile = File(usbPath, filePath)
//
//
//
//                    LiveDataSource.setRecording(file = mFile)
//                    mStartTime = System.currentTimeMillis()
//                    Toast.makeText(mContext, "Recording of channel ${channel.displayName} started",
//                        Toast.LENGTH_SHORT).show()
//                }
//            }
        }

        override fun onStopRecording() {
//            if (mRecordChannel != null){
////
////
////                try {
////
////
////
////
////                    val recordedProgram = RecordedProgram(
////                        inputId = inputId,
////                        title = mRecordChannel?.displayName,
////                        startTimeMillis = mStartTime,
////                        endTimeMillis = System.currentTimeMillis(),
////                        channelLogo = mRecordChannel?.getLogo(),
////                        duration = System.currentTimeMillis()-mStartTime!!,
////                        dataUri = Uri.fromFile(mFile).toString(),
////                    )
////
////
////
////                    val recordedProgramUri = mContext.contentResolver
////                        .insert(TvContract.RecordedPrograms.CONTENT_URI,
////                            recordedProgram.toContentValues()
////                        )
////
////
////                    notifyRecordingStopped(recordedProgramUri)
////                    LiveDataSource.setRecording(record = false)
////
////
////
////                }
////                catch (e: Exception){
////                    Toast.makeText(mContext, "Recording failed to complete properly.", Toast.LENGTH_SHORT).show()
////                    e.printStackTrace()
////                }
//
//            }


        }


        override fun onRelease() {


        }
    }
}