package com.gi.hybridplayer.view

import android.view.LayoutInflater
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.gi.hybridplayer.databinding.ItemCategoryBinding


class CategoryAdapter(list: List<String?>): RecyclerView.Adapter<CategoryAdapter.ViewHolder>(){

    companion object{
        const val ADD_CATEGORY = "add__category"
    }
    private var mListener: ItemSelectedListener? = null

    interface ItemSelectedListener {
        fun onSelectItem(id:String)
        fun onClickItem(id:String)
    }

    inner class ViewHolder(binding: ItemCategoryBinding)
        : RecyclerView.ViewHolder(binding.root){
        private val mBinding = binding

        fun onBind(str:String){
            if (str == ADD_CATEGORY){
                mBinding.categoryText.visibility = INVISIBLE
//                mBinding.addCategoryButton.visibility = VISIBLE
                mBinding.root.setOnClickListener {
                    mListener?.onClickItem(str)
                }
            }
            else{
                mBinding.categoryString = str
                mBinding.root.setOnFocusChangeListener { v, hasFocus ->
                    if (hasFocus){
                        mListener?.onSelectItem(str)
                    }
                }
            }
        }
    }

    private val mList: List<String?>;
    init {
        mList = list
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemCategoryBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)

    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = mList[position]
        holder.onBind("$item")
    }

    override fun getItemCount(): Int {
        return mList.size
    }

    fun setOnItemSelectedListener(listener: ItemSelectedListener) {
        this.mListener = listener
    }


}