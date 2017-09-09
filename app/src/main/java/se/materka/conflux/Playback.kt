package se.materka.conflux

import android.content.Context
import android.media.AudioManager
import android.net.Uri
import android.net.wifi.WifiManager
import android.os.PowerManager
import android.support.v4.media.MediaBrowserServiceCompat
import android.support.v4.media.session.PlaybackStateCompat
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.TrackGroupArray
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.trackselection.TrackSelectionArray
import com.google.android.exoplayer2.upstream.HttpDataSource
import com.google.android.exoplayer2.util.Util
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import se.materka.conflux.utils.PlaylistHelper
import se.materka.exoplayershoutcastdatasource.ShoutcastDataSourceFactory
import se.materka.exoplayershoutcastdatasource.ShoutcastMetadata
import se.materka.exoplayershoutcastdatasource.ShoutcastMetadataListener
import timber.log.Timber
import java.io.IOException
import java.util.*

/**
 * Copyright 2017 Mattias Karlsson

 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at

 * http://www.apache.org/licenses/LICENSE-2.0

 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

class Playback(private val service: MediaBrowserServiceCompat) : Player.EventListener,
        ShoutcastMetadataListener, AudioManager.OnAudioFocusChangeListener {

    interface Callback {
        fun onPlaybackStateChanged(state: Int)
        fun onError(errorCode: Int, error: String)
        fun onMetadataReceived(metadata: ShoutcastMetadata)
    }

    // The volume we set the media player to when we lose audio focus, but are
    // allowed to reduce the volume instead of stopping playback.
    private val VOLUME_DUCK = 0.2f
    // The volume we set the media player when we have audio focus.
    private val VOLUME_NORMAL = 1.0f
    // we don't have audio focus, and can't duck (play at a low volume)
    private val AUDIO_NO_FOCUS_NO_DUCK = 0
    // we don't have focus, but can duck (play at a low volume)
    private val AUDIO_NO_FOCUS_CAN_DUCK = 1
    // we have full audio focus
    private val AUDIO_FOCUSED = 2

    var callback: Callback? = null
    private var currentUri: Uri? = null
    private var playOnFocusGain: Boolean = false
    private var audioSource: MediaSource? = null
    private var audioFocus: Int = AUDIO_NO_FOCUS_NO_DUCK
    private var audioState: Int = PlaybackStateCompat.STATE_NONE
    private val playlist by lazy {
        Stack<Uri>()
    }

    private val player: SimpleExoPlayer by lazy {
        ExoPlayerFactory.newSimpleInstance(service.applicationContext,
                DefaultTrackSelector()).apply {
            addListener(this@Playback)
        }
    }

    private val dataSourceFactory: ShoutcastDataSourceFactory by lazy {
        ShoutcastDataSourceFactory(
                Util.getUserAgent(service.applicationContext, service.applicationContext.getString(R.string.app_name)), this)
    }

    private val wifiLock by lazy {
        (service.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager)
                .createWifiLock(WifiManager.WIFI_MODE_FULL, "confluxWifiLock")
    }

    private val wakeLock by lazy {
        // Make sure the media player will acquire a wake-lock while
        // playing. If we don't do that, the CPU might go to sleep while the
        // song is playing, causing playback to stop.
        (service.applicationContext.getSystemService(Context.POWER_SERVICE) as PowerManager)
                .newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "confluxWakeLock")
    }

    private val audioManager: AudioManager by lazy {
        service.applicationContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    }

    override fun onAudioFocusChange(focusChange: Int) {
        Timber.i("onAudioFocusChange. focusChange=$focusChange")
        if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
            // We have gained focus:
            audioFocus = AUDIO_FOCUSED

        } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS
                || focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT
                || focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK) {
            // We have lost focus. If we can duck (low playback volume), we can keep playing.
            // Otherwise, we need to pause the playback.
            val canDuck = focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK
            audioFocus = if (canDuck) AUDIO_NO_FOCUS_CAN_DUCK else AUDIO_NO_FOCUS_NO_DUCK

            if (audioState == PlaybackStateCompat.STATE_PLAYING && !canDuck) {
                // If we don't have audio focus and can't duck, we save the information that
                // we were playing, so that we can resume playback once we get the focus back.
                playOnFocusGain = true
            }
        } else {
            Timber.i("onAudioFocusChange: Ignoring unsupported focusChange: $focusChange")
        }
        refreshPlayerVolume()
    }

    override fun onMetadataReceived(data: ShoutcastMetadata) {
        Timber.i("Metadata Received")
        callback?.onMetadataReceived(data)
    }

    override fun onPlayerError(error: ExoPlaybackException?) {
        Timber.e(error)
        if (!playlist.isEmpty()) {
            play(playlist.pop())
        } else {
            callback?.onError(PlaybackStateCompat.STATE_ERROR, "No working URLs found")
        }
        if (error?.sourceException is HttpDataSource.InvalidResponseCodeException) {
            val responseCode = (error.sourceException as HttpDataSource.InvalidResponseCodeException).responseCode
            when (responseCode) {
                404 -> {
                    callback?.onError(PlaybackStateCompat.STATE_ERROR, "Url not found")
                }
            }
        }
    }

    override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
        when (playbackState) {
            Player.STATE_BUFFERING -> {
                Timber.i("PlaybackState: Buffering")
                audioState = PlaybackStateCompat.STATE_BUFFERING
            }
            Player.STATE_ENDED -> {
                Timber.i("PlaybackState: Ended")
                audioState = PlaybackStateCompat.STATE_STOPPED
            }
            Player.STATE_IDLE -> {
                Timber.i("PlaybackState: Idle")
                audioState = PlaybackStateCompat.STATE_NONE
            }
            Player.STATE_READY -> {
                Timber.i("PlaybackState: Ready")
                if (playWhenReady) {
                    audioState = PlaybackStateCompat.STATE_PLAYING
                } else {
                    audioState = PlaybackStateCompat.STATE_STOPPED
                }
            }
            else -> audioState = PlaybackStateCompat.STATE_NONE
        }
        callback?.onPlaybackStateChanged(audioState)
    }

    override fun onLoadingChanged(isLoading: Boolean) {
        Timber.i("Loading: $isLoading")
    }

    override fun onPositionDiscontinuity() {
        Timber.i("onPositionDiscontinuity: discontinuity detected")
    }

    override fun onTimelineChanged(timeline: Timeline?, manifest: Any?) {
        Timber.i("onTimelineChanged: ${timeline?.toString()} ${manifest?.toString()}")
    }

    override fun onTracksChanged(trackGroups: TrackGroupArray?, trackSelections: TrackSelectionArray?) {
        Timber.i("onTracksChanged")
    }

    override fun onPlaybackParametersChanged(playbackParameters: PlaybackParameters?) {
        Timber.i("onPlaybackParameterChanged")
    }

    override fun onRepeatModeChanged(repeatMode: Int) {
        Timber.i("onRepeatModeChanged")
    }

    fun stop(releasePlayer: Boolean = false) {
        if (player.playWhenReady) {
            Timber.i("Stopping playback")
            callback?.onPlaybackStateChanged(PlaybackStateCompat.STATE_STOPPED)

            // Give up Audio focus
            abandonAudioFocus()

            player.stop()
            player.playWhenReady = false
            audioSource?.releaseSource()
        }

        if (wifiLock.isHeld) {
            wifiLock.release()
        }
        if (wakeLock.isHeld) {
            wakeLock.release()
        }

        if (releasePlayer) {
            player.release()
        }
    }

    fun play(uri: Uri? = currentUri) {
        if (player.playWhenReady) {
            stop()
        }

        playOnFocusGain = true

        requestAudioFocus()

        uri?.let {
            if (it != Uri.EMPTY) {
                currentUri = it
                if (playlist.isEmpty() && PlaylistHelper.isPlayList(it)) {
                    launch(UI) {
                        PlaylistHelper.getPlaylist(it).let { list ->
                            if (!list.isEmpty()) {
                                playlist.addAll(list)
                                playUri(playlist.pop())
                            }
                        }
                    }
                } else {
                    playUri(it)
                }
            }
        }
    }

    private fun requestAudioFocus() {
        Timber.i("requestAudioFocus")
        val result = audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN)
        audioFocus = if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED)
            AUDIO_FOCUSED
        else
            AUDIO_NO_FOCUS_NO_DUCK
    }

    private fun abandonAudioFocus() {
        Timber.i("abandonAudioFocus")
        if (!playOnFocusGain) {
            if (audioManager.abandonAudioFocus(this) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                audioFocus = AUDIO_NO_FOCUS_NO_DUCK
            }
        }
    }

    private fun refreshPlayerVolume() {
        Timber.i("refreshPlayerVolume. audioFocus = $audioFocus")
        if (audioFocus == AUDIO_NO_FOCUS_NO_DUCK) {
            // If we don't have audio focus and can't duck, we have to pause,
            if (audioState == PlaybackStateCompat.STATE_PLAYING) {
                stop()
            }
        } else {  // we have audio focus:
            player.volume = if (audioFocus == AUDIO_NO_FOCUS_CAN_DUCK)
                VOLUME_DUCK // we'll be relatively quiet
            else
                VOLUME_NORMAL

            // If we were playing when we lost focus, we need to resume playing.
            if (playOnFocusGain) {
                play()
                playOnFocusGain = false
            }
        }
    }

    private fun playUri(uri: Uri) {
        try {
            // This is the MediaSource representing the media to be played.
            audioSource = ExtractorMediaSource(uri,
                    dataSourceFactory, DefaultExtractorsFactory(), null, null)
            player.audioStreamType = AudioManager.STREAM_MUSIC
            player.prepare(audioSource)
            player.playWhenReady = true


            // Prevent wifi from going to sleep, since we´re streaming from internet
            wifiLock.acquire()

            // Prevent CPU from going to sleep while screen is off
            wakeLock.acquire()

            callback?.onPlaybackStateChanged(PlaybackStateCompat.STATE_BUFFERING)

        } catch (e: IOException) {
            Timber.e(e)
            callback?.onError(PlaybackStateCompat.ERROR_CODE_UNKNOWN_ERROR, e.message ?: "Unknown error")
        }
    }
}