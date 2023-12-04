package com.gi.hybridplayer.view

import android.media.tv.TvTrackInfo
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.leanback.widget.Presenter
import com.gi.hybridplayer.databinding.ItemAvBinding

class TrackPresenter: Presenter(){

    override fun onCreateViewHolder(parent: ViewGroup?): ViewHolder {
        val inflater = LayoutInflater.from(parent?.context)
        val binding = ItemAvBinding.inflate(inflater, parent, false)
        return TrackViewHolder(binding)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder?, item: Any?) {
        if (item != null){
            if (item is TvTrackInfo && viewHolder is TrackViewHolder){
                viewHolder.onBind(item)
            }
        }
    }

    override fun onUnbindViewHolder(viewHolder: ViewHolder?) {
        if (viewHolder is TrackViewHolder){
            viewHolder.unBind()
        }
    }
    inner class TrackViewHolder(val binding: ItemAvBinding): Presenter.ViewHolder(binding.root){

        fun onBind(tvTrackInfo: TvTrackInfo){
            binding.track = tvTrackInfo
        }

        fun unBind() {

        }
    }


}
