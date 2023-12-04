package com.gi.hybridplayer

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.KeyEvent
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import com.gi.hybridplayer.conf.ConnectManager
import com.gi.hybridplayer.databinding.ActivityVodBinding
import com.gi.hybridplayer.model.Portal
import com.gi.hybridplayer.model.Profile
import com.phoenix.phoenixplayer2.fragments.VodFragment
import com.gi.hybridplayer.model.Vod
import com.gi.hybridplayer.viewmodel.VodViewModel
import com.gi.hybridplayer.viewmodel.VodViewModelFactory

class VodActivity : FragmentActivity() {

    private lateinit var vodViewModel: VodViewModel
    lateinit var connectManager: ConnectManager
    lateinit var binding: ActivityVodBinding
    lateinit var portal: Portal
    lateinit var type:String
    lateinit var profile: Profile
    private lateinit var categoryFragment: VodCategoryFragment


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_vod)

        portal = intent.getSerializableExtra(Portal.PORTAL_INTENT_TAG)
                as Portal
        type = intent.getStringExtra(Vod.TYPE).toString()
        profile = intent.getSerializableExtra(Profile.TAG_INTENT_PROFILE) as Profile

        connectManager = ConnectManager(portal)

        vodViewModel = ViewModelProvider(this,
            VodViewModelFactory(connectManager)
        )[VodViewModel::class.java]
        categoryFragment = VodCategoryFragment()
        supportFragmentManager.beginTransaction()
            .replace(R.id.vod_category_container, categoryFragment)
            .replace(R.id.vod_item_container, VodFragment())
            .commit()
    }

    fun getMainViewModel(): VodViewModel {
        return vodViewModel
    }


    fun setLoading(isLoading: Boolean){

        if (isLoading){
            binding.vodLoading.visibility = VISIBLE
        }
        else{
            binding.vodLoading.visibility = GONE
        }
    }

    fun isLoading(): Boolean{
        return binding.vodLoading.visibility == VISIBLE
    }
    @SuppressLint("UseRequireInsteadOfGet")
    fun hideCategory() {
        val layoutParams = binding.vodCategoryContainer.layoutParams as ConstraintLayout.LayoutParams
        if (categoryFragment.view?.hasFocus()!! && layoutParams.leftMargin < 0) {
            layoutParams.leftMargin = 0
            binding.vodCategoryContainer.layoutParams = layoutParams
        }
    }
    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
            hideCategory()
        } else if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
            showCategory()
        }
        return super.onKeyUp(keyCode, event)
    }
    @SuppressLint("UseRequireInsteadOfGet")
    fun showCategory() {
        if (categoryFragment.adapter.size()>0) {
            val layoutParams =
                binding.vodCategoryContainer.layoutParams as ConstraintLayout.LayoutParams
            if (!categoryFragment.view?.hasFocus()!! && layoutParams.leftMargin == 0) {
                layoutParams.leftMargin = layoutParams.width * -1
                binding.vodCategoryContainer.layoutParams = layoutParams
            }
        }
    }

}