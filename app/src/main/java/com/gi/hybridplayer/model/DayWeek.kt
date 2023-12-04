package com.gi.hybridplayer.model

import com.fasterxml.jackson.annotation.JsonProperty

data class DayWeek(
    @JsonProperty("f_human")
    var humanDate: String? = null,
    @JsonProperty("f_mysql")
    var sqlDate: String? = null,
    @JsonProperty("today")
    var isToday: String? = null
) {


}