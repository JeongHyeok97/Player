package com.gi.hybridplayer.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class ShortEpg(
    @JsonProperty("id")
    val id: Long? = -1,
    @JsonProperty("ch_id")
    val channelId:Long? = -1,
    @JsonProperty("correct")
    val correct: String? = "",
    @JsonProperty("time")
    val time: String? = "",
    @JsonProperty("time_to")
    val timeTo: String? = "",
    @JsonProperty("duration")
    val duration:Long? = -1,
    @JsonProperty("name")
    val name:String? = "No Information",
    @JsonProperty("descr")
    val description:String? = "",
    @JsonProperty("real_id")
    val realId:String? = "",
    @JsonProperty("start_timestamp")
    val startTimestamp: Long? = -1,
    @JsonProperty("stop_timestamp")
    val stopTimestamp: Long? = -1,
    @JsonProperty("t_time")
    val tTime : String? = "",
    @JsonProperty("t_time_to")
    val tTimeTo : String? = ""){
    companion object{
        val EMPTY_PROGRAM = ShortEpg()
    }
}
