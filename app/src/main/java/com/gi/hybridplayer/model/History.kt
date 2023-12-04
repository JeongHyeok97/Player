package com.gi.hybridplayer.model

import android.view.View
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "vod_history")
data class History(
    val portalId: Long,
    val videoId: Long,
    val endTime: Long) : Serializable{


    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "_id")
    var id: Long? = null


    var percent: Int? = 0
}