package com.gi.hybridplayer.model

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import java.time.ZoneId

@JsonIgnoreProperties(ignoreUnknown = true)
data class Category(
    val id: String? = "*",
    val title: String? = "All",
    val modified: String? = "",
    val number: Int? = -1,
    val alias: String? = "N/A",
    val activeSub: Boolean? = false,
    val censored: Boolean? = false
){
    companion object{
        const val FAVORITE_ID = "fav"
        const val HISTORY_ID = "hist"
        val FAVORITE = Category(id = FAVORITE_ID, title = "FAVORITE" , censored = false)
        val HISTORY = Category(id = HISTORY_ID, title = "RECENTLY WATCH", censored = false)
    }

    override fun toString(): String {
        return "Category{id:$id, title:$title}"
    }
}
