package com.gi.hybridplayer


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.gi.hybridplayer.databinding.FragmentActionBinding
import com.gi.hybridplayer.model.Portal
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PortalActionFragment(val portal: Portal? = null) : Fragment(){


    private lateinit var binding: FragmentActionBinding
    private lateinit var actionConnect: TextView
    private lateinit var actionEdit:TextView
    private lateinit var actionDelete:TextView

    private val selectedPortal = portal!!





    companion object{
        private const val TAG: String = "PortalActionFragment"
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentActionBinding.inflate(inflater, container, false)
        binding.portal = portal
        actionConnect = binding.actionConnect
        val sfm = activity?.supportFragmentManager!!
        actionConnect.setOnClickListener{
            CoroutineScope(Dispatchers.IO).launch {
                (activity as MainActivity).getRepository().connect()
                withContext(Dispatchers.Main){
                    sfm.beginTransaction().add(R.id.main_frame, LoadFragment(selectedPortal))
                        .addToBackStack(null)
                        .hide(this@PortalActionFragment).commit()
                }
            }
        }
        actionEdit = binding.actionEdit
        actionEdit.setOnClickListener {
            sfm.beginTransaction().add(R.id.main_frame, PortalEditFragment(selectedPortal))
                .addToBackStack(null)
                .hide(this@PortalActionFragment).commit()
        }
        actionDelete = binding.actionDelete
        actionDelete.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                (activity as MainActivity).getRepository().delete(selectedPortal)
                withContext(Dispatchers.Main){
                    sfm.popBackStack()
                }
            }
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }
}