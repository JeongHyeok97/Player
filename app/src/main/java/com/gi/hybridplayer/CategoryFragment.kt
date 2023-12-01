package com.gi.hybridplayer

import android.os.Bundle
import android.view.View
import android.view.View.INVISIBLE
import androidx.core.content.ContextCompat
import androidx.leanback.widget.ArrayObjectAdapter
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.gi.hybridplayer.db.repository.TvRepository
import com.gi.hybridplayer.model.Category
import com.gi.hybridplayer.model.Channel
import com.gi.hybridplayer.view.CategoryPresenter
import com.gi.hybridplayer.view.SingleLineVerticalFragment
import com.gi.hybridplayer.viewmodel.ChannelListViewModel


class CategoryFragment(
    private val lastItvGroup : String,
    private val categories: List<Category>? =null): SingleLineVerticalFragment() {


    private lateinit var mTvActivity: TvActivity
    private lateinit var mTvRepository: TvRepository
    private val mCategoryAdapter: ArrayObjectAdapter
            = ArrayObjectAdapter(CategoryPresenter())
    private lateinit var mViewModel: ChannelListViewModel



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
        mTvActivity = activity as TvActivity

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
                mViewModel.setSelectedCategory(item)
            }
        }
    }

    fun getViewModel(): ChannelListViewModel {
        return mViewModel
    }



}