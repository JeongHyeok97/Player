package com.gi.hybridplayer.model

data class Program(
    var id: Long? = -1,
    var channelId:Long?,
    var title:String? = "No Information",
    var startTimeMillis:Long?,
    var endTimeMillis: Long?
) {

    companion object {
    }
}