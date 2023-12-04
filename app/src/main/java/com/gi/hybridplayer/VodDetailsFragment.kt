package com.gi.hybridplayer

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.leanback.app.RowsSupportFragment
import androidx.leanback.widget.*
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.fasterxml.jackson.databind.ObjectMapper
import com.gi.hybridplayer.conf.CloudDBManager
import com.gi.hybridplayer.conf.ConnectManager
import com.gi.hybridplayer.databinding.FragmentDetailsBinding
import com.gi.hybridplayer.model.Episode
import com.gi.hybridplayer.model.Playback
import com.gi.hybridplayer.model.Portal
import com.gi.hybridplayer.model.Vod
import com.gi.hybridplayer.view.DetailCardPresenter
import com.gi.hybridplayer.viewmodel.VodDetailsViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class VodDetailsFragment : Fragment(){
    private lateinit var binding: FragmentDetailsBinding
    private lateinit var viewModel: VodDetailsViewModel
    private var mExtraRowsFragment:RowsSupportFragment? = null
    private var mSelectedVod: Vod?= null
    private var mSelectedDetail: Vod.Detail?= null
    private lateinit var mConnectManager: ConnectManager
    private lateinit var mConnectedPortal: Portal

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val rootActivity = (activity as VodDetailsActivity)
        viewModel = rootActivity.viewModel
        mConnectedPortal = rootActivity.portal
        mConnectManager = rootActivity.connectManager
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentDetailsBinding.inflate(inflater, container, false)
        binding.detailsAction1.setOnClickListener(ButtonClickListener())
        if (mExtraRowsFragment == null){
            mExtraRowsFragment = RowsSupportFragment()
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.extras_row, mExtraRowsFragment!!)
                .commit()
        }
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setVod()
        setDetail()
        setCredits()

    }

    private fun setVod(){
        viewModel.selectedVod.observe(viewLifecycleOwner){
            this@VodDetailsFragment.mSelectedVod = it
            binding.detailsTitle.text = it.name
            binding.detailsGenre.text = it.genresStr
            val action: String
                    = if (it.isSeries == "0") "Watch"
            else "Episodes"
            binding.detailsAction1.text = action
            binding.detailsDescription.text = it.description
        }
    }

    private fun setDetail(){
        viewModel.selectedDetail.observe(viewLifecycleOwner){
            mSelectedDetail = it
            val posterPath = if (it.posterPath != null){
                CloudDBManager.TMDB_IMAGE_SERVER_PATH + it.posterPath
            }
            else{
                it.kinPosterPath
            }
            val backdropPath = if (it.backdropPath != null){
                CloudDBManager.TMDB_IMAGE_SERVER_PATH + it.backdropPath
            }else{
                it.kinBackdropPath
            }
            val requestOptions = RequestOptions()
                .placeholder(R.drawable.movie_default)
                .error(R.drawable.movie_default)
            Glide.with(binding.detailsPoster.context)
                .load(posterPath)
                .apply(requestOptions)
                .into(binding.detailsPoster)
            if (backdropPath != null){

                Glide.with(binding.detailsRoot.context)
                    .asBitmap()
                    .load(backdropPath)
                    .apply(requestOptions)
                    .into(object : CustomTarget<Bitmap>() {
                        override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                            val drawable = BitmapDrawable(resources, resource)
                            binding.detailsRoot.background = drawable
                        }
                        override fun onLoadCleared(placeholder: Drawable?) {
                            // Do nothing
                        }
                    })
            }

            binding.detailsAction1.requestFocus()
        }
    }


    private fun setCredits(){
        val creditAdapter = ArrayObjectAdapter(DetailCardPresenter())
        val headerItem = HeaderItem(0, "Cast & Crew")
        val listRow = ListRow(headerItem, creditAdapter)
        val selector = ClassPresenterSelector()
        selector.addClassPresenter(ListRow::class.java, ListRowPresenter())
        val listRowAdapter = ArrayObjectAdapter(selector)
        listRowAdapter.add(listRow)
        mExtraRowsFragment!!.adapter = listRowAdapter

        viewModel.cast.observe(viewLifecycleOwner){list->
            list?.forEach {
                creditAdapter.add(it)
            }
        }
        viewModel.crew.observe(viewLifecycleOwner){list->
            list?.forEach {
                creditAdapter.add(it)
            }
        }
    }


    inner class ButtonClickListener(): View.OnClickListener{
        override fun onClick(v: View?) {
            if (v is Button) {
                if (v.text == "Watch"){
                    if (mSelectedVod != null){
                        CoroutineScope(Dispatchers.IO).launch {
                            try {
                                val type: String= mSelectedVod?.getType()!!
                                val videoUrl: String = mSelectedVod?.cmd!!

                                val realUrl:String =
                                    mConnectManager.createLink(
                                    type = type,
                                    cmd = videoUrl)!!

                                if (mSelectedDetail == null){
                                    mSelectedDetail = Vod.Detail(title = mSelectedVod?.name)
                                }


                                val playback = Playback(mSelectedVod!!,
                                    mConnectedPortal,
                                    mSelectedDetail!!,
                                    realUrl)
                                val intent = Intent(activity!!, PlaybackActivity::class.java)
                                intent.putExtra(Playback.PLAYBACK_INTENT_TAG, playback)
                                startActivity(intent)
                            }
                            catch (e:Exception){
                                e.printStackTrace()
                            }

                        }
                    }
                }
                else if (v.text == "Episodes"){
                    CoroutineScope(Dispatchers.IO).launch {
                        val episodesData = mConnectManager.getOrderedList(
                            type = Vod.TYPE_SERIES,
                            categoryID = "*",
                            movie_id = mSelectedVod?.id!!,
                            page = 1
                        )
                        val intent = Intent(activity!!, SeriesActivity::class.java)
                        val playback = Playback(vod = mSelectedVod, portal = mConnectedPortal,
                        detail = mSelectedDetail)
                        intent.putExtra(Episode.EPISODE_INTENT, episodesData)
                        intent.putExtra(Playback.PLAYBACK_INTENT_TAG, playback)

                        withContext(Dispatchers.Main){
                            startActivity(intent)
                        }
                    }
                }
            }
        }
    }

    private fun setData(episodesData: String){
        val om = ObjectMapper()
        val nodes = om.readTree(episodesData).get("js").get("data")

        nodes.forEach {
            val series: Vod = om.treeToValue(it, Vod::class.java)

        }
    }

}