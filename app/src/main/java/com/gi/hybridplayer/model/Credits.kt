package com.gi.hybridplayer.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

data class Credits(val id: String) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class Cast(
        @JsonProperty("id")
        val id: String?,
        @JsonProperty("name")
        val name: String?,
        @JsonProperty("profile_path")
        val profilePath: String?,
        @JsonProperty("character")
        val character: String?
    ){


    }
    @JsonIgnoreProperties(ignoreUnknown = true)
    data class Crew(
        @JsonProperty("id")
        val id: String?,
        @JsonProperty("name")
        val name: String?,
        @JsonProperty("profile_path")
        val profilePath: String?,
        @JsonProperty("job")
        val job: String?
    )
}