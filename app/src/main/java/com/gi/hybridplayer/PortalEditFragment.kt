package com.gi.hybridplayer


import android.annotation.SuppressLint
import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.*
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.fasterxml.jackson.databind.ObjectMapper
import com.gi.hybridplayer.conf.ConnectManager
import com.gi.hybridplayer.conf.DeviceManager
import com.gi.hybridplayer.conf.Server
import com.gi.hybridplayer.databinding.FragmentEditBinding
import com.gi.hybridplayer.model.Portal
import com.gi.hybridplayer.model.enums.EPGMode
import com.gi.hybridplayer.model.enums.EPGOffset
import com.gi.hybridplayer.model.enums.GroupChannelNumbering
import com.gi.hybridplayer.model.enums.MacType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import java.io.IOException
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

class PortalEditFragment(private var portal: Portal? = null) : Fragment(){

    private lateinit var binding: FragmentEditBinding
    private val edit:Boolean = portal != null
    private var mPortalId: Long? = -1
    private val mIOScope = CoroutineScope(Dispatchers.IO)



    companion object {
        private const val TAG:String = "PortalEditFragment"

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (edit){
            mPortalId = portal?.id
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentEditBinding.inflate(inflater, container, false)
        setSpinners()
        setCheckBox()
        setButtons()
        return binding.root
    }

    private fun setButtons() {
        binding.buttonEdit.setOnClickListener {

            createPortal()
        }
        binding.buttonQuick.setOnClickListener {
            quickInsert()
        }
    }
    private fun getUnsafeOkHttpClient(): OkHttpClient {
        try {
            val trustAllCertificates: Array<TrustManager> = arrayOf(
                object : X509TrustManager {
                    override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {
                    }

                    override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {
                    }

                    override fun getAcceptedIssuers(): Array<X509Certificate> {
                        return arrayOf()
                    }
                }
            )

            val sslContext: SSLContext = SSLContext.getInstance("TLS")
            sslContext.init(null, trustAllCertificates, java.security.SecureRandom())

            val sslSocketFactory = sslContext.socketFactory

            return OkHttpClient.Builder()
                .sslSocketFactory(sslSocketFactory, trustAllCertificates[0] as X509TrustManager)
                .hostnameVerifier { _, _ -> true }
                .build()
        } catch (e: Exception) {

            throw RuntimeException(e)
        }
    }

    private fun quickInsert(){
        val builder =  AlertDialog.Builder(requireContext())
        val inputCode: EditText = EditText(requireContext())
        builder.setTitle(resources.getString(R.string.quick_insert))
            .setView(inputCode)
            .setPositiveButton("Insert"
            ) { _, _ ->
                mIOScope.launch {
                    val client = getUnsafeOkHttpClient()
                    val url = ("${Server.URL}${Server.PATH_QUICK}").toHttpUrlOrNull()?.newBuilder()?.
                    addQueryParameter("code", inputCode.text.toString())?.build()
                    val request = Request.Builder()
                        .url(url!!)
                        .build()
                    client.newCall(request).enqueue(object : Callback{
                        override fun onFailure(call: Call, e: IOException) {

                        }

                        override fun onResponse(call: Call, response: Response) {
                            if (response.isSuccessful){
                                val result = ObjectMapper().readTree(response.body?.string())
                                if (result.get("type").asText() == "200"){
                                    requireActivity().runOnUiThread{
                                        val nickname = result.get("nickname")
                                        val portalUrl = result.get("url")
                                        binding.portalNameInput.setText(nickname.asText())
                                        binding.portalUrlInput.setText(portalUrl.asText())
                                    }
                                }
                                else{
                                    activity?.runOnUiThread {
                                        Toast.makeText(requireContext(), "Invalid Code", Toast.LENGTH_LONG).show()
                                    }
                                }


                            }
                        }
                    })
                }
            }
            .create().show()
    }

    private fun setCheckBox() {
        val login = binding.checkBox
        login.setOnClickListener {
            val checkBox = it as CheckBox
            if (checkBox.isChecked) {
                binding.userName.visibility = VISIBLE
                binding.userPw.visibility = VISIBLE
            } else {
                binding.userName.visibility = GONE
                binding.userPw.visibility = GONE
            }
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.portalNameInput.requestFocus()
        if (edit){
            binding.portalNameInput.setText(portal!!.title)
            binding.portalUrlInput.setText(portal!!.url)
        }

    }

    private fun setSpinners(){
        val epgModes = EPGMode.values().toList()
        val epgOffsets = EPGOffset.values().toList()
        val grouping = GroupChannelNumbering.values().toList()

        val macTypes = MacType.values().toList()
        binding.spinnerEpgMode.adapter = getSpinnerAdapter(epgModes)
        binding.spinnerEpgOffset.adapter = getSpinnerAdapter(epgOffsets)
        binding.spinnerGrouping.adapter = getSpinnerAdapter(grouping)
        binding.spinnerMacType.adapter = getSpinnerAdapter(macTypes)

    }

    private fun getSpinnerAdapter(list: List<*>): ArrayAdapter<*>{
        return ArrayAdapter(requireContext(), R.layout.spinner_textview, list)
    }


    /**
     * Because the API I use may be different from the Portal API you want (+ my company's copyrighted elements as well)
     * the analysis part of the API has not been uploaded to the repository
     * You just need to formally implement a class called "ConnectManager"(which is just an arbitrary class name)
     * */

    @SuppressLint("SetJavaScriptEnabled")
    private fun createPortal(){

        val loading = binding.editPageLoadingProgress
        val name = binding.portalNameInput.text.toString()
        val clientUrl = binding.portalUrlInput.text.toString()
        val login = binding.checkBox.isChecked
        val userName = binding.userName.text.toString()
        val password = binding.userPw.text.toString()
        val epgMode = binding.spinnerEpgMode.selectedItem as EPGMode
        val epgOffset = binding.spinnerEpgOffset.selectedItem as EPGOffset
        val grouping = binding.spinnerGrouping.selectedItem as GroupChannelNumbering
        val macType = binding.spinnerMacType.selectedItem as MacType
        val defaultMacAddress = DeviceManager.getMacAddress()
        val macAddress:String = when (macType){
            MacType.Default -> {
                macType.getAddress() + defaultMacAddress.substring(8, 17)
            }
            MacType.Alternative -> {defaultMacAddress}
            MacType.Custom -> {""}
        }

        loading.visibility = VISIBLE



        // << Hybrid can run to here

        if (!edit){
            portal = Portal(
                title = name,
                url = clientUrl,
                log_req = login,
                user_ID =  userName,
                user_PW = password,
                EPG_MODE = epgMode,
                EPG_OFFSET = epgOffset,
                group_Numbering = grouping,
                MAC_TYPE = macType,
                macAddress = macAddress)
        }
        else{
            val id = portal?.id
            portal = portal?.copy(
                title = name,
                url = clientUrl,
                log_req = login,
                user_ID =  userName,
                user_PW = password,
                EPG_MODE = epgMode,
                EPG_OFFSET = epgOffset,
                group_Numbering = grouping,
                MAC_TYPE = macType,
                macAddress = macAddress
            )
        }





        CoroutineScope(Dispatchers.IO).launch {
            val objectMapper = ObjectMapper()
            var connectManager = ConnectManager()
            try {
                val serverUrl = connectManager.getServerUrl(portalUrl = portal!!.url)
                val token = connectManager.getToken(macAddress)
                portal = portal!!.copy(
                    serverUrl = serverUrl,
                    token = token
                )
                connectManager = ConnectManager(portal)
                val profile = connectManager.getProfile()
                if (profile?.status == "2" || profile?.message == "Authentication request"){
                    if (!binding.checkBox.isChecked){

                        withContext(Dispatchers.Main){
                            binding.checkBox.performClick()
                            binding.userName.requestFocus()
                            loading.visibility = INVISIBLE
                        }
                    }
                    else{
                        val result = connectManager.doAuth(
                            binding.userName.text.toString(),
                            binding.userPw.text.toString()
                        )
                        if (objectMapper.readTree(result).get("js").asText() == "true"){
                            portal = portal!!.copy(user_ID = binding.userName.text.toString(),
                                user_PW =  binding.userPw.text.toString())
                        }
                    }
                }

                val expDate = connectManager.getMainInfo()!!.expDate

                portal = portal!!.copy(
                    exp_date = expDate
                )


                val x = portal!!
                val repo =  (activity as MainActivity).getRepository()
                if (edit){
                    x.id = mPortalId
                    repo.update(x)
                }
                else{
                    repo.insert(x)
                }
                withContext(Dispatchers.Main){
                    loading.visibility = GONE
                    (activity as MainActivity).supportFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)

                }

            }
            catch (e:Exception){
                e.printStackTrace()
                withContext(Dispatchers.Main){
                    val message = if (e.message?.contains("Authorization") == false){
                        "Login information is incorrect. Please review your input."
                    }
                    else{
                        "Authorization Required."
                    }
                    Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                }

            }
        }
    }

    suspend fun connect(macAddress: String){

    }



}
