package com.phoenix.phoenixplayer2.util

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.TypedValue
import androidx.leanback.R
import androidx.leanback.widget.PlaybackControlsRow

class MultiSoundAction @JvmOverloads constructor(
    context: Context,
    highlightColor: Int = getIconHighlightColor(context)
) :
    PlaybackControlsRow.MultiAction(R.id.lb_control_more_actions) {
    companion object {
        /**
         * Action index for closed caption is off.
         */
        @Deprecated("Use {@link #INDEX_OFF}")
        val OFF = 0

        /**
         * Action index for closed caption is on.
         */
        @Deprecated("Use {@link #INDEX_ON}")
        val ON = 1

        /**
         * Action index for closed caption is off.
         */
        const val INDEX_OFF = 0

        /**
         * Action index for closed caption is on.
         */
        const val INDEX_ON = 1
        fun getIconHighlightColor(context: Context): Int {
            val outValue = TypedValue()
            return if (context.theme.resolveAttribute(
                    R.attr.playbackControlsIconHighlightColor,
                    outValue, true
                )
            ) {
                outValue.data
            } else context.resources.getColor(R.color.lb_playback_icon_highlight_no_theme)
        }

        fun getStyledDrawable(context: Context, index: Int): Drawable? {
            val outValue = TypedValue()
            if (!context.theme.resolveAttribute(
                    R.attr.playbackControlsActionIcons, outValue, false
                )
            ) {
                return null
            }
            val array = context.theme.obtainStyledAttributes(
                outValue.data,
                R.styleable.lbPlaybackControlsActionIcons
            )
            val drawable = array.getDrawable(index)
            array.recycle()
            return drawable
        }

        fun createBitmap(bitmap: Bitmap, color: Int): Bitmap {
            val dst = bitmap.copy(bitmap.config, true)
            val canvas = Canvas(dst)
            val paint = Paint()
            paint.colorFilter = PorterDuffColorFilter(color, PorterDuff.Mode.SRC_ATOP)
            canvas.drawBitmap(bitmap, 0f, 0f, paint)
            return dst
        }
    }
    /**
     * Constructor
     * @param context Context used for loading resources.
     * @param highlightColor Color for the highlighted icon state.
     */
    /**
     * Constructor
     * @param context Context used for loading resources.
     */
    init {
        @SuppressLint("UseCompatLoadingForDrawables") val uncoloredDrawable =
            context.resources.getDrawable(
                R.drawable.lb_ic_search_mic
            ) as BitmapDrawable
        val drawables = arrayOfNulls<Drawable>(2)
        drawables[INDEX_OFF] = uncoloredDrawable
        drawables[INDEX_ON] = BitmapDrawable(
            context.resources,
            createBitmap(uncoloredDrawable.bitmap, highlightColor)
        )
        setDrawables(drawables)
        val labels = arrayOfNulls<String>(drawables.size)
        labels[INDEX_OFF] = context.getString(
            R.string.lb_playback_controls_high_quality_enable
        )
        labels[INDEX_ON] = context.getString(
            R.string.lb_playback_controls_high_quality_disable
        )
        setLabels(labels)
    }
}