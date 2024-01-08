package com.gi.hybridplayer

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.gi.hybridplayer.databinding.FragmentGroupBinding
import com.gi.hybridplayer.db.repository.TvRepository
import com.gi.hybridplayer.model.Category
import com.gi.hybridplayer.model.Channel
import com.gi.hybridplayer.model.Portal
import com.gi.hybridplayer.view.CategoryAdapter
import com.gi.hybridplayer.view.ChannelAdapter
import com.gi.hybridplayer.viewmodel.EditGroupViewModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class EditGroupFragment(private val portal:Portal) : DialogFragment(){
    private lateinit var binding:FragmentGroupBinding
    private lateinit var mRepository: TvRepository
    private var mCategoryView: RecyclerView? = null
    private var mItemView: RecyclerView? = null
    private val mViewModelHandler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mRepository = TvRepository.getInstance(requireContext())
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)

    }

    override fun onResume() {
        super.onResume()
        dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog?.window?.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_group, container , false)
        mCategoryView = binding.editGroupCategory
        mItemView = binding.editGroupItems
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val viewModel = ViewModelProvider(this)[EditGroupViewModel::class.java]
        mCategoryView?.layoutManager = LinearLayoutManager(requireContext())
        mItemView?.layoutManager = LinearLayoutManager(requireContext())
        val categoryAdapter = CategoryAdapter(listOf(CategoryAdapter.ADD_CATEGORY))
        mCategoryView?.adapter = categoryAdapter
        categoryAdapter.setOnItemSelectedListener(object : CategoryAdapter.ItemSelectedListener{
            override fun onSelectItem(id: String) {
                mViewModelHandler.removeCallbacksAndMessages(null)
                if (id != CategoryAdapter.ADD_CATEGORY){
                    mViewModelHandler.postDelayed({viewModel.setGroupId(id)}, 750)
                }
            }

            override fun onClickItem(id: String) {
                if (id == CategoryAdapter.ADD_CATEGORY){

                }
            }
        })
        mItemView?.adapter = ChannelAdapter(mutableListOf())
        val itemAdapter = mItemView?.adapter as ChannelAdapter
        val dataMap = getDataMap()
        val dbScope = CoroutineScope(Dispatchers.IO)
        viewModel.groupId.observe(viewLifecycleOwner){ groupId->
            dbScope.launch {
                val idList = dataMap[groupId]?: listOf()
                val channelList = mutableListOf<Channel>()
                idList.forEach {
                    val channel = mRepository.getChannel(it)
                    if (channel != null){
                        channelList.add(channel)
                    }
                }
                itemAdapter.refresh(channelList)
            }
        }
    }


    suspend fun save(map: Map<String, List<Long>>) {
        val sharedPreferences = requireContext().getSharedPreferences("${portal.id}", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        val gson = Gson()
        val rootActivity = (requireActivity() as TvActivity)
        val categoryMap = rootActivity.getCategoryMap()
        map.forEach {(key, value) ->
            val jsonValue = gson.toJson(value)
            editor.putString(key, jsonValue)
            if (!categoryMap.containsKey(key)){
                rootActivity.putCategory(Category(id = key, title = key.substring(2)))
            }
        }
        editor.apply()
    }



    fun getDataMap(): Map<String, List<Long>> {
        val sharedPreferences = requireContext()
            .getSharedPreferences("${portal.id}", Context.MODE_PRIVATE)
        val gson = Gson()
//        val mapType = object : TypeToken<Map<String, List<Long>>>() {}.type

        val map = mutableMapOf<String, List<Long>>()
        sharedPreferences.all.forEach { (key, value) ->
            val jsonValue = value as? String
            val list = gson.fromJson<List<Long>>(jsonValue, object : TypeToken<List<Long>>() {}.type)
            if (jsonValue != null && list != null) {
                map[key] = list
            }
        }
        return map
    }
    inner class EditDialog() {
        private val dialog: Dialog
        private var positiveButton: View? = null
        private var negativeButton: View? = null

        init {
            dialog = Dialog(requireContext())
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.setCancelable(true)
            dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        }
    }
}