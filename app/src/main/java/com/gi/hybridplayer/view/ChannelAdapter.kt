package com.gi.hybridplayer.view

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.recyclerview.widget.RecyclerView
import com.gi.hybridplayer.R
import com.gi.hybridplayer.databinding.ItemChannelListBinding
import com.gi.hybridplayer.model.Channel

class ChannelAdapter(items:MutableList<Channel>): RecyclerView.Adapter<ChannelAdapter.ViewHolder>() {

    private val mItems = items
    class ViewHolder(binding: ItemChannelListBinding)
        : RecyclerView.ViewHolder(binding.root){
            private val mBinding = binding

        fun onBind(item: Any?){
            val context = itemView.context
            val drawable = ContextCompat.getDrawable(context, R.drawable.ic_tv)
            val wrappedDrawable = DrawableCompat.wrap(
                drawable!!
            )
            DrawableCompat.setTint(
                wrappedDrawable,
                ContextCompat.getColor(context, R.color.color_theme_phoenix)
            )
            if (item is Channel){
                mBinding.channel = item
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemChannelListBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = mItems[position]
        holder.onBind(item)
    }

    override fun getItemCount(): Int {
        return mItems.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun refresh(list:List<Channel>){
        mItems.clear()
        mItems.addAll(list)
        notifyDataSetChanged()
    }
}