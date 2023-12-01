@file:Suppress("DEPRECATION")

package com.gi.hybridplayer

import android.content.ComponentName
import android.media.tv.TvInputInfo
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.View
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import com.gi.hybridplayer.conf.ConnectManager
import com.gi.hybridplayer.databinding.ActivityTvBinding
import com.gi.hybridplayer.db.repository.TvRepository
import com.gi.hybridplayer.model.*
import com.gi.hybridplayer.viewmodel.TvViewModel
import com.google.android.exoplayer2.DefaultLoadControl
import com.google.android.exoplayer2.DefaultRenderersFactory
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
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



class TvActivity : FragmentActivity() {


    private lateinit var activityTvBinding: ActivityTvBinding
    private lateinit var mTvView: StyledPlayerView
    private var mTvPlayer: ExoPlayer? = null
    private lateinit var mRepository: TvRepository
    private lateinit var mConnectedPortal: Portal
    private lateinit var mConnectManager: ConnectManager
    private lateinit var mTvViewModel:TvViewModel
    private lateinit var mInputId: String
    private val backgroundSetupScope = CoroutineScope(Dispatchers.IO)
    private var mProfile: Profile? = null
    private var mCategoryMap: Map<String, Category> = mutableMapOf()
    private var mCurrentChannel: Channel? = null

    private lateinit var mBannerFragment:BannerFragment
    private lateinit var mChannelListFragment: ChannelListFragment
    private lateinit var mCategoryFragment: CategoryFragment

    companion object{
        private val TAG: String = TvActivity::class.java.name
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        AndroidThreeTen.init(this)
        mInputId = getInputId()
        activityTvBinding = ActivityTvBinding.inflate(layoutInflater)
        setContentView(activityTvBinding.root)
        mTvView = activityTvBinding.tvView
        initPlayer()
        mRepository=TvRepository.getInstance(this@TvActivity)
        mConnectedPortal = intent.getSerializableExtra(Portal.PORTAL_INTENT_TAG) as Portal
        mConnectManager = ConnectManager(portal = mConnectedPortal)
        mTvViewModel = ViewModelProvider(this)[TvViewModel::class.java]
        mTvViewModel.currentChannel.observe(this){

            mCurrentChannel = it
            mBannerFragment.updateBanner(it)
            val url =it.videoUrl
            setVideoUrl(url!!)
        }
        mTvViewModel.currentCategory.observe(this){

        }
        backgroundSetupScope.launch {
            mProfile = mConnectManager.getProfile()
            mConnectManager.doAuth(mConnectedPortal.user_ID, mConnectedPortal.user_PW)
            mProfile = mConnectManager.getProfile()
            mCategoryMap = mConnectManager.getCategories().first
            val lastChannelId = mProfile?.lastItvId
            val lastChannel = if (lastChannelId != null && mRepository.getChannel(lastChannelId) != null){
                mRepository.getChannel(lastChannelId)
            } else{
                mRepository.getChannels()[0]
            }
            val lastCategory = mCategoryMap[lastChannel?.genreId]
            val list = mRepository.findListByChannel(lastCategory?.id!!)
            withContext(Dispatchers.Main){
                mBannerFragment = BannerFragment()
                mCategoryFragment = CategoryFragment(lastCategory.id, mCategoryMap.values.toList())
                mChannelListFragment = ChannelListFragment()
                supportFragmentManager.beginTransaction()
                    .replace(R.id.category_container, mCategoryFragment)
                    .replace(R.id.channel_container, mChannelListFragment)
                    .replace(R.id.banner_container, mBannerFragment)
                    .commit()
                supportFragmentManager.executePendingTransactions()
                mTvViewModel.setCurrentChannel(lastChannel)
                mTvViewModel.setCurrentCategory(Pair(lastCategory, list))
            }
        }
    }
    private fun isChannelListVisible(): Boolean {
        return mChannelListFragment.view?.visibility == View.VISIBLE
    }

    fun dPadCenterEvent(){
        if (!mBannerFragment.isHidden){
            supportFragmentManager.beginTransaction().hide(mBannerFragment).commitNow()
            supportFragmentManager.executePendingTransactions()
        }
        if (!isChannelListVisible()){
            mChannelListFragment.requestFocus(mCurrentChannel)
            mChannelListFragment.view?.visibility = View.VISIBLE
            mChannelListFragment.view?.requestLayout()
            mChannelListFragment.requestFocus(mCurrentChannel)
            /*if (mNumberTuner.visibility == View.VISIBLE){
                putNumber(0)
            }
            else{
                mChannelListFragment.requestFocus(mCurrentChannel)
                mChannelListFragment.view?.visibility = View.VISIBLE
                mChannelListFragment.view?.requestLayout()
                mChannelListFragment.requestFocus(mCurrentChannel)
            }*/
        }
    }
    private fun initPlayer(){
        val factory = DefaultRenderersFactory(this)
        factory.setExtensionRendererMode(DefaultRenderersFactory.EXTENSION_RENDERER_MODE_ON)
        val parameters = DefaultTrackSelector.ParametersBuilder()
            .setPreferredAudioLanguage("mul")
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
        mTvView.player = mTvPlayer
    }

    private fun setVideoUrl(url:String){
        val channelUrl = if (url.contains("localhost")){
            mConnectManager.createLink(
                type = ConnectManager.TYPE_ITV,
                cmd = url,
            )!!
        } else{
            url.substring(url.indexOf("http"), url.length)
        }
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

    override fun onResume() {
        super.onResume()
        if (mTvPlayer != null){
            mTvPlayer?.prepare()
        }
    }

    override fun onStop() {
        super.onStop()
        mTvPlayer?.setVideoSurface(null)
        mTvPlayer?.stop()
        mTvPlayer?.release()
    }

    private fun getInputId():String{
        val componentName = ComponentName(this.packageName, InputService::class.java.name)
        val builder: TvInputInfo.Builder = TvInputInfo.Builder(this, componentName)
        val tvInputInfo: TvInputInfo = builder.build()
        val intent = tvInputInfo.createSetupIntent()
        return intent.getStringExtra(TvInputInfo.EXTRA_INPUT_ID)!!
    }


    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_CHANNEL_UP || keyCode == KeyEvent.KEYCODE_CHANNEL_DOWN){
            val list= mTvViewModel.currentCategory.value?.second!!
            var index = -1
            list.forEach {
                if (it.originalNetworkId == mCurrentChannel?.originalNetworkId){
                    index = list.indexOf(it)
                }
            }
            if (keyCode == KeyEvent.KEYCODE_CHANNEL_UP){
                mCurrentChannel =
                    if (index<list.size-1){
                        list[index+1]
                    } else{
                        list[0]
                    }
            }
            else{
                mCurrentChannel = if (index>0){
                    list[index-1]
                }
                else{
                    list[list.size-1]
                }
            }
            mBannerFragment.updateBanner(mCurrentChannel!!)


        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_CHANNEL_UP
            || keyCode == KeyEvent.KEYCODE_CHANNEL_DOWN){
            mTvViewModel.setCurrentChannel(mCurrentChannel)
        }
        if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER){

        }
        return super.onKeyUp(keyCode, event)
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

    fun tune(channel:Channel){
        if (channel != mCurrentChannel){
            mTvViewModel.setCurrentChannel(channel)
        }
    }

    fun isEpgVisible(): Boolean {
        return false
    }
}