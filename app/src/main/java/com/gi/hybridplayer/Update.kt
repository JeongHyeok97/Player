package com.gi.hybridplayer

import android.content.Context
import android.content.ContextWrapper
import android.os.Environment
import java.io.File
import java.io.Serializable

data class Update(
    var versionCode: Int? = null,
    var versionName:String? = null,
    var updateLink:String? = null): Serializable
{
    companion object{
        const val UPDATE_INTENT_TAG = "update_phoenix"
        const val UPDATE_RESULT_TAG = "result_update_phoenix"
        const val UPDATE_RESULT_CODE = 20231013
        const val UPDATE_PROGRESS = "update_phoenix_progress"

        fun getUpdateFile(context: Context): File {
            val wrapper = ContextWrapper(context)
            val dirDownload = wrapper.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
            val file = File(dirDownload, "update.apk")
            return file
        }
    }
}
