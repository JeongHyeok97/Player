package com.gi.hybridplayer

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.os.Handler
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.egeniq.androidtvprogramguide.entity.ProgramGuideSchedule
import com.gi.hybridplayer.db.repository.TvRepository
import com.gi.hybridplayer.model.Channel
import com.gi.hybridplayer.model.Program
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class Scheduler(context: Context) {

    private val mHandler: Handler
    private val mRoot : TvActivity
    private val mTvRepository: TvRepository
    private val mScheduleList = MutableLiveData<List<Schedule>>()
    private val scheduleList: LiveData<List<Schedule>>
        get() {
            return mScheduleList
        }

    init {
        mRoot = context as TvActivity

        mTvRepository = TvRepository.getInstance(context)
        mHandler = Handler()
        mScheduleList.value = listOf()
        scheduleList.observe(mRoot){
            mHandler.removeCallbacksAndMessages(null)
            it.forEach {schedule->
                mHandler.postDelayed(schedule, schedule.schedule.startsAtMillis - System.currentTimeMillis())
            }
        }
    }


    fun setSchedule(schedule: ProgramGuideSchedule<Program>, isRecord: Boolean){
        val startTime= schedule.startsAtMillis
        val channelId = schedule.id
        CoroutineScope(Dispatchers.IO).launch {
            val channel = mTvRepository.getChannel(schedule.id)
            if (channel != null){
                val runnable = Schedule("${channelId}_$startTime",channel,schedule,isRecord)
                val scheduleList =mScheduleList.value?.toMutableList()
                val iterator = scheduleList?.iterator()
                var remove = false
                while (iterator?.hasNext() == true){
                    val it = iterator.next()
                    if (it.id == runnable.id){
                        iterator.remove()
                        remove = true
                    }
                }
                if (!remove){
                    scheduleList?.add(runnable)
                }
                withContext(Dispatchers.Main){
                    this@Scheduler.mScheduleList.value = scheduleList
                }
            }
        }

    }


    inner class Schedule(
        val id:String,
        val channel: Channel,
        val schedule: ProgramGuideSchedule<Program>,
        private val isRecord: Boolean?) : Runnable{


        override fun run() {
            val originalTimes = schedule.originalTimes
            val title = if (schedule.scheduleType == 1){
                "Remind?"
            }
            else{
                "Recording"
            }

            val message = "Would you like to go to channel ${channel.displayName}($originalTimes) that you reserved?"
            val dialog = AlertDialog.Builder(mRoot)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK ") { _, _ ->
                    try {
                        mRoot.tune(channel = channel)
                        if (isRecord == true) {
                            mRoot.record(channel.originalNetworkId!!)
                        }
                        schedule.setScheduled(false)
                        schedule.setScheduleType(-1)
                        mRoot.updateEpgFragment(channel, schedule)
                        val scheduleList =mScheduleList.value?.toMutableList()
                        val iterator = scheduleList?.iterator()
                        var remove = false
                        while (iterator?.hasNext() == true){
                            val it = iterator.next()
                            if (it.id == id){
                                iterator.remove()
                            }
                        }
                        mScheduleList.value = scheduleList
                    }
                    catch (e:Exception){
                        e.printStackTrace()
                    }

                }
                .setNegativeButton("Cancel"){_,_->mHandler.removeCallbacks(this)}
                .create()
            dialog.show()
            mHandler.postDelayed({
                dialog.getButton(DialogInterface.BUTTON_POSITIVE).performClick()
            }, 5000)
        }
    }
}