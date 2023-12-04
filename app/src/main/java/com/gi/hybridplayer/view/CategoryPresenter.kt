package com.gi.hybridplayer.view

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.leanback.widget.Presenter
import com.gi.hybridplayer.databinding.ItemCategoryBinding
import com.gi.hybridplayer.model.Category
import com.gi.hybridplayer.model.DayWeek
import com.gi.hybridplayer.model.Vod
import com.gi.hybridplayer.model.VodCategory

class CategoryPresenter: Presenter() {

    override fun onCreateViewHolder(parent: ViewGroup?): ViewHolder {
        val inflater = LayoutInflater.from(parent?.context)
        val binding = ItemCategoryBinding.inflate(inflater, parent, false)
        return CategoryViewHolder(binding)

    }

    override fun onBindViewHolder(viewHolder: ViewHolder?, item: Any?) {
        val holder : CategoryViewHolder = viewHolder as CategoryViewHolder
        holder.onBind(item)
    }

    override fun onUnbindViewHolder(viewHolder: ViewHolder?) {

    }


    inner class CategoryViewHolder(private val binding:ItemCategoryBinding)
        : Presenter.ViewHolder(binding.root){
        fun onBind(item: Any?){
            when (item) {

                is Category -> {
                    binding.categoryString = item.title
                }
                is VodCategory -> {
                    binding.categoryString = item.categoryStr
                }
                is Vod ->{
                    binding.categoryString = item.name
                }
                is DayWeek ->{
                    binding.categoryString= item.humanDate
                }
                is String -> {
                    binding.categoryString = item
                }
            }
        }
    }


}
