package com.gi.hybridplayer

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import com.gi.hybridplayer.model.Portal
import com.gi.hybridplayer.model.Profile

class SettingsActivity : FragmentActivity(){

    private var mProfile : Profile? = null
    private var mPortal : Portal? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        mProfile = intent.getSerializableExtra(Profile.TAG_INTENT_PROFILE) as Profile
        mPortal = intent.getSerializableExtra(Portal.PORTAL_INTENT_TAG) as Portal
    }



    fun getProfile(): Profile? {
        return mProfile
    }

    fun getPortal(): Portal? {
        return mPortal
    }
    fun setProfile(profile: Profile){
        mProfile = profile
    }



}