package com.gi.hybridplayer.callbacks

import android.media.tv.TvContentRating
import android.media.tv.TvInputManager
import android.media.tv.TvTrackInfo
import android.media.tv.TvView
import android.os.Bundle
import android.util.Log
import com.gi.hybridplayer.TvActivity

class TvCallback(activity:TvActivity) : TvView.TvInputCallback(){

    private val mTvActivity: TvActivity

    init {
        this.mTvActivity = activity
    }

    override fun onVideoAvailable(inputId: String?) {
        super.onVideoAvailable(inputId)
        Log.d("AVa", "$inputId")

    }
    //    override fun onVideoAvailable(inputId: String?) {
//        super.onVideoAvailable(inputId)
//        mBannerFragment.setState(0)
//    }
//    override fun onTracksChanged(inputId: String?, tracks: MutableList<TvTrackInfo>?) {
//        super.onTracksChanged(inputId, tracks)
//        mCurrentTracks.clear()
//        if (tracks != null){
//            mCurrentTracks.addAll(tracks)
//            tracks.forEach {
//                if (it.type == TvTrackInfo.TYPE_VIDEO){
//                    mBannerFragment.setVideoType(it)
//                }
//            }
//        }
//    }
//
//
//
//    override fun onVideoUnavailable(inputId: String?, reason: Int) {
//        super.onVideoUnavailable(inputId, reason)
//        if (reason == TvInputManager.VIDEO_UNAVAILABLE_REASON_BUFFERING || reason == TvInputManager.VIDEO_UNAVAILABLE_REASON_UNKNOWN){
//            val bundle = Bundle()
//            bundle.putBoolean("auth", true)
//            tune(false, mCurrentChannel!!, bundle)
//            if (reason == TvInputManager.VIDEO_UNAVAILABLE_REASON_BUFFERING){
//                mBannerFragment.setState(1)
//            }
//            else{
//                mBannerFragment.setState(-1)
//            }
//        }
//        else{
//            mBannerFragment.setState(-1)
//        }
//    }
//
//    override fun onContentBlocked(inputId: String?, rating: TvContentRating?) {
//        super.onContentBlocked(inputId, rating)
//        if (rating == TvContentRating.UNRATED){
//            mTvView.reset()
//
//            val auth = AuthDialog(this@TvActivity, mProfile.parentPassword!!
//            ) {
//                val bundle = Bundle()
//                bundle.putBoolean("auth", true)
//                tune(false, mCurrentChannel!!, bundle)
//            }
//
//            auth.show()
//        }
//    }
}
