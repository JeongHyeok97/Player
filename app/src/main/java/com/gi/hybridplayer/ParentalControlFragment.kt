package com.gi.hybridplayer


import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.gi.hybridplayer.conf.ConnectManager
import com.gi.hybridplayer.databinding.DialogAuthBinding
import com.gi.hybridplayer.model.Profile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ParentalControlFragment(private val type:String): Fragment() {

    private lateinit var mRootActivity: FragmentActivity
    private lateinit var binding:DialogAuthBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (activity is TvActivity){
            mRootActivity = activity as TvActivity

        }
        else if (activity is SettingsActivity){
            mRootActivity = activity as SettingsActivity
        }
        else if (activity is VodActivity){
            mRootActivity = activity as VodActivity
        }

    }


    private var mPassword: String? = null
    private var mNewPassword: String? = null
    private var mRepeatPassWord: String? = null
    companion object{
        const val AUTH_FOR_TUNE = "auth"
        const val AUTH_FOR_UNLOCK = "unlock"
        const val AUTH_FOR_CHANGE = "change"
        const val AUTH_FOR_UNBLOCK_CATEGORY = "category"
    }




    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DialogAuthBinding.inflate(inflater, container, false)
        binding.parentalInputText.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s?.length == 4){
                   if (mRootActivity is SettingsActivity){
                        changePassword()
                    }
                }
            }

            override fun afterTextChanged(s: Editable?) {

            }

        })

        return binding.root
    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.parentalInputText.requestFocus()
    }



    fun changePassword(){
        if (type == AUTH_FOR_CHANGE){
            val settingActivity = mRootActivity as SettingsActivity
            val profile = settingActivity.getProfile()
            val password = binding.parentalInputText.text.toString()
            binding.parentalInputText.setText("")
            if (binding.parentalTitle.text == resources.getString(R.string.parental_title)){
                if (password == profile?.parentPassword) {
                    binding.parentalTitle.text = resources.getString(R.string.new_password)
                    mPassword =password
                }
                else{
                    Toast.makeText(requireContext(), "Password Incorrect!", Toast.LENGTH_LONG).show()
                }
            }
            else if (binding.parentalTitle.text == resources.getString(R.string.new_password)){
                mNewPassword = password
                binding.parentalTitle.text = resources.getString(R.string.repeat_password)
            }
            else if (binding.parentalTitle.text == resources.getString(R.string.repeat_password)){
                if (mNewPassword == password){
                    mRepeatPassWord = password
                    CoroutineScope(Dispatchers.IO).launch {
                        val connectManager = ConnectManager(settingActivity.getPortal()!!)
                        val result = connectManager.setPassword(
                            mPassword!!, mNewPassword!!,
                            mRepeatPassWord!!
                        )
                        withContext(Dispatchers.Main){
                            val newProfile: Profile = profile?.copy(parentPassword = mNewPassword)!!
                            settingActivity.setProfile(newProfile)
                            settingActivity.supportFragmentManager.popBackStack()
                        }
                    }
                }
                else{
                    Toast.makeText(requireContext(), "Password Incorrect!", Toast.LENGTH_LONG).show()
                }
            }
            binding.parentalInputText.requestFocus()
        }
    }
}
