package com.gi.hybridplayer.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.recyclerview.widget.RecyclerView
import com.gi.hybridplayer.R
import com.gi.hybridplayer.databinding.ItemChannelListBinding
import com.gi.hybridplayer.model.Channel

class SearchResultAdapter(list: List<Channel>): RecyclerView.Adapter<SearchResultAdapter.ViewHolder>() {

    companion object{
        const val SORT_BY_NAME = 1
        const val SORT_BY_NUMBER = 2
    }

    private var mItemList = list
    private var mListener: Listener?= null

    interface Listener{
        fun onClick(item: Any)
    }



    inner class ViewHolder(binding:ItemChannelListBinding) : RecyclerView.ViewHolder(binding.root){
        private val mBinding: ItemChannelListBinding
        init {
            mBinding = binding


        }
        fun bind(channel: Channel){
            val drawable = ContextCompat.getDrawable(itemView.context, R.drawable.ic_tv)
            val wrappedDrawable = DrawableCompat.wrap(
                drawable!!
            )
            DrawableCompat.setTint(
                wrappedDrawable,
                ContextCompat.getColor(itemView.context, R.color.color_theme_phoenix)
            )
            itemView.setOnClickListener {
                mListener?.onClick(channel)
            }
            mBinding.channel = channel
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent?.context)
        val binding = ItemChannelListBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(mItemList[position])
    }

    override fun getItemCount(): Int {
        return mItemList.size
    }

    fun setListener(listener: Listener) {
        this.mListener = listener
    }

    fun sort(type: Int, keyword:String = ""){
        if (type == SORT_BY_NAME){
            mItemList = mItemList.sortedBy { item->
                val distance = getSimilarity(keyword, item.displayName!!)
                distance
            }
        }
        else if (type == SORT_BY_NUMBER){
            mItemList = mItemList.sortedBy {
                it.displayNumber?.toInt()
            }
        }
        notifyItemRangeChanged(0, mItemList.size)
    }


    private fun getSimilarity(keyword: String, target: String): Int {
        val dp = Array(keyword.length + 1) { IntArray(target.length + 1) }

        for (i in 0..keyword.length) {
            for (j in 0..target.length) {
                when {
                    i == 0 -> dp[i][j] = j
                    j == 0 -> dp[i][j] = i
                    else -> {
                        dp[i][j] = if (keyword[i - 1] == target[j - 1]) {
                            dp[i - 1][j - 1]
                        } else {
                            1 + minOf(dp[i][j - 1], dp[i - 1][j], dp[i - 1][j - 1])
                        }
                    }
                }
            }
        }

        return dp[keyword.length][target.length]
    }
}