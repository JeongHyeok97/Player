package com.gi.hybridplayer.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.leanback.widget.Presenter
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.gi.hybridplayer.R
import com.gi.hybridplayer.conf.CloudDBManager
import com.gi.hybridplayer.databinding.ItemSeriesBinding
import com.gi.hybridplayer.model.Episode
import com.gi.hybridplayer.model.Vod

class SeriesItemPresenter : Presenter(){

    override fun onCreateViewHolder(parent: ViewGroup?): ViewHolder {
        val inflater = LayoutInflater.from(parent?.context)
        val binding = ItemSeriesBinding.inflate(inflater, parent, false)
        return SeriesItemViewHolder(binding)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder?, item: Any?) {
        val seriesViewHolder: SeriesItemViewHolder = viewHolder as SeriesItemViewHolder
        seriesViewHolder.bind(item)
    }

    override fun onUnbindViewHolder(viewHolder: ViewHolder?) {

    }
    val mRequestOptions = RequestOptions()
        .placeholder(R.drawable.movie_default)
        .error(R.drawable.movie_default)


    inner class SeriesItemViewHolder(val binding: ItemSeriesBinding): ViewHolder(binding.root){

        fun bind(item: Any?){
            if (item is Episode){
                Glide.with(binding.seriesThumbnail.context)
                    .load(CloudDBManager.TMDB_IMAGE_SERVER_PATH + item.thumbnail)
                    .apply(mRequestOptions)
                    .into(binding.seriesThumbnail)
                binding.episode = item
            }
        }

    }

}
