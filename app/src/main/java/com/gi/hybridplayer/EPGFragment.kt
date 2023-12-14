package com.gi.hybridplayer

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.TextView
import com.egeniq.androidtvprogramguide.ProgramGuideFragment
import com.egeniq.androidtvprogramguide.ProgramGuideGridView
import com.egeniq.androidtvprogramguide.R
import com.egeniq.androidtvprogramguide.entity.ProgramGuideChannel
import com.egeniq.androidtvprogramguide.entity.ProgramGuideSchedule
import com.gi.hybridplayer.conf.ConnectManager
import com.gi.hybridplayer.db.repository.TvRepository
import com.gi.hybridplayer.model.Channel
import com.gi.hybridplayer.model.Portal
import com.gi.hybridplayer.model.Program
import com.gi.hybridplayer.model.ShortEpg
import com.gi.hybridplayer.viewmodel.ChannelListViewModel
import com.gi.hybridplayer.viewmodel.TvViewModel
import kotlinx.coroutines.*
import org.threeten.bp.LocalDate
import org.threeten.bp.ZoneOffset
import java.util.ConcurrentModificationException
import java.util.concurrent.CopyOnWriteArrayList

class EPGFragment
    : ProgramGuideFragment<Program>() {
    lateinit var mRootActivity: TvActivity
    lateinit var mChannelsViewModel: TvViewModel
    private val mChannels = programGuideManager.channels
    private val mChannelsMap = programGuideManager.channelEntriesMap
    private var mCoroutineScope = CoroutineScope(Dispatchers.IO)
    private lateinit var mZoneId: String
    private var displayNumberView: TextView? = null
    private var titleView: TextView? = null
    private var programTitleView: TextView? = null
    private var metaDataView: TextView? = null
    private var descriptionView: TextView? = null
    private var mSelectedSchedule: ProgramGuideSchedule<Program>? = null
    private var mSelectedChannel : ProgramGuideChannel? = null
    private var mGridView : ProgramGuideGridView<Program>? = null

    private var mConnectManager: ConnectManager? = null
    private var mSetup : Boolean = false
    private var mUpdateJob : Job? =null
    private lateinit var mTvRepository: TvRepository
    private var scheduler:Scheduler? = null





    companion object{
        private const val TAG = "EpgFragment"
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mRootActivity = activity as TvActivity
        mTvRepository = TvRepository.getInstance(requireContext())
        mConnectManager = mRootActivity.getConnectManager()
        scheduler = Scheduler(mRootActivity)

//        mEventRepository = mRootActivity.mEventRepository
        mChannelsViewModel = mRootActivity.getTvViewModel()
        mZoneId = mRootActivity.getProfile()?.defaultTimeZone!!

    }



    val getProgramHandler : Handler = Handler(Looper.getMainLooper())

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        findView(view)
        val startUtcMillis = currentDate.minusDays(3).atStartOfDay().toInstant(
            ZoneOffset.UTC).toEpochMilli()
        val endUtcMillis = currentDate.plusDays(4).atStartOfDay().toInstant(
            ZoneOffset.UTC).toEpochMilli()

        mChannelsViewModel.currentCategory.observe(viewLifecycleOwner){ category->
            val channelList = category.second.toMutableList()
            if (channelList.size > 2000) {
                channelList.subList(2000, channelList.size).clear()
            }

            setState(State.Loading)
            mChannels.clear()
            mChannelsMap.clear()

            if (mUpdateJob?.isActive == true){
                mUpdateJob!!.cancel()
                mCoroutineScope = CoroutineScope(Dispatchers.IO)
            }

            mUpdateJob = mCoroutineScope.launch {
                delay(1000)
                if (channelList.isNotEmpty()){
                    mChannels.addAll(channelList)
                    val setupChannels = mutableMapOf<String, List<ProgramGuideSchedule<Program>>>()
                    val iterator = mChannels.iterator()
                    while(iterator.hasNext()){
                        val it = iterator.next()
                        setupChannels[it.chId] =
                            programGuideManager.createGapList(it.chId.toLong(),
                                startUtcMillis,
                                endUtcMillis,
                                viewportMillis)
                    }
                    mChannelsMap.putAll(setupChannels)
                }
                withContext(Dispatchers.Main){
                    setState(State.Content)
                    programGuideManager.updateChannelsTimeRange(currentDate, DISPLAY_TIMEZONE)
                    programGuideManager.notifySchedulesUpdated()
                }

                try {
                    updatePrograms(startUtcMillis, endUtcMillis)
                }
                catch (e: ConcurrentModificationException){
                    e.printStackTrace()
                }
            }
        }
    }



    private suspend fun updatePrograms(startUtcMillis: Long, endUtcMillis: Long) {
        val iterator = CopyOnWriteArrayList(mChannels)
        var count = 0
        val interval = 13
        iterator.forEach { channel->
            if (channel is Channel){
                val programs = mConnectManager
                    ?.getAllProgramsForChannel(
                        originalNetworkId = channel.chId.toLong(),
                        zoneId = mZoneId)!!.toMutableList()
                if (programs.isNotEmpty()){
                    val firstProgram = programs[0]
                    val lastProgram = programs[programs.size-1]
                    if (firstProgram.startsAtMillis<startUtcMillis){
                        programs.add(0, ProgramGuideSchedule.createGap(channel.chId.toLong(),
                            startUtcMillis,
                            firstProgram.startsAtMillis))
                    }
                    if (lastProgram.endsAtMillis<endUtcMillis){
                        programs.add(programs.size, ProgramGuideSchedule.createGap(channel.chId.toLong(),
                            startUtcMillis,
                            lastProgram.endsAtMillis))
                    }
                    mChannelsMap[channel.chId] = programs
                    update(channelId = channel.chId)

                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        getProgramHandler.removeCallbacksAndMessages(null)
    }


    override fun autoScrollToBestProgramme(
        useTimeOfDayFilter: Boolean,
        specificChannelId: String?
    ) {

        var channelId: String? = null
        if (mSelectedSchedule != null){
            channelId = mSelectedSchedule?.id.toString()
        }
        else{
            channelId = specificChannelId
        }
        super.autoScrollToBestProgramme(useTimeOfDayFilter, channelId)


    }

    private fun findView(view: View){
        mGridView = view.findViewById<ProgramGuideGridView<Program>>((R.id.programguide_grid))
        displayNumberView = view.findViewById(R.id.programguide_channel_number)
        titleView = view.findViewById(R.id.programguide_detail_title)
        programTitleView = view.findViewById(R.id.programguide_detail_program_title)
        metaDataView = view.findViewById(R.id.programguide_detail_metadata)
        descriptionView = view.findViewById(R.id.programguide_detail_description)
    }




    override fun isTopMenuVisible(): Boolean {
        return false
    }

    override fun requestingProgramGuideFor(localDate: LocalDate) {

    }

    override fun requestRefresh() {
        requestingProgramGuideFor(currentDate)
    }

    override fun onScheduleSelected(programGuideSchedule: ProgramGuideSchedule<Program>?) {
        if (programGuideSchedule != null){
            mCoroutineScope.launch {
                mSelectedSchedule = programGuideSchedule
                val channel = mTvRepository.getChannel(mSelectedSchedule?.id!!)
                mSelectedChannel = channel
                withContext(Dispatchers.Main){
                    displayNumberView?.text = channel?.displayNumber
                    titleView?.text = channel?.displayName
                    programTitleView?.text = programGuideSchedule.displayTitle
                    if (programGuideSchedule.isGap){
                        metaDataView?.text = ""
                    }
                    else{
                        metaDataView?.text = programGuideSchedule.originalTimes.toString()
                    }
                }
            }
        }
    }

    override fun onScheduleClicked(programGuideSchedule: ProgramGuideSchedule<Program>) {
        if (!isHidden){
            mCoroutineScope.launch {
                val channel = mTvRepository.getChannel(mSelectedSchedule?.id!!)
                if (channel != null) {
                    withContext(Dispatchers.Main){
                        mRootActivity.tune(channel)
                    }
                }
            }

        }
        else{
            mRootActivity.dPadCenterEvent()
        }
    }


    fun update(channel: ProgramGuideChannel, schedule: ProgramGuideSchedule<Program>){
        var channelIndex = -1
        val list= CopyOnWriteArrayList(mChannels)
        val listIterator = list.iterator()
        while (listIterator.hasNext()){
            val it = listIterator.next()
            if (it.chId == channel.chId){
                channelIndex = list.indexOf(it)
            }
        }
        val programs = CopyOnWriteArrayList(mChannelsMap[channel.chId]!!)
        val programsIterator = programs.iterator()
        var programIndex = -1
        while (programsIterator.hasNext()){
            val it = programsIterator.next()
            if (it.id == schedule.id){
                programIndex = programs.indexOf(it)
            }
        }
//        val programIndex = mChannelsMap[channel.chId]?.indexOf(schedule)!!

        if (channelIndex != -1 && programIndex != -1){
            updateProgram(schedule, channelIndex, programIndex)
        }
    }



    fun requestRemind() {
        if (mSelectedSchedule?.isGap == false){
            val channelIndex = mChannels.indexOf(mSelectedChannel)
            val programIndex = mChannelsMap[mSelectedChannel?.chId]?.indexOf(mSelectedSchedule)!!
            val selectedSchedule = mSelectedSchedule!!
            if (selectedSchedule.startsAtMillis>System.currentTimeMillis()){
                if (channelIndex != -1 && programIndex != -1){
                    val isScheduled = selectedSchedule.isScheduled
                    if (!isScheduled){
                        selectedSchedule.setScheduled(true)
                        selectedSchedule.setScheduleType(1)
                    }
                    else{
                        selectedSchedule.setScheduled(false)
                        selectedSchedule.setScheduleType(-1)
                    }
                    updateProgram(selectedSchedule, channelIndex, programIndex)
                    scheduler?.setSchedule(selectedSchedule, isRecord = false)
                }

            }
        }

    }

    fun requestRecord() {
        if (mSelectedSchedule?.isGap == false){
            val channelIndex = mChannels.indexOf(mSelectedChannel)
            val programIndex = mChannelsMap[mSelectedChannel?.chId]?.indexOf(mSelectedSchedule)!!
            val selectedSchedule = mSelectedSchedule!!
            if (selectedSchedule.startsAtMillis>System.currentTimeMillis()){
                if (channelIndex != -1 && programIndex != -1){
                    val isScheduled = selectedSchedule.isScheduled
                    if (!isScheduled){
                        selectedSchedule.setScheduled(true)
                        selectedSchedule.setScheduleType(0)
                    }
                    else{
                        selectedSchedule.setScheduled(false)
                        selectedSchedule.setScheduleType(-1)
                    }
                    updateProgram(selectedSchedule, channelIndex, programIndex)
                    scheduler?.setSchedule(selectedSchedule, isRecord = true)
                }
            }
        }

    }

    fun setEpgDetail() {
        if (mSelectedSchedule?.isGap == false){
            val originalNetworkId = mSelectedChannel?.chId?.toLong()
            val shortEpgList = mRootActivity.getShortEpg(originalNetworkId!!)
            var shortEpg: ShortEpg? = null
            shortEpgList.forEach {
                if (it.name == mSelectedSchedule?.displayTitle){
                    shortEpg = it
                }
            }
            if (shortEpg != null){

            }
        }
    }


}