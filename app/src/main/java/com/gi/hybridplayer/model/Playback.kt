package com.gi.hybridplayer.model

import java.io.Serializable

data class Playback(var vod: Vod? = null,
                    var portal: Portal? = null,
                    var detail: Vod.Detail? = null,
                    var url:String? = null):Serializable {
    companion object{
        const val PLAYBACK_INTENT_TAG = "playback_intent"

    }
}