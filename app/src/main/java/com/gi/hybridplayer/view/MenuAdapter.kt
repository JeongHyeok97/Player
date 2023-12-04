package com.gi.hybridplayer.view


import android.content.Context
import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.gi.hybridplayer.R
import com.gi.hybridplayer.databinding.ItemMenuBinding
import com.gi.hybridplayer.model.Menu


class MenuAdapter(private val menuItems:List<Menu>,
                  private val onClickListener: OnItemClickEventListener? = null)
    : RecyclerView.Adapter<MenuAdapter.ViewHolder>() {

    interface OnItemClickEventListener {
        fun onItemClick(menuString: String?)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater=LayoutInflater.from(parent.context)
        val binding:ItemMenuBinding = ItemMenuBinding.inflate(inflater, parent, false)

        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = menuItems[position]
        holder.onBind(item)
    }

    override fun getItemCount(): Int {
        return menuItems.size
    }




    inner class ViewHolder(private val binding: ItemMenuBinding)
        : RecyclerView.ViewHolder(binding.root){
        var context: Context = itemView.context

        init {
            itemView.setOnFocusChangeListener { v: View?, hasFocus:Boolean ->
                var color = ContextCompat.getColor(
                    context,
                    android.R.color.white
                )
                var colorStateList = ColorStateList.valueOf(color)
                if (hasFocus) {
                    color = ContextCompat.getColor(context, R.color.phoenix)
                    colorStateList = ColorStateList.valueOf(color)
                }
                binding.menuText.setTextColor(color)
                binding.iconMenuItem.imageTintList = colorStateList
            }
            itemView.setOnClickListener{_ ->
                run {
                    onClickListener!!.onItemClick(binding.menuText.text.toString())
                }
            }
            itemView.isFocusable = true
            itemView.isFocusableInTouchMode = true
        }

        fun onBind(item:Any?){
            if (item is Menu){
                binding.menu = item
            }
        }
    }
}