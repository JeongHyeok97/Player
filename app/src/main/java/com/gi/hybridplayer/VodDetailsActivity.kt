package com.gi.hybridplayer

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import com.fasterxml.jackson.databind.ObjectMapper
import com.gi.hybridplayer.conf.CloudDBManager
import com.gi.hybridplayer.conf.ConnectManager
import com.gi.hybridplayer.databinding.ActivityDetailBinding
import com.gi.hybridplayer.model.Credits
import com.gi.hybridplayer.model.Portal
import com.gi.hybridplayer.model.Vod
import com.gi.hybridplayer.viewmodel.VodDetailsViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class VodDetailsActivity: FragmentActivity() {

    lateinit var binding: ActivityDetailBinding
    lateinit var viewModel: VodDetailsViewModel
    lateinit var portal: Portal
    lateinit var connectManager: ConnectManager

    companion object {

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_detail)
        portal = intent.getSerializableExtra(Portal.PORTAL_INTENT_TAG)
                as Portal
        connectManager = ConnectManager(portal)
        viewModel = ViewModelProvider(this)[VodDetailsViewModel::class.java]
        binding.vod = viewModel
        val selectedVod: Vod = intent.getSerializableExtra(Vod.INTENT_VOD_TAG) as Vod
        CoroutineScope(Dispatchers.IO).launch {
            if (selectedVod.cmd?.startsWith("/") == true){
                try {
                    val vodData = ObjectMapper().readTree(connectManager.getOrderedList(type = "vod", movie_id = selectedVod.id, page = 0)).get("js").get("data")
                    if (vodData.size()>0){
                        val cmd = vodData[0].get("url")
                        selectedVod.cmd = cmd.toString()
                    }

                }
                catch (e:Exception){
                    e.printStackTrace()
                }
            }

            withContext(Dispatchers.Main){
                viewModel.select(selectedVod)
            }
           setupDetail(selectedVod)
        }
        supportFragmentManager.beginTransaction().replace(R.id.details_container, VodDetailsFragment())
            .commitNow()
    }




    suspend fun setupDetail(selectedVod: Vod){
        val manager = CloudDBManager()
        val type: String
                = if (selectedVod.isSeries == "1") Vod.Detail.TYPE_SERIES
        else Vod.Detail.TYPE_MOVIE
        var detailsApi:String = ""
        var creditsApi:String =""
        if (selectedVod.tmdbId != null){
            detailsApi = manager.getTmDbApi(type,selectedVod.tmdbId!!, "")!!
            creditsApi = manager.getCreditsApi(type = type, tmdbID = selectedVod.tmdbId)!!
        }
        else{
            detailsApi = manager.getKinopoiskApi(selectedVod.kinopoiskId!!)

        }

        val om = ObjectMapper()
        try {
            val detailNode = om.readTree(detailsApi)
            val vodDetail = om.treeToValue(detailNode, Vod.Detail::class.java)


            withContext(Dispatchers.Main){
                viewModel.setDetail(vodDetail)
            }

            val creditsNode = om.readTree(creditsApi)

            val casts = mutableListOf<Credits.Cast>()
            val crews = mutableListOf<Credits.Crew>()
            for (node in creditsNode["cast"]){
                val cast = om.treeToValue(node, Credits.Cast::class.java)
                casts.add(cast)
            }
            for (node in creditsNode["crew"]){
                val crew = om.treeToValue(node, Credits.Crew::class.java)
                crews.add(crew)
            }

            withContext(Dispatchers.Main){
                viewModel.setCredits(casts, crews)

            }

        }
        catch (e:Exception){
            e.printStackTrace()
            Log.e("VOD ERROR ", detailsApi)
            withContext(Dispatchers.Main){
                Toast.makeText(this@VodDetailsActivity,
                    "VOD Server status is unstable. Please try again later", Toast.LENGTH_LONG).show()
            }
        }
    }


}