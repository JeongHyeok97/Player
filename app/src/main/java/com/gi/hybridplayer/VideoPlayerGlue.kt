package com.phoenix.phoenixplayer2.util

import android.content.Context
import androidx.leanback.media.PlaybackTransportControlGlue
import androidx.leanback.widget.Action
import androidx.leanback.widget.ArrayObjectAdapter
import androidx.leanback.widget.ObjectAdapter
import androidx.leanback.widget.PlaybackControlsRow.*
import com.google.android.exoplayer2.ext.leanback.LeanbackPlayerAdapter
import java.util.concurrent.TimeUnit

class VideoPlayerGlue(
    context: Context?,
    playerAdapter: LeanbackPlayerAdapter?,
    private val mActionListener: OnActionClickedListener
) : PlaybackTransportControlGlue<LeanbackPlayerAdapter?>(context, playerAdapter) {
    /** Listens for when skip to next and previous actions have been dispatched.  */
    interface OnActionClickedListener {
        fun onCaption(
            closedCaptioningAction: ClosedCaptioningAction,
            secondaryActionsAdapter: ObjectAdapter?
        )

        fun onSoundTrack(
            soundAction: MultiSoundAction?,
            secondaryActionsAdapter: ObjectAdapter?
        )

        /** Skip to the previous item in the queue.  */
        fun onPrevious()

        /** Skip to the next item in the queue.  */
        fun onNext()
    }

    private val mThumbsUpAction: ThumbsUpAction
    private val mThumbsDownAction: ThumbsDownAction
    private val mSkipPreviousAction: SkipPreviousAction
    private val mSkipNextAction: SkipNextAction
    private val mFastForwardAction: FastForwardAction
    private val mRewindAction: RewindAction
    private val mCaptionAction: ClosedCaptioningAction
    private val mSelectSoundTrack: MultiSoundAction
    override fun onCreatePrimaryActions(adapter: ArrayObjectAdapter) {
        // Order matters, super.onCreatePrimaryActions() will create the play / pause action.
        // Will display as follows:
        // play/pause, previous, rewind, fast forward, next
        //   > /||      |<        <<        >>         >|
        super.onCreatePrimaryActions(adapter)
        adapter.add(mRewindAction)
        adapter.add(mFastForwardAction)
        onSkipEpisode(adapter)
    }

    override fun onCreateSecondaryActions(adapter: ArrayObjectAdapter) {
        super.onCreateSecondaryActions(adapter)
        adapter.add(mCaptionAction)
        adapter.add(mSelectSoundTrack)
    }

    private fun onSkipEpisode(adapter: ArrayObjectAdapter) {
        adapter.add(mSkipNextAction)
        adapter.add(mSkipPreviousAction)
    }

    override fun onActionClicked(action: Action) {
        if (shouldDispatchAction(action)) {
            dispatchAction(action)
            return
        }
        // Super class handles play/pause and delegates to abstract methods next()/previous().
        super.onActionClicked(action)
    }

    // Should dispatch actions that the super class does not supply callbacks for.
    private fun shouldDispatchAction(action: Action): Boolean {
        return action === mRewindAction || action === mFastForwardAction || action === mSelectSoundTrack || action === mCaptionAction
    }

    private fun dispatchAction(action: Action) {
        if (action === mRewindAction) {
            rewind()
        } else if (action === mFastForwardAction) {
            fastForward()
        } else if (action === mCaptionAction) {
            mActionListener.onCaption(mCaptionAction, controlsRow.secondaryActionsAdapter)
        } else if (action === mSelectSoundTrack) {
            mActionListener.onSoundTrack(mSelectSoundTrack, controlsRow.secondaryActionsAdapter)
        }
    }

    override fun next() {
        if (duration > -1) {
            var newPosition = currentPosition + ONE_MIN
            newPosition = Math.min(newPosition, duration)
            playerAdapter!!.seekTo(newPosition)
        }
    }

    override fun previous() {
        var newPosition = currentPosition - ONE_MIN
        newPosition = if (newPosition < 0) 0 else newPosition
        playerAdapter!!.seekTo(newPosition)
    }

    /** Skips backwards 10 seconds.  */
    fun rewind() {
        var newPosition = currentPosition - TEN_SECONDS
        newPosition = if (newPosition < 0) 0 else newPosition
        playerAdapter!!.seekTo(newPosition)
    }

    /** Skips forward 10 seconds.  */
    fun fastForward() {
        if (duration > -1) {
            var newPosition = currentPosition + TEN_SECONDS
            newPosition = Math.min(newPosition, duration)
            playerAdapter!!.seekTo(newPosition)
        }
    }

    companion object {
        private val TEN_SECONDS = TimeUnit.SECONDS.toMillis(10)
        private val ONE_MIN = TimeUnit.SECONDS.toMillis(60)
        private const val TAG = "VodPlayerGlue"
        private const val SOUND_TRACK_ID = 123456
    }

    init {
        mSkipPreviousAction = SkipPreviousAction(context)
        mSkipNextAction = SkipNextAction(context)
        mFastForwardAction = FastForwardAction(context)
        mRewindAction = RewindAction(context)
        mThumbsUpAction = ThumbsUpAction(context)
        mThumbsUpAction.index = ThumbsUpAction.INDEX_OUTLINE
        mThumbsDownAction = ThumbsDownAction(context)
        mThumbsDownAction.index = ThumbsDownAction.INDEX_OUTLINE
        mSelectSoundTrack = MultiSoundAction(context!!)
        mCaptionAction = ClosedCaptioningAction(context)
    }
}