package com.gi.hybridplayer.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class Episode(

    @JsonProperty("name")
    val title:String?,
    @JsonProperty("episode_number")
    val episodeNumber: Int?,
    @JsonProperty("season_number")
    val seasonNumber : Int?,
    @JsonProperty("overview")
    val description:String? = null,
    @JsonProperty("runtime")
    val duration:Long?,
    @JsonProperty("still_path")
    val thumbnail:String?){
    companion object{
        const val EPISODE_INTENT = "episode_intent"
    }
    var cmd:String? = null
}
