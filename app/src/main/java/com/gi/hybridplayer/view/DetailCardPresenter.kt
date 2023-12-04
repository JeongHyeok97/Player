package com.gi.hybridplayer.view


import android.content.Context
import android.util.Log
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.leanback.widget.ImageCardView
import androidx.leanback.widget.Presenter
import com.bumptech.glide.Glide
import com.gi.hybridplayer.R
import com.gi.hybridplayer.conf.CloudDBManager
import com.gi.hybridplayer.model.Credits

class DetailCardPresenter : Presenter() {
    private var context: Context? = null


    override fun onCreateViewHolder(parent: ViewGroup): ViewHolder {
        context = parent.context
        sDefaultBackgroundColor = ContextCompat.getColor(parent.context, R.color.default_background)
        sSelectedBackgroundColor =
            ContextCompat.getColor(parent.context, R.color.selected_background)

        val cardView: ImageCardView = object : ImageCardView(parent.context) {
            override fun setSelected(selected: Boolean) {
                updateCardBackgroundColor(this, selected)
                super.setSelected(selected)
            }
        }
        cardView.setMainImageDimensions(
            context!!.resources.getDimensionPixelSize(R.dimen.detail_cardview_width),
            context!!.resources.getDimensionPixelSize(R.dimen.detail_cardview_height)
        )
        cardView.isFocusable = true
        cardView.isFocusableInTouchMode = true
        updateCardBackgroundColor(cardView, false)
        return ViewHolder(cardView)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, item: Any) {
        val cardView = viewHolder.view as ImageCardView
        if (item is Credits.Cast) {
            val cast: Credits.Cast = item
            cardView.titleText = cast.name
            Log.d(TAG , "${cast.name}")
            cardView.contentText = cast.character
            Glide.with(context!!).load(CloudDBManager.TMDB_IMAGE_SERVER_PATH + cast.profilePath)
                .error(
                    ContextCompat.getDrawable(
                        context!!,
                        R.drawable.movie_default
                    )
                )
                .into(cardView.mainImageView)
        } else if (item is Credits.Crew) {
            val crew: Credits.Crew = item as Credits.Crew
            cardView.titleText = crew.name
            cardView.contentText = crew.job
            Glide.with(context!!).load(CloudDBManager.TMDB_IMAGE_SERVER_PATH + crew.profilePath)
                .error(
                    ContextCompat.getDrawable(
                        context!!,
                        R.drawable.movie_default
                    )
                )
                .into(cardView.mainImageView)
        }
    }

    override fun onUnbindViewHolder(viewHolder: ViewHolder) {
        val cardView = viewHolder.view as ImageCardView
        // Remove references to images so that the garbage collector can free up memory
        cardView.badgeImage = null
        cardView.mainImage = null
    }

    companion object {
        private const val TAG = "CardPresenter"
        private const val CARD_WIDTH = 180
        private const val CARD_HEIGHT = 240
        private var sSelectedBackgroundColor = 0
        private var sDefaultBackgroundColor = 0
        private fun updateCardBackgroundColor(view: ImageCardView, selected: Boolean) {
            val color = if (selected) sSelectedBackgroundColor else sDefaultBackgroundColor
            // Both background colors should be set because the view"s background is temporarily visible
            // during animations.
            view.setBackgroundColor(color)
            view.setInfoAreaBackgroundColor(color)
        }
    }
}