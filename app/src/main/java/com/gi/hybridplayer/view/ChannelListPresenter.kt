package com.gi.hybridplayer.view

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.leanback.widget.Presenter
import com.gi.hybridplayer.R
import com.gi.hybridplayer.databinding.ItemChannelListBinding
import com.gi.hybridplayer.model.Channel

class ChannelListPresenter: Presenter() {



    override fun onCreateViewHolder(parent: ViewGroup?): ViewHolder {
        val inflater = LayoutInflater.from(parent?.context)
        val binding = ItemChannelListBinding.inflate(inflater, parent, false)
        return ChannelListViewHolder(binding)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder?, item: Any?) {
        val holder : ChannelListViewHolder = viewHolder as ChannelListViewHolder
        holder.onBind(item)
    }

    override fun onUnbindViewHolder(viewHolder: ViewHolder?) {

    }
    inner class ChannelListViewHolder(
        private val binding: ItemChannelListBinding
    )
        :Presenter.ViewHolder(binding.root) {
        private val context: Context = binding.root.context

        fun onBind(item: Any?){
            val drawable = ContextCompat.getDrawable(context, R.drawable.ic_tv)
            val wrappedDrawable = DrawableCompat.wrap(
                drawable!!
            )
            DrawableCompat.setTint(
                wrappedDrawable,
                ContextCompat.getColor(context, R.color.color_theme_phoenix)
            )
            if (item is Channel){
                binding.channel = item
            }
        }
    }
}