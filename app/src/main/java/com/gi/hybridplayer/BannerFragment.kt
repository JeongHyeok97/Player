package com.gi.hybridplayer

import android.annotation.SuppressLint
import android.graphics.PorterDuff
import android.media.tv.TvTrackInfo
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.preference.PreferenceManager
import android.view.LayoutInflater
import android.view.View
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.SeekBar
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.gi.hybridplayer.databinding.FragmentBannerBinding
import com.gi.hybridplayer.model.Channel
import com.gi.hybridplayer.model.ShortEpg
import com.gi.hybridplayer.model.enums.VideoResolution
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.threeten.bp.Instant
import org.threeten.bp.ZoneId
import org.threeten.bp.ZonedDateTime
import org.threeten.bp.format.DateTimeFormatter
import java.text.SimpleDateFormat
import java.util.*

class BannerFragment: Fragment() {

    private lateinit var binding: FragmentBannerBinding
    private lateinit var mRootActivity: TvActivity
    private lateinit var mZoneId: String
    private val mBackgroundScope = CoroutineScope(Dispatchers.IO)

    var bannerOsdTimeout:Long = 5000L
    companion object {
        private const val TAG = "BannerFragment"
        private const val INVALID_NUMBER = "-1"
        const val BANNER_OSD = "banner_in_backstack"
    }

    private var mCurrentChannelId: Long = -1

    private val bannerAnimationHandler: Handler = Handler(Looper.getMainLooper())
    private var timer: Timer? = Timer()
    private var timerTask: TimerTask? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mRootActivity = (requireActivity() as TvActivity)

        mZoneId = mRootActivity.getProfile()?.defaultTimeZone!!
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater,
            R.layout.fragment_banner, container, false)
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        val osdTimeOut = PreferenceManager.getDefaultSharedPreferences(requireContext())
            .getString("SETTINGS_PREFERENCE", "5000")!!.replace(",", "").toLong()
        bannerOsdTimeout = osdTimeOut
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        timerTask = object : TimerTask() {
            override fun run() {
                if (binding.currentProgram != null){
                    binding.progressBar.progress ++
                }
            }
        }
        timer = Timer()
        timer?.scheduleAtFixedRate(timerTask, 0, 1000)
        val viewModel = mRootActivity.getTracks()
        viewModel.currentTracks.observe(viewLifecycleOwner){ formats->
            binding.videoType.visibility = INVISIBLE
            binding.videoType.text = ""
            formats.forEach {
                if (it.sampleMimeType?.startsWith("video") == true){
                    val width = it.width
                    val height = it.height
                    VideoResolution.values().forEach { videoResolution ->
                        if (videoResolution.width == width && videoResolution.height == height){
                            binding.videoType.visibility = VISIBLE
                            binding.videoType.text = videoResolution.name
                        }
                    }
                }
            }
        }
        binding.progressBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (progress == seekBar?.max){
                    updateProgram(originalNetworkId = mCurrentChannelId)
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })
    }

    fun setVideoType(videoInfo: TvTrackInfo){
        var videoResolution: VideoResolution? = null
        binding.videoType.visibility = VISIBLE
        if (videoInfo != null) {
            val videoWidth = videoInfo.videoWidth
            val videoHeight = videoInfo.videoHeight
            for (resolution in VideoResolution.values()) {
                if (videoWidth == resolution.width && videoHeight == resolution.height) {
                    videoResolution = resolution
                    break
                }
            }
        }
        if (videoResolution == null) {
            videoResolution = VideoResolution.SD
        }
        binding.videoType.text = videoResolution.name
    }

    override fun onStop() {
        super.onStop()
        bannerAnimationHandler.removeCallbacksAndMessages(null)
    }

    fun updateBanner(channel: Channel) {
        mCurrentChannelId = channel.originalNetworkId!!
        val sfm = mRootActivity.supportFragmentManager
        bannerAnimationHandler.removeCallbacksAndMessages(null)
        try {
            binding.channelName.text = channel.displayName
            binding.channelNumber.text = channel.displayNumber
            Glide.with(requireContext())
                .load(channel.logoUrl)
                .error(R.drawable.ic_tv)
                .placeholder(R.drawable.ic_tv)
                .centerCrop()
                .into(binding.channelLogo)
            val map = mRootActivity.getCategoryMap()

            if (map.isNotEmpty()){
                binding.channelGroup.text = map[channel.genreId]!!.title
            }
            if (!mRootActivity.isEpgVisible()){
                sfm.beginTransaction().show(this@BannerFragment).commit()
                bannerAnimationHandler.postDelayed({
                    sfm.beginTransaction().hide(this@BannerFragment).commit()
                }, bannerOsdTimeout)
            }

            val isFavor = channel.isFavorite
            val isLocked = channel.isLock
            setFavorite(isFavor)
            setLock(isLocked)
            binding.videoType.visibility = View.INVISIBLE
            updateProgram(originalNetworkId = channel.originalNetworkId!!)
        }
        catch (e:Exception){
            e.printStackTrace()
        }
    }

    private fun releaseBannerProgram(){
        binding.currentProgram = null
        binding.nextProgram = null
        binding.progressBar.progress = 0
        programUpdateHandler.removeCallbacksAndMessages(null)
    }

    private val programUpdateHandler: Handler = Handler(Looper.getMainLooper())

    fun updateProgram(originalNetworkId: Long){
        releaseBannerProgram()

        mBackgroundScope.launch {
            val programs = mRootActivity.getShortEpg(originalNetworkId)
            if (programs.isNotEmpty()){
                val iteratorPrograms = programs.iterator()
                var currentProgram : ShortEpg? = null
                while (iteratorPrograms.hasNext()){
                    val program = iteratorPrograms.next()
                    if (getTimeMillisFromHHmm(program.time!!)<System.currentTimeMillis()/1000
                        && getTimeMillisFromHHmm(program.timeTo!!)>System.currentTimeMillis()/1000){
                        currentProgram = program
                    }
                }

                if (currentProgram != null){
                    if (currentProgram.channelId == mCurrentChannelId){
                        val progress = System.currentTimeMillis()/1000 -getTimeMillisFromHHmm(currentProgram.time!!)
                        programUpdateHandler.postDelayed({
                            binding.currentProgram = currentProgram
                            binding.progressBar.max = currentProgram.duration?.toInt()!!
                            binding.progressBar.progress = progress.toInt()
                            if (programs.indexOf(currentProgram)<programs.size-1){
                                binding.nextProgram = programs[programs.indexOf(currentProgram)+1]
                            }
                        }, 800)
                    }
                }
                else{
                    releaseBannerProgram()
                }
            }
        }

    }


    fun getTimeMillisFromHHmm(timeString: String): Long {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        val date: Date = sdf.parse(timeString)
        val timestamp: Long = date.time
        return timestamp/1000 // 오류가 발생하거나 변환이 실패할 경우 -1L 반환
    }

    fun convertMillisToHumanDate(millis: Long, zoneId: String): String {
        val instant = Instant.ofEpochMilli(millis)
        val zone: ZoneId = ZoneId.of(zoneId)
        val zonedDateTime = ZonedDateTime.ofInstant(instant, zone)

        val formatter = DateTimeFormatter.ofPattern("HH:mm")
        return formatter.format(zonedDateTime)
    }

    fun setFavorite(insert: Boolean) {
        binding.channelFavorite.visibility =
            if (insert){
                VISIBLE
            } else{
                View.GONE
            }
    }

    fun setLock(insert: Boolean) {
        binding.channelLock.visibility =
            if (insert){
                VISIBLE
            } else{
                View.GONE
            }
    }

    fun setRecord(isRecording: Boolean) {
        if (isRecording){
            binding.recordingStateIndicator.visibility = VISIBLE
        }
        else{
            binding.recordingStateIndicator.visibility = View.GONE
        }
    }

    fun setState(state: Int) {
        val stateView = binding.connectionState
        val stateDrawableId: Int? = when (state) {
            0 -> {
                R.drawable.ic_stream_available
            }
            1 -> {
                R.drawable.ic_stream_buffering
            }
            2 -> {
                R.drawable.ic_stream_weak
            }
            else -> {
                null
            }
        }
        if (stateDrawableId != null){
            val stateDrawable = ContextCompat.getDrawable(requireContext(), stateDrawableId)
            stateDrawable?.setColorFilter(
                ContextCompat.getColor(requireContext(), android.R.color.white),
                PorterDuff.Mode.SRC_IN
            )
            stateView.setImageDrawable(stateDrawable)
        }
        else{
            stateView.setImageDrawable(null)
        }
    }

}