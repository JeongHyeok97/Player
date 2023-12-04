package com.gi.hybridplayer

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.leanback.widget.VerticalGridView
import com.gi.hybridplayer.databinding.FragmentMenuBinding
import com.gi.hybridplayer.model.Menu
import com.gi.hybridplayer.model.Portal
import com.gi.hybridplayer.model.Profile
import com.gi.hybridplayer.model.Vod
import com.gi.hybridplayer.view.MenuAdapter

class MenuFragment(val portal: Portal): Fragment() {

    private lateinit var binding:FragmentMenuBinding
    private lateinit var menuView: VerticalGridView
    private val menuTree: MutableList<Menu> = mutableListOf()



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val liveTv = Menu(
            resources.getString(R.string.live_tv),
            ContextCompat.getDrawable(requireContext(), R.drawable.ic_tv)!!
        )
        val vod = Menu(
            resources.getString(R.string.vod),
            ContextCompat.getDrawable(requireContext(), R.drawable.ic_vod)!!
        )
        val tvSeries = Menu(
            resources.getString(R.string.tv_series),
            ContextCompat.getDrawable(requireContext(), R.drawable.ic_series)!!
        )
        val recording = Menu(
            resources.getString(R.string.recordings),
            ContextCompat.getDrawable(requireContext(), R.drawable.ic_recording)!!
        )

        val portal = Menu(
            resources.getString(R.string.portal),
            ContextCompat.getDrawable(requireContext(), R.drawable.ic_portal)!!
        )
        val settings = Menu(
            resources.getString(R.string.setting),
            ContextCompat.getDrawable(requireContext(), R.drawable.ic_settings)!!
        )
        val exit = Menu(
            resources.getString(R.string.exit_text_1),
            ContextCompat.getDrawable(requireContext(), R.drawable.ic_exit)!!
        )
        if (menuTree.size == 0) {
            menuTree.add(liveTv)
            menuTree.add(vod)
            menuTree.add(tvSeries)
            menuTree.add(recording)
            menuTree.add(portal)
            menuTree.add(settings)
            menuTree.add(exit)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMenuBinding.inflate(inflater, container, false)
        menuView = binding.menuContainer
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val menuAdapter = MenuAdapter(menuTree, ItemClickListener())
        menuView.adapter = menuAdapter
        view.requestFocus()
    }

    inner class ItemClickListener(): MenuAdapter.OnItemClickEventListener{

        @Suppress("CAST_NEVER_SUCCEEDS")
        override fun onItemClick(menuString: String?) {

            when (menuString) {
                resources.getString(R.string.live_tv) -> {
                    activity?.supportFragmentManager?.popBackStack()
                }
                resources.getString(R.string.vod) -> {
                    val intent = Intent(activity, VodActivity::class.java)
                    intent.putExtra(Portal.PORTAL_INTENT_TAG, portal)
                    intent.putExtra(Vod.TYPE, Vod.TYPE_VOD)
                    intent.putExtra(Profile.TAG_INTENT_PROFILE, (activity as TvActivity).getProfile())
                    startActivity(intent)
                }
                resources.getString(R.string.tv_series) -> {
                    val intent = Intent(activity, VodActivity::class.java)
                    intent.putExtra(Portal.PORTAL_INTENT_TAG, portal)
                    intent.putExtra(Vod.TYPE, Vod.TYPE_SERIES)
                    intent.putExtra(Profile.TAG_INTENT_PROFILE, (activity as TvActivity).getProfile())
                    startActivity(intent)
                }
                resources.getString(R.string.recordings) -> {
                    val intent = Intent(activity, RecordingsActivity::class.java)
                    startActivity(intent)
                }
                resources.getString(R.string.portal) -> {
                    val intent = Intent(activity, MainActivity::class.java)
                    intent.putExtra(Menu.TAG_PORTAL, "portal_intent")
                    startActivity(intent)
                }
                resources.getString(R.string.setting) -> {
//                    val intent = Intent(activity?.baseContext, SettingsActivity::class.java)
//                    val rootActivity = activity as TvActivity
//                    val profile = rootActivity.mProfile
//                    val portal = rootActivity.mConnectedPortal
//                    intent.putExtra(Profile.TAG_INTENT_PROFILE, profile)
//                    intent.putExtra(Portal.PORTAL_INTENT_TAG, portal)
//                    startActivity(intent)


                }
                resources.getString(R.string.exit_text_1) -> {
                    val rootActivity = activity as TvActivity
                    rootActivity.exit()

                }
            }
        }

    }





}