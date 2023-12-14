package com.gi.hybridplayer


import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.WindowManager
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.FragmentActivity
import androidx.leanback.preference.LeanbackPreferenceFragment
import androidx.leanback.preference.LeanbackSettingsFragment
import androidx.preference.Preference
import androidx.preference.PreferenceFragment
import androidx.preference.PreferenceScreen
import com.gi.hybridplayer.conf.DeviceManager
import com.gi.hybridplayer.model.Portal
import com.gi.hybridplayer.model.Profile
import com.gi.hybridplayer.view.BaseDialog
import com.gi.hybridplayer.BuildConfig
import com.gi.hybridplayer.databinding.PrefInfoBinding
import java.util.*

@Suppress("DEPRECATION")
class SettingsFragment: LeanbackSettingsFragment() {


    companion object{
        const val PREFERENCE_RESOURCE_ID = "preferenceResource"
        const val PREFERENCE_ROOT = "root"
        const val TAG = "SettingsFragment"
    }


    override fun onPreferenceStartFragment(
        caller: PreferenceFragment?,
        pref: Preference?
    ): Boolean {

        return false
    }



    override fun onPreferenceStartScreen(
        caller: PreferenceFragment?,
        pref: PreferenceScreen?
    ): Boolean {
        val frag = buildPreferenceFragment(R.xml.prefs, pref?.key!!)
        startPreferenceFragment(frag)
        return true
    }

    override fun onPreferenceStartInitialScreen() {
        startPreferenceFragment(buildPreferenceFragment(R.xml.prefs, null))
    }


    private fun buildPreferenceFragment(preferenceResId: Int, root: String?): PreferenceFragment {
        val fragment: PreferenceFragment =
            PrefFragment()
        val args = Bundle()
        args.putInt(PREFERENCE_RESOURCE_ID, preferenceResId)
        args.putString(PREFERENCE_ROOT, root)
        fragment.arguments = args
        return fragment
    }


    @SuppressLint("ValidFragment")
    class PrefFragment : LeanbackPreferenceFragment() {

        var portal:Portal? = null
        var profile: Profile? = null


        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            portal = activity.intent.getSerializableExtra(Portal?.PORTAL_INTENT_TAG) as Portal
            profile = activity.intent.getSerializableExtra(Profile.TAG_INTENT_PROFILE) as Profile


        }

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            val root = arguments.getString(PREFERENCE_ROOT, null)
            val prefResId = arguments.getInt(PREFERENCE_RESOURCE_ID)
            if (root == null) {
                addPreferencesFromResource(prefResId)
            } else {
                setPreferencesFromResource(prefResId, root)
            }
        }


        private fun showData(title: String, results: List<String>){
            val dialog = BaseDialog(context)
            val binding:PrefInfoBinding = DataBindingUtil.inflate(
                LayoutInflater.from(context),
                R.layout.pref_info, null, false)
            binding.data = results
            dialog.bindContentView(binding.root)
            dialog.setTitle(title)
            dialog.setWidth(resources.getDimensionPixelSize(R.dimen.prefs_dialog_width))
            dialog.show()
        }

        @SuppressLint("HardwareIds")
        override fun onPreferenceTreeClick(preference: Preference?): Boolean {

            if (preference?.key == "prefs_portal_info_key"){
                val nameResult = "Portal Name: ${portal?.title}"
                val expResult = "Expired Date: ${portal?.exp_date}"
                val urlResult = "Portal Url: ${portal?.url}"

                val parts = profile
                    ?.defaultLocale
                    ?.split("[_.]".toRegex())!!
                    .toTypedArray()
                val language = parts[0]
                val country = parts[1]
                val encoding = parts[2]
                val locale = Locale(language, country, encoding)
                val localeResult = "Language: ${locale.displayLanguage} - ${locale.displayCountry}"
                val results = mutableListOf<String>()
                results.add(nameResult)
                results.add(urlResult)
                results.add(expResult)
                results.add(localeResult)
                showData(preference.title.toString(),results)

            }

            else if (preference?.title == resources.getString(R.string.settings_box_information)){
                val realMac = DeviceManager.getMacAddress()
                val model = Build.MODEL
                val version = BuildConfig.VERSION_NAME
                val serial:String = try {
                    Build.getSerial()
                } catch (e:Exception){
                    "-"
                }
                val results = mutableListOf<String>()
                results.add("MAC Address: $realMac")
                results.add("Model: $model")
                results.add("Serial Number: $serial")
                results.add("Player version: $version")
                showData(preference.title.toString(), results)
            }
            else if (preference?.title == resources.getString(R.string.settings_parental_pin)){
                if (profile != null){
                    val parentalControlFragment = ParentalControlFragment(ParentalControlFragment.AUTH_FOR_CHANGE)
                    (activity as FragmentActivity).supportFragmentManager.beginTransaction().add(R.id.settings_root, parentalControlFragment)
                        .addToBackStack(null).commit()
                }

            }
            return super.onPreferenceTreeClick(preference)


        }
    }
}