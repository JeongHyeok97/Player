package com.gi.hybridplayer.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.leanback.widget.Presenter
import com.gi.hybridplayer.databinding.ItemPortalBinding
import com.gi.hybridplayer.model.Portal

class PortalCardPresenter : Presenter(){

    override fun onCreateViewHolder(parent: ViewGroup?): ViewHolder {
        val inflater = LayoutInflater.from(parent?.context)
        val binding = ItemPortalBinding.inflate(inflater, parent, false)
        return PortalViewHolder(binding)

    }
    override fun onBindViewHolder(viewHolder: ViewHolder?, item: Any?) {
        val holder = viewHolder as PortalViewHolder
        holder.onBind(item)

    }
    override fun onUnbindViewHolder(viewHolder: ViewHolder?) {
    }

    class PortalViewHolder(private val binding: ItemPortalBinding)
        : ViewHolder(binding.root) {
        fun onBind(item: Any?){
            if (item is Portal) {
                binding.portal = item
            }

        }
    }
}