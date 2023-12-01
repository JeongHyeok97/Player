package com.gi.hybridplayer.view

import android.graphics.drawable.Drawable
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.gi.hybridplayer.R

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
}