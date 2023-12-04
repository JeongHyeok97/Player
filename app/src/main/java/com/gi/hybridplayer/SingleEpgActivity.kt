package com.phoenix.phoenixplayer2.components

import android.os.Bundle
import android.view.View
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import com.gi.hybridplayer.R
import com.gi.hybridplayer.conf.ConnectManager
import com.gi.hybridplayer.model.DayWeek
import com.gi.hybridplayer.model.Portal
import com.gi.hybridplayer.viewmodel.SingleEpgViewModel
import com.gi.hybridplayer.SingleEpgFragment
import com.gi.hybridplayer.model.Channel
import com.phoenix.phoenixplayer2.fragments.WeekFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SingleEpgActivity : FragmentActivity(){

    private lateinit var mSingleEpgViewModel: SingleEpgViewModel
    private lateinit var mWeek : List<DayWeek>
    private lateinit var mConnectManager: ConnectManager


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_single_epg)
        mSingleEpgViewModel = ViewModelProvider(this)[SingleEpgViewModel::class.java]
        val portal = intent.getSerializableExtra(Portal.PORTAL_INTENT_TAG) as Portal

        mConnectManager = ConnectManager(portal = portal)
        CoroutineScope(Dispatchers.IO).launch {
            mWeek = mConnectManager.getWeek()
            withContext(Dispatchers.Main){
                val transaction = supportFragmentManager.beginTransaction()
                    .replace(R.id.single_epg_day, WeekFragment())
                    .replace(R.id.single_epg_program, SingleEpgFragment())

                try {
                    transaction.commit()
                }
                catch (e:Exception){
                    e.printStackTrace()
                }
            }
        }
    }

    fun getWeek():List<DayWeek>{
        View.VISIBLE
        return mWeek
    }

    fun getViewModel(): SingleEpgViewModel {
        return mSingleEpgViewModel
    }

    fun getConnectManager(): ConnectManager {
        return mConnectManager
    }

    fun getCurrentChannelId(): Long {
        return intent.getLongExtra(Channel.CHANNEL_INTENT_TAG, -1)
    }
}