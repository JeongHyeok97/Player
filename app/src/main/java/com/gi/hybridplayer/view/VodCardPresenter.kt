package com.gi.hybridplayer.view

import android.view.LayoutInflater
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.leanback.widget.Presenter
import com.bumptech.glide.Glide
import com.gi.hybridplayer.R
import com.gi.hybridplayer.databinding.ItemCardViewBinding
import com.gi.hybridplayer.model.Vod
import java.util.*

class VodCardPresenter : Presenter(){


    override fun onCreateViewHolder(parent: ViewGroup?): ViewHolder {
        val inflater = LayoutInflater.from(parent?.context)
        val binding = ItemCardViewBinding.inflate(inflater, parent, false)
        return CardViewHolder(binding)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder?, item: Any?) {
        val cardHolder = viewHolder as CardViewHolder
        cardHolder.onBind(item)
    }

    override fun onUnbindViewHolder(viewHolder: ViewHolder?) {

    }


    inner class CardViewHolder(val binding:ItemCardViewBinding)
        :ViewHolder(binding.root){


        fun onBind(item: Any?){
            val context = binding.root.context
            if (item is Vod){
                binding.vodTitle.text = item.name
                binding.vodDesc.text = item.genresStr
                Glide.with(context)
                    .load(item.screenshotUri)
                    .error(ContextCompat.getDrawable(context, R.drawable.movie_default))
                    .placeholder(ContextCompat.getDrawable(context, R.drawable.movie_default))
                    .into(binding.vodCardMainImage)
                binding.vodAdded.text = item.added
                if (item.history != null){
                    binding.playedProgress.visibility = VISIBLE
                    binding.playedProgress.progress = item.history?.percent!!
                }
                else{
                    binding.playedProgress.visibility = GONE
                }
            }
//            else if (item is RecordedProgram){
//                binding.vodTitle.text = item.title
//                val desc = "~${convertMillisToTimeString(item.endTimeMillis!!)}"
//                binding.vodDesc.text =  desc
//                val duration = formatSecondsToHHmm(item.duration?.div(1000)?.toInt()!!)
//                binding.vodAdded.text = duration
//                Glide.with(context)
//                    .load(item.channelLogo)
//                    .error(ContextCompat.getDrawable(context, R.drawable.ic_dvr))
//                    .placeholder(ContextCompat.getDrawable(context, R.drawable.ic_dvr))
//                    .into(binding.vodCardMainImage)
//            }
        }
        fun formatSecondsToHHmm(seconds: Int): String {
            val hours = seconds / 3600
            val minutes = (seconds % 3600) / 60
            val remainingSeconds = seconds % 60

            return String.format("%02d:%02d:%02d", hours, minutes, remainingSeconds)
        }

        fun convertMillisToTimeString(millis: Long): String {
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = millis
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            val minute = calendar.get(Calendar.MINUTE)
            return String.format("%02d:%02d", hour, minute)
        }


    }

}
