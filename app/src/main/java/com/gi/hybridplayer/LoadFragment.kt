package com.gi.hybridplayer

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Intent
import android.media.tv.TvInputInfo
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.gi.hybridplayer.conf.ConnectManager
import com.gi.hybridplayer.databinding.FragmentLoadingBinding
import com.gi.hybridplayer.model.Portal
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoadFragment(private var selectedPortal: Portal): Fragment(), TvDataManager.StateListener {

    private lateinit var binding: FragmentLoadingBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.loadingCircle.startAnimation(AnimationUtils.loadAnimation(context, R.anim.rotate))
        val connectManager = ConnectManager(selectedPortal)

        val dataManager = TvDataManager(requireContext(), selectedPortal)
        CoroutineScope(Dispatchers.IO).launch {
            val channelsData = connectManager.getAllChannelsData()
            withContext(Dispatchers.Main){
                onStarted()
            }
            val blockedChannel = connectManager.getBlockedChannels()
            dataManager.insertAllChannels(channelsData,
                blockedChannel,
                this@LoadFragment)
//            context?.let { context ->
//                try {
//                    )}
//                catch (e:Exception){
//                    e.printStackTrace()
//                    requireActivity().runOnUiThread {
//                        Toast.makeText(requireContext(), e.message, Toast.LENGTH_LONG).show()
//                    }
//                }
//
//            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLoadingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onStarted() {
        binding.loadingText.text = resources.getString(R.string.start_download)

    }

    @SuppressLint("SetTextI18n")
    override fun onProgress(percent: Int) {
        binding.loadingProgress.progress = percent
        binding.loadingProgressText.text = "${percent}%"
    }

    override suspend fun onConnect(portal: Portal) {
        val repository = (activity as MainActivity).getRepository()
        repository.update(portal)
        selectedPortal = portal
        val intent = Intent(context, TvActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        intent.putExtra(Portal.PORTAL_INTENT_TAG, selectedPortal)
        startActivity(intent)
    }


    override fun onStop() {
        super.onStop()
        binding.loadingCircle.clearAnimation()

    }





}
