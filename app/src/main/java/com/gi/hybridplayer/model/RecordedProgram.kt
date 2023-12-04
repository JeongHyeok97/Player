package com.gi.hybridplayer.model

import android.graphics.Bitmap

data class RecordedProgram(
    var title:String? = "No Information",
    var startTimeMillis:Long?,
    var duration:Long? = -1,
    var channelLogo: String? = null,
    var dataUri: String?
)