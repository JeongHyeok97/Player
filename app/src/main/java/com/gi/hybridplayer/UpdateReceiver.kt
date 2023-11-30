package com.gi.hybridplayer

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.ResultReceiver
import androidx.core.content.ContextCompat.startActivity
import androidx.core.content.FileProvider

class UpdateReceiver(private val context: Context, handler: Handler): ResultReceiver(handler) {
    companion object {
        private const val TAG: String = "UpdateReceiver"
        val AUTHORITY = java.lang.String.format("%s.%s", BuildConfig.APPLICATION_ID, "FileProvider")
    }
    override fun onReceiveResult(resultCode: Int, resultData: Bundle?) {
        super.onReceiveResult(resultCode, resultData)
        if (resultCode == Update.UPDATE_RESULT_CODE){
            val progress= resultData?.getInt(Update.UPDATE_PROGRESS)!!
            if (progress == 100){
                val file = Update.getUpdateFile(context)
                val fileUri = FileProvider.getUriForFile(context, AUTHORITY, file)
                val intent = Intent(Intent.ACTION_VIEW)
                intent.setDataAndType(fileUri, "application/vnd.android.package-archive")
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                context.startActivity(intent)
            }
        }
    }


}
