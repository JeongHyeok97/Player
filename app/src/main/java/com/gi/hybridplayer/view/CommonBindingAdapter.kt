package com.gi.hybridplayer.view

import android.graphics.drawable.Drawable
import android.util.Log
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.gi.hybridplayer.R
import com.gi.hybridplayer.TvActivity

object CommonBindingAdapters{


    @BindingAdapter("app:imageUrl")
    @JvmStatic fun loadImage(imageView: ImageView, url: String?){
        val context = imageView.context
        val drawable = ContextCompat.getDrawable(context, R.drawable.ic_tv)
        val wrappedDrawable = DrawableCompat.wrap(
            drawable!!
        )
        DrawableCompat.setTint(
            wrappedDrawable,
            ContextCompat.getColor(context, R.color.color_theme_phoenix)
        )
        Glide.with(imageView.context)
            .load(url)
            .placeholder(wrappedDrawable)
            .error(wrappedDrawable)
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .apply(RequestOptions().fitCenter())
            .into(imageView)
    }
    @BindingAdapter("app:drawable")
    @JvmStatic fun loadDrawable(imageView: ImageView, drawable: Drawable){
        val context = imageView.context
        Glide.with(imageView.context)
            .load(drawable)
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .apply(RequestOptions().fitCenter())
            .into(imageView)
    }
    @BindingAdapter("app:data")
    @JvmStatic fun setData(recyclerView: RecyclerView, list:List<Any>?){
        val listAdapter = if (list == null){
            AudioDialogAdapter(listOf())
        }
        else{
            AudioDialogAdapter(list)
        }
        recyclerView.layoutManager = LinearLayoutManager(recyclerView.context)
        recyclerView.adapter = listAdapter
    }
    @BindingAdapter("app:listener")
    @JvmStatic fun setData(recyclerView: RecyclerView, listener: AudioDialogAdapter.Listener){
        val listAdapter:AudioDialogAdapter = recyclerView.adapter as AudioDialogAdapter
        listAdapter.setOnListener(listener)
    }

}