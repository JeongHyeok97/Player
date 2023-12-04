package com.gi.hybridplayer

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.leanback.widget.ArrayObjectAdapter
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.gi.hybridplayer.view.CategoryPresenter
import com.gi.hybridplayer.view.SingleLineVerticalFragment
import com.gi.hybridplayer.model.VodCategory
import com.gi.hybridplayer.viewmodel.VodViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class VodCategoryFragment : SingleLineVerticalFragment(){

    private lateinit var rootActivity: VodActivity
    val listHandler: Handler = Handler(Looper.getMainLooper())
    private lateinit var type:String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        rootActivity = (activity as VodActivity)
        type = rootActivity.type

        val viewModel: VodViewModel = rootActivity.getMainViewModel()
        setOnItemViewClickedListener { itemViewHolder, item, rowViewHolder, row ->  }
        setOnItemViewSelectedListener { itemViewHolder, item, rowViewHolder, row ->
            if (item != null){
                listHandler.removeCallbacksAndMessages(null)
                rootActivity.setLoading(true)
                val category: VodCategory = item as VodCategory
                viewModel.setTitle(category.categoryStr!!)
                listHandler.postDelayed({
                    viewModel.select(category.categoryID!!, type, item.isCensored)
                }, 800)
            }
        }
        adapter = ArrayObjectAdapter(CategoryPresenter())
        CoroutineScope(Dispatchers.IO).launch {
            setupCategory()
        }
    }


    private suspend fun setupCategory(){
        val connectManager = rootActivity.connectManager
        val json = connectManager.getCategories(type)
        try {
            val node:JsonNode = ObjectMapper().readTree(json).get("js")
            node.forEach {
                val categoryId :String = it.get("id").asText()
                val categoryString :String = it.get("title").asText()
                val alias :String = it.get("alias").asText()
                val censored: Boolean = it.get("censored").asText() == "1"
                val vodCategory = VodCategory(
                    categoryId, categoryString, alias, censored
                )
                withContext(Dispatchers.Main){
                    (adapter as ArrayObjectAdapter).add(vodCategory)
                }
            }
        }
        catch (e: Exception){
            withContext(Dispatchers.Main){
                Toast.makeText(context, "Connection state is unstable, Try again", Toast.LENGTH_LONG).show()
            }
        }

    }
}