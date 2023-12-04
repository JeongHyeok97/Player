package com.gi.hybridplayer

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import androidx.leanback.app.VerticalGridSupportFragment
import androidx.leanback.widget.ArrayObjectAdapter
import androidx.leanback.widget.FocusHighlight
import androidx.leanback.widget.VerticalGridPresenter
import androidx.leanback.widget.VerticalGridView
import com.gi.hybridplayer.conf.ConnectManager
import com.gi.hybridplayer.model.Portal
import com.gi.hybridplayer.model.Vod
import com.gi.hybridplayer.view.VodCardPresenter
import com.gi.hybridplayer.viewmodel.VodViewModel
import com.gi.hybridplayer.view.TextMatchDialog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class VodFragment: VerticalGridSupportFragment() {
    private lateinit var vodViewModel: VodViewModel
    private lateinit var rootActivity: VodActivity
    private lateinit var mAdapter: ArrayObjectAdapter
    private lateinit var mPortal: Portal
    private lateinit var mConnectManager: ConnectManager
    private lateinit var mHistoryRepository: HistoryRepository
    private var mBlockedId: String? = null



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        rootActivity = activity as VodActivity
        mPortal = rootActivity.portal
        mConnectManager = ConnectManager(mPortal)
        mHistoryRepository = HistoryRepository(requireContext(), mPortal.id!!)

        setupFragment()
        mAdapter = ArrayObjectAdapter(VodCardPresenter())
        adapter = mAdapter
        vodViewModel = rootActivity.getMainViewModel()
        setEventListener()
    }

    override fun onResume() {
        super.onResume()

    }

    private fun setEventListener() {
        setOnItemViewClickedListener { _, item, _, _ ->
            if (item != null){
                if (item is Vod){
                    val intent = Intent(requireActivity(), VodDetailsActivity::class.java)
                    intent.putExtra(Vod.INTENT_VOD_TAG, item)
                    intent.putExtra(Portal.PORTAL_INTENT_TAG, mPortal)
                    startActivity(intent)
                }
            }
        }
        setOnItemViewSelectedListener { itemViewHolder, item, rowViewHolder, row ->
            if (item is Vod && !rootActivity.isLoading()){
                val maxPage = if (mAdapter.size()%6 == 0){
                    mAdapter.size()/6
                }
                else{
                    mAdapter.size()/6 +1
                }
                if (mAdapter.indexOf(item)>=(maxPage-1)*6 && mAdapter.indexOf(item)<maxPage*6){
                    rootActivity.setLoading(true)
                    vodViewModel.add(id = item.categoryId!!, type = rootActivity.type, (mAdapter.size()/14)+1)
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val scope = CoroutineScope(Dispatchers.Main)

        vodViewModel.selectedVodList.observe(viewLifecycleOwner){
            pair->
            view.findViewById<VerticalGridView>(androidx.leanback.R.id.browse_grid).visibility = INVISIBLE
            scope.launch {
                val isCensored = pair.first
                val it = pair.second
                if (mAdapter.size()>0){
                    mAdapter.clear()
                }
                if (!isCensored){
                    it.forEach { vod ->
                        withContext(Dispatchers.IO){
                            mHistoryRepository.getHistory().forEach {
                                val vodId = vod.id?.toLongOrNull()
                                if (it.videoId == vodId) {
                                    it.percent = ((it.endTime)/(vod.time?.toLong()!!*600)).toInt()
                                    vod.history = it
                                }
                            }
                        }
                        mAdapter.add(vod)
                    }
                }
                else{
                    rootActivity.authentication(object : TextMatchDialog.OnSuccessListener{
                        override fun onSuccess() {
                            it.forEach { vod ->
                                CoroutineScope(Dispatchers.IO).launch{
                                    mHistoryRepository.getHistory().forEach {
                                        val vodId = vod.id?.toLongOrNull()
                                        if (it.videoId == vodId) {
                                            it.percent = ((it.endTime)/(vod.time?.toLong()!!*600)).toInt()
                                            vod.history = it
                                        }
                                    }
                                }
                                mAdapter.add(vod)
                            }
                        }
                    })
                }
                view.findViewById<VerticalGridView>(androidx.leanback.R.id.browse_grid).visibility = VISIBLE
                rootActivity.setLoading(false)
            }

        }
        vodViewModel.selectedCategory.observe(viewLifecycleOwner){
            title = it!!
        }

        vodViewModel.addedList.observe(viewLifecycleOwner){
            scope.launch {
                it?.forEach { vod->
                    withContext(Dispatchers.IO){
                        mHistoryRepository.getHistory().forEach { history->
                            val vodId = vod.id?.toLongOrNull()
                            if (history.videoId == vodId) {
                                history.percent = ((history.endTime)/(vod.time?.toLong()!!*600)).toInt()
                                vod.history = history
                            }
                        }
                    }
                    mAdapter.add(vod)
                }
                rootActivity.setLoading(false)
            }
        }
    }

    private fun setupFragment() {
        val verticalGridPresenter = VerticalGridPresenter(
            FocusHighlight.ZOOM_FACTOR_NONE,
            false
        )
        verticalGridPresenter.shadowEnabled = false
        verticalGridPresenter.numberOfColumns = 6
        gridPresenter = verticalGridPresenter
    }
}