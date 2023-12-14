@file:Suppress("DEPRECATION")

package com.gi.hybridplayer

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.transition.TransitionManager
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.ViewModelProvider
import com.egeniq.androidtvprogramguide.entity.ProgramGuideSchedule
import com.gi.hybridplayer.conf.ConnectManager
import com.gi.hybridplayer.conf.DeviceManager
import com.gi.hybridplayer.databinding.ActivityTvBinding
import com.gi.hybridplayer.databinding.DialogAvBinding
import com.gi.hybridplayer.databinding.NumberTunerBinding
import com.gi.hybridplayer.db.repository.PortalRepository
import com.gi.hybridplayer.db.repository.TvRepository
import com.gi.hybridplayer.model.*
import com.gi.hybridplayer.view.AudioDialogAdapter
import com.gi.hybridplayer.view.BaseDialog
import com.gi.hybridplayer.view.TextMatchDialog
import com.gi.hybridplayer.viewmodel.AudioViewModel
import com.gi.hybridplayer.viewmodel.TvViewModel
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.ui.StyledPlayerView
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource
import com.jakewharton.threetenabp.AndroidThreeTen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File


class TvActivity : FragmentActivity() {



    private lateinit var activityTvBinding: ActivityTvBinding
    private lateinit var mTvView: StyledPlayerView
    private lateinit var mNumberTuner: NumberTunerBinding
    private var mTvPlayer: ExoPlayer? = null
    private lateinit var mRepository: TvRepository
    private lateinit var mConnectedPortal: Portal
    private lateinit var mConnectManager: ConnectManager
    private lateinit var mTvViewModel:TvViewModel
    private lateinit var mTracksViewModel: AudioViewModel

    private val backgroundSetupScope = CoroutineScope(Dispatchers.IO)
    private var mProfile: Profile? = null
    private var mCategoryMap: Map<String, Category> = mutableMapOf()
    private var mCurrentChannel: Channel? = null
    private var mPreviousChannel:Channel? = null
    private var mCurrentCategory:Category? = null
    private var mCurrentChannels: List<Channel> = listOf()
    private lateinit var mBannerFragment:BannerFragment
    private lateinit var mChannelListFragment: ChannelListFragment
    private lateinit var mCategoryFragment: CategoryFragment
    private lateinit var mEpgFragment: EPGFragment
    private var menuFragment: MenuFragment? = null
    private var setup:Boolean = false
    private var mHistory = mutableListOf<Channel>()

    private val numberHandler: Handler = Handler(Looper.getMainLooper())
    companion object{
        private val TAG: String = TvActivity::class.java.name
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AndroidThreeTen.init(this)
        activityTvBinding = ActivityTvBinding.inflate(layoutInflater)
        setContentView(activityTvBinding.root)
        mTvView = activityTvBinding.tvView
        mNumberTuner = activityTvBinding.numberTuner
        initPlayer()
        mRepository=TvRepository.getInstance(this@TvActivity)
        mConnectedPortal = intent.getSerializableExtra(Portal.PORTAL_INTENT_TAG) as Portal
        mConnectManager = ConnectManager(portal = mConnectedPortal)
        mTvViewModel = ViewModelProvider(this)[TvViewModel::class.java]
        mTracksViewModel = ViewModelProvider(this)[AudioViewModel::class.java]
        mTvViewModel.currentChannel.observe(this){
            mTracksViewModel.setCurrentTracks(listOf())
            mBannerFragment.setState(3)
            if (mHistory.size>10){
                mHistory.removeAt(0)
            }
            mHistory.add(it)
            mCurrentChannel = it
            mBannerFragment.updateBanner(it)
            if (it.isLock){
                authentication(object : TextMatchDialog.OnSuccessListener{
                    override fun onSuccess() {
                        val url =it.videoUrl
                        backgroundSetupScope.launch {
                            setVideoUrl(url!!)
                            mConnectManager.setLastId(it.originalNetworkId)
                        }
                    }
                })
            }
            else{
                val url =it.videoUrl
                backgroundSetupScope.launch {
                    setVideoUrl(url!!)
                    mConnectManager.setLastId(it.originalNetworkId)
                }
            }
        }
        mTvViewModel.currentCategory.observe(this){
            mCurrentCategory = it.first
            mCurrentChannels = it.second
        }

        backgroundSetupScope.launch {
            mProfile = mConnectManager.getProfile()
            mConnectManager.doAuth(mConnectedPortal.user_ID, mConnectedPortal.user_PW)
            mProfile = mConnectManager.getProfile()
            mCategoryMap = mConnectManager.getTvGenres().first
            val allChannels = mRepository.getChannels()
            if (allChannels.isEmpty()){
                val portalRepository = PortalRepository(this@TvActivity)
                mConnectedPortal.connected = false
                portalRepository.update(mConnectedPortal)
                withContext(Dispatchers.Main){
                    Toast.makeText(this@TvActivity,
                        "Channel data did not load successfully", Toast.LENGTH_LONG).show()
                }
                val intent = Intent(this@TvActivity, MainActivity::class.java)
                startActivity(intent)
            }
            else{
                val lastChannelId = mProfile?.lastItvId
                val lastChannel = if (lastChannelId != null && mRepository.getChannel(lastChannelId) != null){
                    mRepository.getChannel(lastChannelId)
                } else{
                    allChannels[0]
                }
                val lastCategory = mCategoryMap[lastChannel?.genreId?:"*"]
                val list = mRepository.findListByChannel(lastCategory?.id!!)
                withContext(Dispatchers.Main){
                    mBannerFragment = BannerFragment()
                    mCategoryFragment = CategoryFragment(lastCategory.id, mCategoryMap.values.toList())
                    mChannelListFragment = ChannelListFragment(lastCategory)
                    mEpgFragment = EPGFragment()
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.category_container, mCategoryFragment)
                        .replace(R.id.channel_container, mChannelListFragment)
                        .replace(R.id.banner_container, mBannerFragment)
                        .replace(R.id.epg_container, mEpgFragment)
                        .commit()
                    supportFragmentManager.executePendingTransactions()
                    supportFragmentManager.beginTransaction().hide(mEpgFragment).commit()
                    mTvViewModel.setCurrentChannel(lastChannel)
                    mTvViewModel.setCurrentCategory(Pair(lastCategory, list))
                    setup = true
                    activityTvBinding.startLoading.visibility= INVISIBLE
            }
            }
        }
    }
    private fun isChannelListVisible(): Boolean {
        return mChannelListFragment.view?.visibility == VISIBLE
    }

    fun dPadCenterEvent(){
        if (!mBannerFragment.isHidden){
            supportFragmentManager.beginTransaction().hide(mBannerFragment).commitNow()
            supportFragmentManager.executePendingTransactions()
        }
        if (!isChannelListVisible()){
            if (mNumberTuner.root.visibility == VISIBLE){
                putNumber(0)
            }
            else{
                mChannelListFragment.requestFocus(mCurrentChannel)
                mChannelListFragment.view?.visibility = VISIBLE
                mChannelListFragment.view?.requestLayout()
                mChannelListFragment.requestFocus(mCurrentChannel)
            }
        }
    }
    private fun initPlayer(){
        val factory = DefaultRenderersFactory(this)
        factory.setExtensionRendererMode(DefaultRenderersFactory.EXTENSION_RENDERER_MODE_ON)
        val parameters = DefaultTrackSelector.ParametersBuilder()
            .setPreferredAudioLanguage("mul")
            .setPreferredAudioLanguage("de")
            .build()
        val trackSelector = DefaultTrackSelector(this, parameters)
        val loadControl = DefaultLoadControl.Builder()
            .setBufferDurationsMs(10000,
                20000,
                DefaultLoadControl.DEFAULT_BUFFER_FOR_PLAYBACK_MS,
                DefaultLoadControl.DEFAULT_BUFFER_FOR_PLAYBACK_AFTER_REBUFFER_MS)
            .setTargetBufferBytes(DEFAULT_BUFFER_SIZE*DEFAULT_BUFFER_SIZE)
            .setPrioritizeTimeOverSizeThresholds(true)
            .build()
        val bandwidthMeter = DefaultBandwidthMeter.Builder(this).build()
        val builder = ExoPlayer.Builder(this)
        mTvPlayer = builder.setTrackSelector(trackSelector)
            .setRenderersFactory(factory)
            .setLoadControl(loadControl)
            .setBandwidthMeter(bandwidthMeter)
            .build()
        mTvPlayer?.addListener(object : Player.Listener{
            override fun onPlayerError(error: PlaybackException) {
                mTvPlayer?.prepare()
                mBannerFragment.setState(3)
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                super.onPlaybackStateChanged(playbackState)
                when (playbackState) {
                    Player.STATE_READY -> {
                        val formats = getFormats()
                        mTracksViewModel.setCurrentTracks(formats)
                        mBannerFragment.setState(0)
                    }
                    Player.STATE_BUFFERING -> {
                        mTvPlayer?.prepare()
                        mBannerFragment.setState(2)
                    }
                    Player.STATE_ENDED -> {
                        mBannerFragment.setState(3)
                        mTvPlayer?.prepare()
                    }
                    Player.STATE_IDLE -> {
                        mBannerFragment.setState(3)
                        mTvPlayer?.prepare()
                        mTvPlayer?.play()
                    }
                }
            }
        })
        mTvView.player = mTvPlayer
    }

    private suspend fun setVideoUrl(url:String){
        val channelUrl = if (url.contains("localhost")){
            mConnectManager.createLink(
                type = ConnectManager.TYPE_ITV,
                cmd = url,
            )!!
        } else{
            url.substring(url.indexOf("http"), url.length)
        }
        withContext(Dispatchers.Main){
            try {
                val mediaSource = getMediaSource(channelUrl)
                mTvPlayer?.setMediaSource(mediaSource)
                mTvPlayer?.playWhenReady = true
                mTvPlayer?.prepare(mediaSource)
            }
            catch (e:IllegalStateException){
                e.printStackTrace()
            }
        }
    }



    private fun getMediaSource(
        streamUrl: String
    ): MediaSource {
        val factory = DefaultHttpDataSource.Factory()
        val upstreamFactory: DataSource.Factory =
            LiveDataSource.Factory(this, factory)
        val mediaItem = MediaItem.fromUri(Uri.parse(streamUrl))
        return if (streamUrl.contains(".m3u8")){
            val m3u8Factory = HlsMediaSource.Factory(upstreamFactory)
            m3u8Factory.createMediaSource(mediaItem)
        } else{
            val progressiveFactory =
                ProgressiveMediaSource.Factory(upstreamFactory)
            progressiveFactory.createMediaSource(mediaItem)
        }
    }
    fun isTvRecording(): Boolean? {
        return LiveDataSource.isRecording
    }
    fun setRecording(recording: Boolean){
        val usbStorage = DeviceManager.getUsbStorage(this)
        if (usbStorage != null){
            val filePath = "${mCurrentChannel?.originalNetworkId}_${System.currentTimeMillis()}"
            val file = File(usbStorage, filePath)
            LiveDataSource.setRecording(recording, file)
            mBannerFragment.setRecord(recording)
            if (recording){
                Toast.makeText(this,
                    "Start Recording Channel ${mCurrentChannel?.displayName}",
                    Toast.LENGTH_LONG).show()
            }
            else{
                Toast.makeText(this,
                    "Stop Recording Channel ${mCurrentChannel?.displayName}",
                    Toast.LENGTH_LONG).show()
            }

        }
        else{
            Toast.makeText(this,
                resources.getString(R.string.recording_no_usb),
                Toast.LENGTH_LONG).show()
        }

    }

    override fun onRestart() {
        super.onRestart()
        if (mTvPlayer == null){
            if (mCurrentChannel != null){
//                tune(mCurrentChannel!!)
                initPlayer()
                mTvViewModel.setCurrentChannel(channel = mCurrentChannel)
            }
//            mTvPlayer?.prepare()
        }
        backgroundSetupScope.launch {
            try {
                mProfile = mConnectManager.getProfile()
            }
            catch (e: Exception){
                e.printStackTrace()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (mTvPlayer == null){
            if (mCurrentChannel != null){
//                tune(mCurrentChannel!!)
                initPlayer()
                mTvViewModel.setCurrentChannel(channel = mCurrentChannel)
            }
//            mTvPlayer?.prepare()
        }
        backgroundSetupScope.launch {
            try {
                mProfile = mConnectManager.getProfile()
            }
            catch (e: Exception){
                e.printStackTrace()
            }
        }
    }

    override fun onPause() {
        super.onPause()
        mTvPlayer?.stop()
        mTvPlayer?.release()
        mTvPlayer = null
    }


    override fun onStop() {
        super.onStop()
//        mTvPlayer?.setVideoSurface(null)
        mTvPlayer?.stop()
        mTvPlayer?.release()
        mTvPlayer = null
    }


    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
//        Toast.makeText(this, "$keyCode -> $event", Toast.LENGTH_SHORT).show()
        if (setup){
            if (isTvRecording() == false){
                if (!isChannelListVisible() && !isEpgVisible()){
                    if (keyCode == KeyEvent.KEYCODE_CHANNEL_UP || keyCode == KeyEvent.KEYCODE_CHANNEL_DOWN ||
                        keyCode == KeyEvent.KEYCODE_PAGE_UP || keyCode == KeyEvent.KEYCODE_PAGE_DOWN){
                        val list= mTvViewModel.currentCategory.value?.second!!
                        var index = -1
                        list.forEach {
                            if (it.originalNetworkId == mCurrentChannel?.originalNetworkId){
                                index = list.indexOf(it)
                            }
                        }
                        if (keyCode == KeyEvent.KEYCODE_CHANNEL_UP || keyCode == KeyEvent.KEYCODE_PAGE_UP){
                            mCurrentChannel =
                                if (index<list.size-1){
                                    list[index+1]
                                } else{
                                    list[0]
                                }
                        }
                        else if (keyCode == KeyEvent.KEYCODE_CHANNEL_DOWN ||keyCode == KeyEvent.KEYCODE_PAGE_DOWN){
                            mCurrentChannel = if (index>0){
                                list[index-1]
                            }
                            else{
                                list[list.size-1]
                            }
                        }
                        mBannerFragment.updateBanner(mCurrentChannel!!)
                    }
                }
            }
            else{
                if (keyCode == KeyEvent.KEYCODE_CHANNEL_UP || keyCode == KeyEvent.KEYCODE_CHANNEL_DOWN ||
                    keyCode == KeyEvent.KEYCODE_PAGE_UP || keyCode == KeyEvent.KEYCODE_PAGE_DOWN){
                    val dialog = AlertDialog.Builder(this)
                        .setTitle("Stop Recording?")
                        .setMessage("Do you want to stop recording?")
                        .setPositiveButton("OK "
                        ) { _, _ ->
                            setRecording(false)
                        }
                        .create()
                    dialog.show()
                }

            }

        }

        return super.onKeyDown(keyCode, event)
    }

    fun authentication(listener: TextMatchDialog.OnSuccessListener){
        val dialog = TextMatchDialog(this, listener)
        dialog.setContentView(R.layout.dialog_authentication)
        dialog.setMainText(R.id.input_password)
        dialog.setTargetText(mProfile?.parentPassword)
        dialog.show()
    }


    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
//        Toast.makeText(this, "$event", Toast.LENGTH_LONG).show()
        if (setup){
            if (mEpgFragment.isHidden){
                if (keyCode == KeyEvent.KEYCODE_BACK){
                    if (supportFragmentManager.backStackEntryCount>0){
                        supportFragmentManager.popBackStack()
                    }
                    else{
                        if (mCategoryFragment.view?.visibility == VISIBLE){
                            hideCategory()
                            mCategoryFragment.scrollLast(mCurrentChannel!!.genreId!!)
                        }
                        else if (isChannelListVisible()){
                            hideChannelList()
                            mCategoryFragment.scrollLast(mCurrentChannel!!.genreId!!)
                        }
                        else if (mNumberTuner.root.visibility == VISIBLE){
                            numberHandler.removeCallbacksAndMessages(null)
                            setChannelNumber(null)
                        }
                        else if (!mBannerFragment.isHidden){
                            supportFragmentManager.beginTransaction().hide(mBannerFragment)
                                .commit()
                        }
                        else{
                            if (mHistory.size>1){
                                val previous = mHistory[mHistory.size-2]
                                tune(previous)
                            }
                        }
                    }
                    return true
                }
                if (keyCode == KeyEvent.KEYCODE_CHANNEL_UP || keyCode == KeyEvent.KEYCODE_CHANNEL_DOWN ||
                    keyCode == KeyEvent.KEYCODE_PAGE_UP || keyCode == KeyEvent.KEYCODE_PAGE_DOWN){
                    if (!isChannelListVisible()){
                        if (isTvRecording() == false){
                            mTvViewModel.setCurrentChannel(mCurrentChannel)
                        }
                    }
                }
                else if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT){
                    if (mCategoryFragment.view?.visibility == View.INVISIBLE && isChannelListVisible()){
                        mCategoryFragment.view?.visibility = VISIBLE
                        setCategoryAnimator(false)
                        mCategoryFragment.view?.requestFocus()
                    }
                }
                else if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT){
                    if (mCategoryFragment.view?.visibility == VISIBLE){
                        hideCategory()
                    }
                    else if (!mBannerFragment.isHidden){
                        val intent = Intent(this, SingleEpgActivity::class.java)
                        intent.putExtra(Portal.PORTAL_INTENT_TAG, mConnectedPortal)
                        intent.putExtra(Channel.CHANNEL_INTENT_TAG, mCurrentChannel?.originalNetworkId!!)
                        startActivity(intent)
                    }
                }
                else if (keyCode == KeyEvent.KEYCODE_MENU){
                    hideCategory()
                    hideChannelList()

                    val fragmentManager = supportFragmentManager
                    val existingFragment = fragmentManager.findFragmentById(R.id.tv_root)
                    if (existingFragment !is MenuFragment) {
                        val transaction = fragmentManager.beginTransaction()
                        if (menuFragment == null) {
                            menuFragment = MenuFragment(mConnectedPortal)
                        }
                        transaction.replace(R.id.tv_root, menuFragment!!)
                            .addToBackStack(null)
                            .hide(mBannerFragment)

                            .commit()
                    }
                }
                else if (keyCode == KeyEvent.KEYCODE_INFO || (keyCode == KeyEvent.KEYCODE_UNKNOWN && event?.scanCode == 358)){
                    if (isChannelListVisible()){
                        mChannelListFragment.view?.visibility = View.INVISIBLE
                        mChannelListFragment.view?.requestLayout()
                        hideCategory()
                    }
                    val transaction: FragmentTransaction =
                        if (mBannerFragment.isHidden){
                            supportFragmentManager.beginTransaction().show(mBannerFragment)
                        } else{
                            supportFragmentManager.beginTransaction().hide(mBannerFragment)
                        }
                    transaction.commitNow()
                }
                else if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER){
                    dPadCenterEvent()
                }
                else if (keyCode == KeyEvent.KEYCODE_GUIDE || (keyCode == KeyEvent.KEYCODE_UNKNOWN && event?.scanCode == 365)){
                    mEpgFragment.scrollToChannelWithId(mCurrentChannel?.originalNetworkId.toString())
                    supportFragmentManager.beginTransaction()
                        .hide(mBannerFragment)
                        .show(mEpgFragment).commit()
                    activityTvBinding.epgContainer.visibility = VISIBLE
                    setTvViewScale(true)
                }
                else if (keyCode == KeyEvent.KEYCODE_PROG_YELLOW){
                    if (mCurrentChannel != null){
                        mCurrentChannel?.isFavorite = mCurrentChannel?.isFavorite == false
                        mBannerFragment.setFavorite(mCurrentChannel?.isFavorite!!)
                        backgroundSetupScope.launch {
                            mRepository.updateChannel(mCurrentChannel!!)
                            val favoriteChannels = mRepository.getFavoriteChannels()
                            val idList = mutableListOf<Long>()
                            favoriteChannels.forEach {
                                idList.add(it.originalNetworkId!!)
                            }
                            mConnectManager.setFav(idList)
                        }
                    }

                }
                else if (keyCode == KeyEvent.KEYCODE_PROG_GREEN){
                    if (!mBannerFragment.isHidden){
                        val audioDialog = BaseDialog(this)
                        val binding: DialogAvBinding = DataBindingUtil.inflate(
                            LayoutInflater.from(this),
                            R.layout.dialog_av, null, false)
                        binding.viewModel = mTracksViewModel
                        binding.listener = object : AudioDialogAdapter.Listener{
                            override fun onClick(item: Any) {
                                if (item is Format){
                                    val mimType = item.sampleMimeType
                                    if (mimType?.startsWith("audio") == true){
                                        if (item.language != null){
                                            selectLanguage(item.language!!)
                                        }
                                    }
                                }
                            }
                        }

                        audioDialog.bindContentView(binding.root)
                        audioDialog.setWidth(resources.getDimensionPixelSize(R.dimen.prefs_dialog_width))

                        audioDialog.show()

                    }
                }
                else if (keyCode == KeyEvent.KEYCODE_PROG_RED){
                    if (isTvRecording() == true){
                        setRecording(false)
                    }
                    else{
                        setRecording(true)
                    }
                }
                else if (keyCode == KeyEvent.KEYCODE_PROG_BLUE){
                    if (mCurrentChannel?.isLock == true){
                        authentication(object :TextMatchDialog.OnSuccessListener{
                            override fun onSuccess() {
                                mCurrentChannel?.isLock = false
                                mBannerFragment.setLock(false)
                                backgroundSetupScope.launch {
                                    mRepository.updateChannel(mCurrentChannel!!)
                                }
                            }
                        })
                    }
                    else{
                        setLock()
                    }
                }
                else if (keyCode >= KeyEvent.KEYCODE_0 && keyCode <= KeyEvent.KEYCODE_9){

                    if (!isChannelListVisible()){
                        numberHandler.removeCallbacksAndMessages(null)
                        mNumberTuner.root.requestFocus()
                        val inputNumber: String = (keyCode - 7).toString()
                        if (mNumberTuner.root.visibility == View.GONE && mNumberTuner.tunerDisplayNumber.text == ""){
                            mNumberTuner.root.visibility = VISIBLE
                            mNumberTuner.tunerDisplayNumber.text = inputNumber
                            putNumber(3000)
                        }
                        else if (mNumberTuner.root.visibility == VISIBLE && mNumberTuner.tunerDisplayNumber.text != ""){
                            if (mNumberTuner.tunerDisplayNumber.text != "0"){
                                val n = mNumberTuner.tunerDisplayNumber.text.toString() + inputNumber
                                mNumberTuner.tunerDisplayNumber.text = n
                                putNumber(3000)
                            }
                            else{
                                setChannelNumber(null)
                            }
                        }
                    }
                }
            }
            else{
                if (keyCode == KeyEvent.KEYCODE_BACK ||
                    keyCode == KeyEvent.KEYCODE_GUIDE ||
                    (keyCode == KeyEvent.KEYCODE_UNKNOWN && event?.scanCode == 365)){
                    supportFragmentManager.beginTransaction().hide(mEpgFragment).commit()
                    setTvViewScale(false)
                    return true
                }
                else if (keyCode == KeyEvent.KEYCODE_PROG_YELLOW){
                    mEpgFragment.autoScrollToBestProgramme()
                }
                else if (keyCode == KeyEvent.KEYCODE_PROG_GREEN){
                    mEpgFragment.requestRemind()
                }
                else if (keyCode == KeyEvent.KEYCODE_PROG_RED){
                    mEpgFragment.requestRecord()
                }
                else if (keyCode == KeyEvent.KEYCODE_PROG_BLUE){
                    mEpgFragment.setEpgDetail();
                }
            }

        }
        return super.onKeyUp(keyCode, event)
    }

    private fun setLock() {
        mCurrentChannel?.isLock = true
        mBannerFragment.setLock(true)
        backgroundSetupScope.launch {
            mRepository.updateChannel(mCurrentChannel!!)
        }
    }

    private fun hideChannelList(){
        mChannelListFragment.view?.visibility = View.INVISIBLE
    }


    fun hideCategory(){
        setCategoryAnimator(true)
        mCategoryFragment.view?.visibility = View.INVISIBLE
    }
    fun getCategoryMap(): Map<String, Category> {
        return mCategoryMap
    }
    fun getProfile():Profile?{
        return mProfile
    }

    fun getShortEpg(originalNetworkId: Long): MutableList<ShortEpg> {
        return mConnectManager.getShortEpg(originalNetworkId)
    }

    fun tune(channel: Channel){
        tune(channel, null, null)
    }

    fun tune(channel:Channel, category: Category?, list: List<Channel>?){
        if (isTvRecording() == true){
            val dialog = AlertDialog.Builder(this)
                .setTitle("Stop Recording?")
                .setMessage("Do you want to stop recording?")
                .setPositiveButton("OK "
                ) { _, _ ->
                    setRecording(false)
                    tune(channel,category,list)
                }
                .create()
            dialog.show()
            Handler().postDelayed({dialog.getButton(DialogInterface.BUTTON_POSITIVE).performClick()},
                5000)
        }
        else{
            if (channel != mCurrentChannel){
                mTvViewModel.setCurrentChannel(channel)
                if (list != null){
                    if (category is Category){
                        if (mCurrentCategory?.id !=category.id){
                            mTvViewModel.setCurrentCategory(Pair(category, list))
                        }
                    }
                }
            }

        }
    }

    fun isEpgVisible(): Boolean {
        return false
    }
    private fun setCategoryAnimator(hidden: Boolean){
        val parentLayout = activityTvBinding.tvRoot
        val categoryId = R.id.category_container
        val constraintSet = ConstraintSet()
        try {
            if (hidden){
                constraintSet.clone(parentLayout)
                constraintSet.clear(categoryId, ConstraintSet.START)
                constraintSet.clear(categoryId, ConstraintSet.END)
                constraintSet.connect(
                    categoryId,
                    ConstraintSet.END,
                    ConstraintSet.PARENT_ID,
                    ConstraintSet.START
                )
                constraintSet.applyTo(parentLayout)
            }
            else{
                constraintSet.clone(parentLayout)
                constraintSet.clear(categoryId, ConstraintSet.START)
                constraintSet.clear(categoryId, ConstraintSet.END)
                constraintSet.connect(
                    categoryId,
                    ConstraintSet.START,
                    ConstraintSet.PARENT_ID,
                    ConstraintSet.START
                )
                TransitionManager.beginDelayedTransition(parentLayout)
                constraintSet.applyTo(parentLayout)
            }
        }
        catch (e: RuntimeException){
            e.printStackTrace()
        }

    }

    fun exit() {
        val exitDialog = BaseDialog(this)
        exitDialog.setContentView(R.layout.exit_layout)
        exitDialog.setNegativeButton(R.id.frmNo
        ) {
            exitDialog.dismiss()
        }
        exitDialog.setNegativeButton(R.id.frmOk){
            exitDialog.dismiss()
            finishAndRemoveTask()
        }
        exitDialog.show()
    }

    fun updateEpgFragment(channel: Channel, schedule: ProgramGuideSchedule<Program>) {
        mEpgFragment.update(channel, schedule)
    }

    fun record() {
        setRecording(true)
    }

    fun getConnectManager():ConnectManager{
        return mConnectManager
    }
    fun getTvViewModel(): TvViewModel {
        return mTvViewModel
    }
    private fun setTvViewScale(isHidden: Boolean) {
        val layoutParams = activityTvBinding.tvView.layoutParams as ConstraintLayout.LayoutParams
        if (isHidden) {
            layoutParams.width = resources.getDimensionPixelSize(R.dimen.tv_view_scaled_width)
            layoutParams.height = resources.getDimensionPixelSize(R.dimen.tv_view_scaled_height)
            layoutParams.topToTop = ConstraintLayout.LayoutParams.PARENT_ID
            layoutParams.startToStart = ConstraintLayout.LayoutParams.PARENT_ID
            layoutParams.leftMargin =
                resources.getDimensionPixelSize(R.dimen.tv_view_scaled_margin_left)
            layoutParams.topMargin =
                resources.getDimensionPixelSize(R.dimen.tv_view_scaled_margin_top)
            activityTvBinding.bannerContainer.visibility = View.GONE
            activityTvBinding.categoryContainer.visibility = View.GONE
            activityTvBinding.channelContainer.visibility = View.GONE
        } else {
            supportFragmentManager.beginTransaction().hide(mEpgFragment).commit()
            layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
            layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
            layoutParams.leftMargin = 0
            layoutParams.topMargin = 0
            activityTvBinding.bannerContainer.visibility = VISIBLE
            activityTvBinding.categoryContainer.visibility = VISIBLE
            activityTvBinding.channelContainer.visibility = VISIBLE
        }
        activityTvBinding.tvView.layoutParams = layoutParams
    }
    private fun setChannelNumber(selectChannel: Channel?){
        mNumberTuner.root.visibility = View.GONE
        mNumberTuner.tunerDisplayNumber.text = ""
        mNumberTuner.tunerDisplayChannel.text = ""
        mNumberTuner.root.requestLayout()
        if (selectChannel != null){
            if (selectChannel.id != mCurrentChannel?.id){
                tune(selectChannel)
            }
        }
    }

    private fun putNumber(delay: Long){
        val channelNumber = mNumberTuner.tunerDisplayNumber.text.toString()
        val currentChannels = mCurrentChannels
        var selectChannel: Channel? = null
        currentChannels.forEach {
            if (it.displayNumber == channelNumber){
                selectChannel = it
            }
        }
        if (selectChannel != null){
            mNumberTuner.tunerDisplayChannel.text = selectChannel?.displayName
        }
        else{
            mNumberTuner.tunerDisplayChannel.text = ""
        }
        if (delay>0){
            numberHandler.postDelayed({
                setChannelNumber(selectChannel)
            }, delay)
        }
        else{
            setChannelNumber(selectChannel)
        }
    }

    fun getPortal(): Portal {
        return mConnectedPortal

    }

    fun getHistory(): List<Channel> {
        return mHistory
    }
    private fun getFormats(): List<Format> {
        val formats: MutableList<Format> = java.util.ArrayList()
        if (mTvPlayer != null){
            val groups = mTvPlayer?.currentTracks?.groups
            val size = groups?.size!!
            for (i in 0 until size) {
                val trackGroup = groups[i].mediaTrackGroup
                val length = trackGroup.length
                for (j in 0 until length) {
                    val format = trackGroup.getFormat(j)

                    formats.add(format)
                }
            }
        }

        return formats
    }
    fun selectLanguage(language: String){
        val parameters = DefaultTrackSelector.ParametersBuilder()
            .setPreferredAudioLanguage(language)
//                    .setTunnelingEnabled(true)
            .build()
        (mTvPlayer?.trackSelector as DefaultTrackSelector).parameters = parameters
//                player.trackSelectionParameters = parameters
        mTvPlayer?.prepare()

    }
    fun getTracks(): AudioViewModel {
        return mTracksViewModel
    }
}