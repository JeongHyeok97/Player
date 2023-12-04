package com.gi.hybridplayer

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import com.fasterxml.jackson.databind.ObjectMapper
import com.gi.hybridplayer.conf.DeviceManager
import com.gi.hybridplayer.conf.Server
import com.gi.hybridplayer.databinding.ActivityMainBinding
import com.gi.hybridplayer.db.repository.PortalRepository
import com.gi.hybridplayer.model.Menu
import com.gi.hybridplayer.model.Portal
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import java.io.IOException

class MainActivity : FragmentActivity(){

    private lateinit var mainActivityBinding: ActivityMainBinding
    private val mBackgroundScope = CoroutineScope(Dispatchers.IO)
    private lateinit var mRepository: PortalRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainActivityBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mainActivityBinding.root)
        mRepository = PortalRepository(this)

        if (intent.getStringExtra(Menu.TAG_PORTAL) == null){
            mBackgroundScope.launch {
                val mac = DeviceManager.getMacAddress()
                withContext(Dispatchers.Main){
                    val macView = findViewById<TextView>(R.id.mac_address)
                    val macAddress = "MAC : $mac"
                    macView.text = macAddress
                    macView.visibility = View.VISIBLE
                }
                isSupported(mac)
            }
        }
        else{
            val sfm = supportFragmentManager
            sfm.beginTransaction()
                .replace(R.id.portal_fragment, MainFragment())
                .commit()

        }
    }


    private fun isSupported(macValue: String){
        val client = Server.createClient()
        val macCheckUrl = "${Server.URL}${Server.PATH_MAC}"
        val macUrl = macCheckUrl.toHttpUrlOrNull()
            ?.newBuilder()
            ?.addEncodedQueryParameter("mac", macValue)
            ?.build()!!

        val request: Request = Request.Builder()
            .url(macUrl)
            .build()
        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val responseData = response.body?.string()
                    if (ObjectMapper().readTree(responseData).get("type").asText() == "200"){
                        checkVersion(client)
                    }
                    else{
                        finishAndRemoveTask()
                    }
                }
                else{
                    Toast.makeText(this@MainActivity,
                        resources.getString(R.string.auth_failed),
                        Toast.LENGTH_LONG).show()
                }
            }
            override fun onFailure(call: Call, e: IOException) {
                Toast.makeText(this@MainActivity,
                    resources.getString(R.string.auth_failed),
                    Toast.LENGTH_LONG).show()
                e.printStackTrace()
            }
        })
    }
    private fun checkVersion(client:OkHttpClient) {
        val versionCheck = "${Server.URL}${Server.PATH_VERSION}"
        val versionCheckUrl = versionCheck.toHttpUrlOrNull()
            ?.newBuilder()
            ?.build()!!

        val request: Request = Request.Builder()
            .url(versionCheckUrl)
            .build()

        val versionCode = BuildConfig.VERSION_CODE
        val versionName = BuildConfig.VERSION_NAME
        val response = client.newCall(request).execute()
        if (response.isSuccessful) {
            val versionData = ObjectMapper().readTree(response.body?.string())
            val needUpdate = ((versionCode < versionData.get("versionCode").asInt())
                    || versionName < versionData.get("versionName").asText())
            if (needUpdate){
                runOnUiThread {
                    AlertDialog.Builder(this@MainActivity)
                        .setTitle("Update")
                        .setMessage("We have a new update")
                        .setPositiveButton("OK") { _, _ ->
                            val updateLink = versionData.get("downloadLink").asText()
                            val update = Update(
                                versionCode = versionCode,
                                versionName = versionName,
                                updateLink = updateLink
                            )
                            val intent = Intent(this@MainActivity, UpdateService::class.java)
                            intent.putExtra(Update.UPDATE_INTENT_TAG, update)
                            intent.putExtra(Update.UPDATE_RESULT_TAG, UpdateReceiver(this, Handler(
                                Looper.getMainLooper())))
                            startService(intent)
                        }
                        .setNegativeButton("Exit") { _, _ ->
                            finishAndRemoveTask()
                        }
                        .create()
                        .show()
                }
            }
            else{
                response.close()
                mBackgroundScope.launch {
                    val connectedPortal = mRepository.getConnectedPortal()
                    withContext(Dispatchers.Main){
                        if (connectedPortal == null){
                            val sfm = supportFragmentManager
                            if (!sfm.isDestroyed){
                                runOnUiThread{
                                    sfm.beginTransaction()
                                        .replace(R.id.portal_fragment, MainFragment())
                                        .commit()
                                }
                            }
                        }
                        else{
                            val intent = Intent(this@MainActivity, TvActivity::class.java)
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                            intent.putExtra(Portal.PORTAL_INTENT_TAG, connectedPortal)
                            startActivity(intent)
                        }
                    }
                }
            }
        }

    }

    fun getRepository(): PortalRepository {
        return mRepository
    }


}