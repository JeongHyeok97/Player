package com.gi.hybridplayer

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.View.*
import android.widget.FrameLayout
import android.widget.ProgressBar
import androidx.leanback.widget.ArrayObjectAdapter
import com.gi.hybridplayer.SimplePlaybackActivity.SimplePlayback.SIMPLE_PLAYBACK_BUNDLE
import com.gi.hybridplayer.SimplePlaybackActivity.SimplePlayback.SIMPLE_PLAYBACK_SUBTITLE
import com.gi.hybridplayer.SimplePlaybackActivity.SimplePlayback.SIMPLE_PLAYBACK_TITLE
import com.gi.hybridplayer.SimplePlaybackActivity.SimplePlayback.SIMPLE_PLAYBACK_URL
import com.gi.hybridplayer.conf.ConnectManager
import com.gi.hybridplayer.model.SingleEpg
import com.gi.hybridplayer.view.SingleEpgPresenter
import com.gi.hybridplayer.view.SingleLineVerticalFragment
import com.gi.hybridplayer.viewmodel.SingleEpgViewModel
import com.phoenix.phoenixplayer2.components.SingleEpgActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SingleEpgFragment : SingleLineVerticalFragment(){


    private lateinit var mRootActivity: SingleEpgActivity
    private lateinit var mAdapter: ArrayObjectAdapter
    private lateinit var mViewModel: SingleEpgViewModel
    private lateinit var mConnectManager: ConnectManager
    private var mLoading : ProgressBar? = null
    private val mConnectScope = CoroutineScope(Dispatchers.IO)
    private var mCurrentPage : Int? = -1


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mRootActivity = activity as SingleEpgActivity
        mConnectManager = mRootActivity.getConnectManager()
        mViewModel = mRootActivity.getViewModel()
        mAdapter = ArrayObjectAdapter(SingleEpgPresenter())
        adapter = mAdapter
        setOnItemViewClickedListener { itemViewHolder, item, rowViewHolder, row ->
            if (item is SingleEpg){
                if (item.markArchive == 1L){
                    CoroutineScope(Dispatchers.IO).launch {
                        val url = mConnectManager.createLink(type = ConnectManager.TYPE_TV_ARCHIVE,
                            cmd = item.getCmd()!!)
                        val title = item.name!!
                        Log.e("SingleEpg ", "$url")
                        val subtitle = "${item.tTime} - ${item.tTimeTo}"
                        withContext(Dispatchers.Main){
                            val intent = Intent(requireActivity(), SimplePlaybackActivity::class.java)
                            val bundle = Bundle()
                            bundle.putString(SIMPLE_PLAYBACK_TITLE, title)
                            bundle.putString(SIMPLE_PLAYBACK_SUBTITLE, subtitle)
                            bundle.putString(SIMPLE_PLAYBACK_URL, url)
                            intent.putExtra(SIMPLE_PLAYBACK_BUNDLE, bundle)
                            startActivity(intent)
                        }
                    }
                }
            }
        }
        setOnItemViewSelectedListener { itemViewHolder, item, rowViewHolder, row ->
            if (item is SingleEpg){
                if (mAdapter.indexOf(item) == mAdapter.size()-1){
                    mViewModel.nextPage()
                }

            }
        }
    }


    val handler: Handler = Handler(Looper.getMainLooper())


    suspend fun startLoading(){
        withContext(Dispatchers.Main){
            mLoading?.visibility = VISIBLE
        }
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mLoading = ProgressBar(requireContext())
        val lp: FrameLayout.LayoutParams = FrameLayout.LayoutParams(100, 100)
        lp.gravity = Gravity.CENTER
        mLoading?.layoutParams = lp
        val rootView = view as FrameLayout
        rootView.addView(mLoading)
        mViewModel.day.observe(viewLifecycleOwner){
            mLoading?.visibility = VISIBLE
            mAdapter.clear()
            handler.removeCallbacksAndMessages(null)
            val selectedDay = it
            mConnectScope.launch {
                if (it != null){
                    view.visibility = INVISIBLE
                    startLoading()
                    val data = mConnectManager.getSingleEpgData(mRootActivity.getCurrentChannelId(),
                        it.sqlDate!!,
                        1)
                    handler.postDelayed({
                        if (it.sqlDate == selectedDay.sqlDate){
                            mAdapter.addAll(0, data)
                        }
                        mLoading?.visibility = GONE
                        view.visibility = VISIBLE
                    }, 750)
                }

            }
        }
        mViewModel.page.observe(viewLifecycleOwner){
            mConnectScope.launch{
                if (it > 1){
                    startLoading()
                    val position = mAdapter.size()
                    val date: String = mViewModel.day.value?.sqlDate!!
                    val data = mConnectManager.getSingleEpgData(mRootActivity.getCurrentChannelId(),
                        date,
                        it)
                    withContext(Dispatchers.Main){
                        mAdapter.addAll(position, data)
                        mLoading?.visibility = GONE
                    }
                }
            }
        }
    }
}