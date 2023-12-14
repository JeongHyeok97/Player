package com.gi.hybridplayer.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.gi.hybridplayer.databinding.ItemAudioDialogBinding
import com.gi.hybridplayer.databinding.ItemAvBinding
import com.gi.hybridplayer.model.TrackInfo
import com.google.android.exoplayer2.Format
import okhttp3.internal.format

@Suppress("IMPLICIT_CAST_TO_ANY")
class AudioDialogAdapter(items:List<Any>): RecyclerView.Adapter<AudioDialogAdapter.ViewHolder>() {

    private val mItemList: List<Any>
    private var mItemSize: Int
    private var mListener: Listener? = null
    init {
        mItemList = items
        mItemSize = items.size
    }


    interface Listener{
        fun onClick(item: Any)
    }


    inner class ViewHolder(binding: ItemAudioDialogBinding): RecyclerView.ViewHolder(binding.root) {
        private val mBinding: ItemAudioDialogBinding
        init {
            mBinding = binding
            if (mListener != null){
                mBinding.root.setOnClickListener {
                    mListener?.onClick(mItemList[adapterPosition])
                }
            }
        }
        fun onBind(track:Any){
            if (track is Format){
                val mimeType = track.sampleMimeType
                val formatDescription = if (mimeType?.startsWith("video") == true){
                    "Video : ${TrackInfo.getCodecName(mimeType)} - ${track.width}*${track.height}"
                }
                else if (mimeType?.startsWith("audio") == true){
                    "Audio : ${TrackInfo.getCodecName(mimeType)} - ${track.language}"
                } else {
                    ""
                }
                if (formatDescription != ""){
                    mBinding.str =formatDescription
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return mItemSize
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemAudioDialogBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = mItemList[position]
        holder.onBind(item)
    }

    fun setOnListener(listener: AudioDialogAdapter.Listener) {
        this.mListener = listener
    }
}