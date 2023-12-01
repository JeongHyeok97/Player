package com.gi.hybridplayer

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.View
import android.view.View.INVISIBLE
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.FrameLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.leanback.widget.*
import com.gi.hybridplayer.model.Channel
import com.gi.hybridplayer.view.ChannelListPresenter
import com.gi.hybridplayer.view.SingleLineVerticalFragment


class ChannelListFragment()
    : SingleLineVerticalFragment() {


    private val mChannelsAdapter = ArrayObjectAdapter(ChannelListPresenter())

    private lateinit var mTvActivity: TvActivity

    private val mUpdateHandler: Handler = Handler(Looper.getMainLooper())
    private var isFav: Boolean = false
    private var mGridView: VerticalGridView? = null


    companion object{
        private const val TAG:String = "ChannelListFragment"
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mTvActivity = activity as TvActivity
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
                    mTvActivity.tune(item)
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
                view.visibility = INVISIBLE


            }




//            val loadingBar = ProgressBar(requireContext())
//            val lp = FrameLayout.LayoutParams(75, 75)
//            lp.gravity = Gravity.CENTER
//            loadingBar.layoutParams = lp
//            view.addView(loadingBar)
//            val noChannelsView = TextView(requireContext())
//            val nLp = FrameLayout.LayoutParams(WRAP_CONTENT, 75)
//            nLp.gravity = Gravity.CENTER
//            noChannelsView.layoutParams = nLp
//            noChannelsView.text = "No Channels Found"
//            noChannelsView.textSize = 18F
//            view.addView(noChannelsView)
//            mTvActivity.mListViewModel.tmpChannels.observe(viewLifecycleOwner){
//                noChannelsView.visibility = GONE
//                mGridView?.visibility = INVISIBLE
//                loadingBar.visibility = VISIBLE
//                mChannelsAdapter.clear()
//                mUpdateHandler.removeCallbacksAndMessages(null)
//                if (it.isNotEmpty()){
//                    mUpdateHandler.postDelayed(
//                        {mChannelsAdapter.addAll(0, it)
//                            mGridView?.visibility = VISIBLE
//                            loadingBar.visibility = GONE
//                        },
//                        750)
//                }
//                else{
//                    noChannelsView.visibility = VISIBLE
//                    loadingBar.visibility = GONE
//                }
//            }

        }


//        setChannelList()
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