package com.gi.hybridplayer.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class SingleEpg(
    @JsonProperty("id")
    val id:String? = null,
    @JsonProperty("ch_id")
    val channelId: String? = null,
    @JsonProperty("time")
    val time: String? = null,
    @JsonProperty("time_to")
    val timeTo: String? = null,
    @JsonProperty("duration")
    val duration: Long? = 0,
    @JsonProperty("name")
    val name: String? = null,
    @JsonProperty("descr")
    val descr: String? = null,
    @JsonProperty("real_id")
    val realId: String? = null,
    @JsonProperty("category")
    val category: String? = null,
    @JsonProperty("director")
    val director: String? = null,
    @JsonProperty("actor")
    val actor: String? = null,
    @JsonProperty("start_timestamp")
    val startTimestamp: Long = 0,
    @JsonProperty("stop_timestamp")
    val stopTimestamp: Long = 0,
    @JsonProperty("t_time")
    val tTime: String? = null,
    @JsonProperty("t_time_to")
    val tTimeTo: String? = null,
    @JsonProperty("open")
    val open:Long? = 0,
    @JsonProperty("mark_memo")
    val markMemo:Long? = 0,
    @JsonProperty("mark_rec")
    val markRec:Long? = 0,
    @JsonProperty("mark_archive")
    val markArchive:Long? = 0
                     ){
    val TAG_SINGLE_EPG_PORTAL = "single_epg_portal"
    val TAG_SINGLE_EPG_CHANNEL = "single_epg_channel"
    val TAG_CATCH_UP_VIDEO = "catch_up_video"


    fun getCmd(): String? {
        val cmdBase = "auto "
        return "$cmdBase/media/$id"
    }
}
