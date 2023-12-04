package com.gi.hybridplayer.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.leanback.widget.Presenter
import com.gi.hybridplayer.databinding.ItemSingleEpgBinding
import com.gi.hybridplayer.model.SingleEpg

class SingleEpgPresenter : Presenter(){
    override fun onCreateViewHolder(parent: ViewGroup?): ViewHolder {
        val binding = ItemSingleEpgBinding.inflate(LayoutInflater.from(parent?.context), parent, false)
        return SingleEpgViewHolder(binding)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder?, item: Any?) {
        val epgViewHolder: SingleEpgViewHolder = viewHolder as SingleEpgViewHolder
        epgViewHolder.onBind(item)
    }

    override fun onUnbindViewHolder(viewHolder: ViewHolder?) {

    }

    inner class SingleEpgViewHolder(private val
                                    binding: ItemSingleEpgBinding): ViewHolder(binding.root){
        fun onBind(item: Any?){
            if (item is SingleEpg){
                binding.singleEpg = item
            }
        }
    }
}