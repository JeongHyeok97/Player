package com.gi.hybridplayer.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class MainInfo(
    @JsonProperty("mac")
    val mac:String,
    @JsonProperty("phone")
    val expDate: String){
}

