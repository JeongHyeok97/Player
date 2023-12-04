package com.gi.hybridplayer.model

import java.io.Serializable

data class VodCategory(
    var categoryID: String? = null,
    var categoryStr: String? = null,
    var alias: String? = null,
    var isCensored: Boolean? = false,
) : Serializable {

}
