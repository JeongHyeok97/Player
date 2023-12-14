package com.gi.hybridplayer

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.View
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.FrameLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.leanback.widget.*
import com.gi.hybridplayer.db.repository.TvRepository
import com.gi.hybridplayer.model.Category
import com.gi.hybridplayer.model.Channel
import com.gi.hybridplayer.view.ChannelListPresenter
import com.gi.hybridplayer.view.SingleLineVerticalFragment
import com.gi.hybridplayer.view.TextMatchDialog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class ChannelListFragment(private val lastCategory: Category)
    : SingleLineVerticalFragment() {


    private val mChannelsAdapter = ArrayObjectAdapter(ChannelListPresenter())
    private val viewUpdateHandler= Handler(Looper.getMainLooper())
    private lateinit var mTvActivity: TvActivity

    private val mScope = CoroutineScope(Dispatchers.Default)
    private var isFav: Boolean = false
    private var mGridView: VerticalGridView? = null
    private lateinit var mRepository: TvRepository
    private var mCurrentChannels = listOf<Channel>()
    private var mSelectedCategory: Category? = null


    companion object{
        private const val TAG:String = "ChannelListFragment"
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mTvActivity = activity as TvActivity
        mRepository = TvRepository.getInstance(requireContext())
        onItemViewClickedListener = ChannelItemClickListener()
        setOnItemViewSelectedListener(ChannelItemSelectedListener())
        adapter = mChannelsAdapter


    }

    inner class ChannelItemSelectedListener : OnItemViewSelectedListener {
        override fun onItemSelected(
            itemViewHolder: Presenter.ViewHolder?,
            item: Any?,
            rowViewHolder: RowPresenter.ViewHolder?,
            row: Row?
        ) {
        }
    }

    inner class ChannelItemClickListener : OnItemViewClickedListener {
        override fun onItemClicked(
            itemViewHolder: Presenter.ViewHolder?,
            item: Any?,
            rowViewHolder: RowPresenter.ViewHolder?,
            row: Row?
        ) {
            if (item != null){
                if (item is Channel){
                    mTvActivity.tune(item, mSelectedCategory, mCurrentChannels)
                    view?.visibility = INVISIBLE
                }
            }
        }
    }




    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (view is FrameLayout){
            view.background = ContextCompat.getDrawable(requireContext(), R.drawable.default_background)
            mGridView = view.findViewById(androidx.leanback.R.id.browse_grid)
            view.visibility = INVISIBLE
            val categoryFragment = mTvActivity.supportFragmentManager
                .findFragmentById(R.id.category_container) as CategoryFragment
            val viewModel = categoryFragment.getViewModel()
            viewModel.selectedCategory.observe(viewLifecycleOwner){
                mSelectedCategory = it
                mGridView?.visibility = INVISIBLE
                mChannelsAdapter.clear()
                viewUpdateHandler.removeCallbacksAndMessages(null)
                mScope.launch {
                    val list = if (it.id != Category.HISTORY_ID){
                        mRepository.findListByChannel(it.id!!)
                    }
                    else{
                        mTvActivity.getHistory()
                    }
                    mCurrentChannels = list
                    viewUpdateHandler.post{
                        if (it.censored == true){
                            mTvActivity.authentication(object :TextMatchDialog.OnSuccessListener{
                                override fun onSuccess() {
                                    mChannelsAdapter.addAll(0, list)
                                }
                            })
                        }
                        else{
                            mChannelsAdapter.addAll(0, list)
                        }
                    }
                    viewUpdateHandler.postDelayed(
                        {mGridView?.visibility = VISIBLE},
                        300)
                }
            }
            viewModel.setSelectedCategory(lastCategory)
        }

    }


    fun requestFocus(currentChannel: Channel?) {
        if (mGridView != null){
            mGridView?.requestFocus()
        }
        for (i in 0 until mChannelsAdapter.size()){
            val item = mChannelsAdapter.get(i) as Channel
            if (item.originalNetworkId == currentChannel?.originalNetworkId){
                val index = mChannelsAdapter.indexOf(item)
                setSelectedPositionSmooth(index)
            }
        }

    }



}