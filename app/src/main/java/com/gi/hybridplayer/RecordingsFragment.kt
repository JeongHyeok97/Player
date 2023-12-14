package com.gi.hybridplayer

import android.content.Intent
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.leanback.app.BrowseSupportFragment
import androidx.leanback.widget.*
import com.gi.hybridplayer.SimplePlaybackActivity.SimplePlayback
import com.gi.hybridplayer.conf.DeviceManager
import com.gi.hybridplayer.db.repository.TvRepository
import com.gi.hybridplayer.model.RecordedProgram
import com.gi.hybridplayer.view.VodCardPresenter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class RecordingsFragment : BrowseSupportFragment() {

    var arrayObjectAdapter: ArrayObjectAdapter? = null


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        prepareEntranceTransition()
        setupUIElements()
        arrayObjectAdapter = ArrayObjectAdapter(ListRowPresenter())
        adapter = arrayObjectAdapter

        setRecordingsElement()
        setEventListeners()
    }

    private fun setEventListeners() {
        val dateFormat = SimpleDateFormat("yy-MM-dd  ~HH:mm")
        onItemViewClickedListener =
            OnItemViewClickedListener { _, item, _, _ ->
                if (item is RecordedProgram){
                    val subtitle = dateFormat.format(Date(item.startTimeMillis!! + item.duration!!))
                    val intent = Intent(requireActivity(), SimplePlaybackActivity::class.java)
                    val bundle = Bundle()
                    bundle.putString(SimplePlayback.SIMPLE_PLAYBACK_TITLE, item.title)
                    bundle.putString(SimplePlayback.SIMPLE_PLAYBACK_SUBTITLE, subtitle)
                    bundle.putString(SimplePlayback.SIMPLE_PLAYBACK_URL, item.dataUri)
                    bundle.putString(SimplePlayback.SIMPLE_PLAYBACK_VIDEO_TYPE, "file")
                    intent.putExtra(SimplePlayback.SIMPLE_PLAYBACK_BUNDLE, bundle)
                    startActivity(intent)
                }
            }
        onItemViewSelectedListener =
            OnItemViewSelectedListener { _, item, _, _ ->
            }
    }

    private fun setupUIElements() {
        headersState = HEADERS_HIDDEN
        isHeadersTransitionOnBackEnabled = true
        brandColor = ContextCompat.getColor(requireActivity(), R.color.red_gray)
    }





    fun setRecordingsElement() {
        val dataPath = DeviceManager.getUsbStorage(requireContext())
        val backgroundScope = CoroutineScope(Dispatchers.IO)
        val repo = TvRepository.getInstance(requireContext())
        val programs: MutableList<RecordedProgram> = ArrayList<RecordedProgram>()
        backgroundScope.launch {
            if (dataPath != null){
                val dir = File(dataPath)
                if (dir.exists() && dir.isDirectory){
                    dir.listFiles()?.let {it ->
                        it.forEach { file->
                            val fileName = file.name
                            val parts = fileName.split("_")
                            val duration = getVideoDuration(file.absolutePath)
                            if (parts.size == 2){
                                val channelId = parts[0]
                                val recordingTime = parts[1]
                                val channel = repo.getChannel(channelId.toLong())
                                val recordedProgram = RecordedProgram(
                                    title = channel?.displayName,
                                    startTimeMillis = recordingTime.toLong(),
                                    duration = duration,
                                    channelLogo = channel?.logoUrl,
                                    dataUri = file.absolutePath)
                                if (recordedProgram.duration != -1L){
                                    programs.add(recordedProgram)
                                }
                            }
                        }
                    }
                }
            }
            val rAdapter = ArrayObjectAdapter(VodCardPresenter())
            withContext(Dispatchers.Main){
                rAdapter.addAll(0, programs)
                val row = ListRow(HeaderItem(0, "Recordings"), rAdapter)
                arrayObjectAdapter!!.add(row)
            }
        }
    }

    fun getVideoThumbnail(filePath: String): Bitmap? {
        return try {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(filePath)
            val bitmap: Bitmap? = retriever.getFrameAtTime(50000000,
                MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
            retriever.release()
            bitmap
        } catch (e:Exception){
            null
        }
    }



    private fun getVideoDuration(filePath: String): Long {
        return try {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(filePath)
            val durationString = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            val duration: Long = durationString?.toLong() ?: 0
            retriever.release()
            duration
        } catch (e:Exception){
            -1
        }
    }

    companion object {
        private const val TAG = "RecordingFragment"
    }
}