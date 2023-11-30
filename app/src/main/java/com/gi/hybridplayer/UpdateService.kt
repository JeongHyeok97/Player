package com.gi.hybridplayer


import android.app.IntentService
import android.content.Intent
import android.os.Bundle
import android.os.ResultReceiver
import okhttp3.*
import java.io.FileOutputStream
import java.io.IOException
import java.net.URL


class UpdateService: IntentService(TAG) {


    companion object{
        const val TAG = "UpdateService"
    }
    private var mUpdate:Update? = null
    private var mUpdateReceiver: ResultReceiver? = null

    override fun onHandleIntent(intent: Intent?) {
        mUpdate = (intent?.getSerializableExtra(Update.UPDATE_INTENT_TAG) as Update?)!!
        mUpdateReceiver = intent?.getParcelableExtra(Update.UPDATE_RESULT_TAG) as ResultReceiver?
        downloadUpdateData(mUpdate?.updateLink!!)

    }

    fun downloadUpdateData(updateUrl:String, cookie:String? = null){
        val client = OkHttpClient.Builder().followRedirects(false).build()
        val builder = if (cookie == null){
            Request.Builder().url(updateUrl)
        }
        else{
            Request.Builder().url(updateUrl).addHeader("Cookie", cookie)
        }
        val request = builder.build()

        client.newCall(request).enqueue(object :Callback{
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {

                if (response.isRedirect){
                    val base = request.url
                    val location = response.header("Location")

                    var result: String? = ""
                    if (location != null){
                        val redirectedUrl = URL(base.toUrl(), location).toString()
                        val cookies = response.headers("Set-Cookie")
                        cookies.forEach {
                            result += it
                        }
                        response.close()
                        downloadUpdateData(redirectedUrl, result)
                    }
                }
                else if (response.isSuccessful){
                    try {
                        val body = response.body
                        val data = body?.byteStream()
                        val length = body?.contentLength()?.toInt()!!
                        val byteArray = ByteArray(length)
                        val file= Update.getUpdateFile(baseContext)
                        val fos = FileOutputStream(file)
                        var isRead = true
                        var total = 0L
                        while (isRead){
                            val read = data?.read(byteArray)!!

                            if (read<=0){
                                isRead = false
                            }
                            else{
                                total += read
                                val resultData = Bundle()
                                resultData.putInt(Update.UPDATE_PROGRESS, (total*100/length).toInt())
                                mUpdateReceiver?.send(Update.UPDATE_RESULT_CODE, resultData)
                                fos.write(byteArray, 0, read)
                            }
                        }
                        data?.close()
                        fos.flush()
                        fos.close()
                        response.close()
                    }
                    catch (e:Exception){
                        e.printStackTrace()
                    }

                }
                else{
                    call.cancel()
                }
            }
        })


    }
}