package com.gi.hybridplayer

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.View.INVISIBLE
import androidx.core.content.ContextCompat
import androidx.leanback.widget.ArrayObjectAdapter
import androidx.lifecycle.ViewModelProvider
import com.gi.hybridplayer.db.repository.TvRepository
import com.gi.hybridplayer.model.Category
import com.gi.hybridplayer.view.CategoryPresenter
import com.gi.hybridplayer.view.SingleLineVerticalFragment
import com.gi.hybridplayer.viewmodel.ChannelListViewModel


class CategoryFragment(
    private val lastItvGroup : String,
    private val categories: List<Category>? =null): SingleLineVerticalFragment() {



    private lateinit var mTvRepository: TvRepository
    private val mCategoryAdapter: ArrayObjectAdapter
            = ArrayObjectAdapter(CategoryPresenter())
    private val mHandler = Handler(Looper.getMainLooper())
    private lateinit var mViewModel: ChannelListViewModel
    private var mSelectedCategory: Category? = null



    companion object{
        private const val TAG = "CategoryFragment"
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.background = ContextCompat.getDrawable(requireContext(), R.drawable.default_background)
        scrollLast(lastItvGroup)
        view.visibility = INVISIBLE
        mViewModel = ViewModelProvider(this)[ChannelListViewModel::class.java]
    }

    fun scrollLast(groupId: String){
        var lastCategoryIndex = -1
        categories?.forEach {
            if (it.id == groupId){
                lastCategoryIndex = mCategoryAdapter.indexOf(it)
            }
        }
        setSelectedPositionSmooth(lastCategoryIndex)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mTvRepository = TvRepository.getInstance(requireContext())
        adapter = mCategoryAdapter
        mCategoryAdapter.addAll(0, categories)
        setEventListener()

    }
    private fun setEventListener() {
        setOnItemViewClickedListener { _, _, _, _ ->
//            mTvActivity.hideCategory()
        }
        setOnItemViewSelectedListener { _, item, _, _ ->
            if (item is Category){

                mSelectedCategory = item
                mHandler.removeCallbacksAndMessages(null)
                mHandler.postDelayed({mViewModel.setSelectedCategory(item)
                }, 750)
            }
        }
    }

    fun getViewModel(): ChannelListViewModel {
        return mViewModel
    }

    fun getCategoryList(): MutableList<String> {
        val list = mutableListOf<String>()
        for (i in 0 until mCategoryAdapter.size()){
            val category: Category = mCategoryAdapter.get(i) as Category
            list.add(category.id!!)
        }
        return list

    }

    fun moveToTop(){
        val index = mCategoryAdapter.indexOf(mSelectedCategory)
        if (index != -1){
            mCategoryAdapter.move(index, 1)
        }
    }
    fun moveToBottom(){
        val index = mCategoryAdapter.indexOf(mSelectedCategory)
        if (index != -1){
            mCategoryAdapter.move(index, mCategoryAdapter.size()-1)
        }
    }

    fun putCategory(category: Category){
        mCategoryAdapter.add(1, category)
    }

    fun removeCategory(category: Category){
        for (i in 0 until mCategoryAdapter.size()){
            val exCategory: Category = mCategoryAdapter.get(i) as Category
            if (exCategory.id == category.id){
                mCategoryAdapter.remove(exCategory)
                break
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}