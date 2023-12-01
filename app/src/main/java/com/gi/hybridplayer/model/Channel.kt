package com.gi.hybridplayer.model

import android.text.Spanned
import android.text.SpannedString
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import java.io.Serializable
@JsonIgnoreProperties(ignoreUnknown = true)
@Entity(tableName = "channel")
data class Channel(
    @JsonProperty("name")
    var displayName: String? = "N/A",
    @JsonProperty("number")
    var displayNumber: String? = "-1",
    @JsonProperty("id")
    var originalNetworkId: Long? = -1,
    @JsonProperty("tv_genre_id")
    var genreId: String? = "*",
    @JsonProperty("cmd")
    var videoUrl: String? = "",
    @JsonProperty("logo")
    var logoUrl: String? = "",
    @JsonProperty("fav")
    var isFavorite:Boolean = false,
    @JsonProperty("enable_tv_archive")
    var isEnableTvArchive:Boolean = false
): /*ProgramGuideChannel, */Serializable {



    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "_id")
    var id:Long? = null
    var inputId: String? = "null"
    var packageName: String?= "com.gi.hybridplayer"
    var isLocked:Boolean = false


//    override val chId: String
//        get() = originalNetworkId.toString()
//    override val name: Spanned
//        get() = SpannedString(displayName)
//    override val imageUrl: String?
//        get() = logoUrl
}
