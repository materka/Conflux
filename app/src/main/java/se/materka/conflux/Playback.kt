package se.materka.conflux

import android.annotation.SuppressLint
import android.content.Context
import android.media.AudioManager
import android.net.Uri
import android.net.wifi.WifiManager
import android.os.PowerManager
import android.support.v4.media.MediaBrowserServiceCompat
import android.support.v4.media.session.PlaybackStateCompat
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.TrackGroupArray
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.trackselection.TrackSelectionArray
import com.google.android.exoplayer2.upstream.HttpDataSource
import com.google.android.exoplayer2.util.Util
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.async
import se.materka.conflux.service.PlaylistService
import se.materka.exoplayershoutcastdatasource.ShoutcastDataSourceFactory
import se.materka.exoplayershoutcastdatasource.ShoutcastMetadata
import se.materka.exoplayershoutcastdatasource.ShoutcastMetadataListener
import timber.log.Timber
import java.io.IOException
import java.util.*

@SuppressLint("WifiManagerPotentialLeak")

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

class Playback(service: MediaBrowserServiceCompat, private val callback: Callback) : Player.EventListener,
        ShoutcastMetadataListener {

    companion object {
        // The volume we set the media player to when we lose audio focus, but are
        // allowed to reduce the volume instead of stopping playback.
        private val VOLUME_DUCK = 0.2f
        // The volume we set the media player when we have audio focus.
        private val VOLUME_NORMAL = 1.0f
    }

    interface Callback {
        fun onPlaybackStateChanged(state: Int)
        fun onError(errorCode: Int, error: String)
        fun onMetadataReceived(metadata: ShoutcastMetadata)
    }

    private val context = service.applicationContext
    private var currentUri: Uri? = null
    private var audioSource: MediaSource? = null
    private var audioState: Int = PlaybackStateCompat.STATE_NONE
    private var playOnFocusGain: Boolean = false
    private val playlist by lazy {
        Stack<Uri>()
    }

    private val player: SimpleExoPlayer by lazy {
        ExoPlayerFactory.newSimpleInstance(context,
                DefaultTrackSelector()).apply {
            addListener(this@Playback)
            audioAttributes = AudioAttributes.Builder()
                    .setUsage(C.USAGE_MEDIA)
                    .setContentType(C.CONTENT_TYPE_MUSIC)
                    .build()
        }
    }

    private val dataSourceFactory: ShoutcastDataSourceFactory by lazy {
        ShoutcastDataSourceFactory(
                Util.getUserAgent(context, context.getString(R.string.app_name)), this)
    }

    private val wifiLock by lazy {
        (context.getSystemService(Context.WIFI_SERVICE) as WifiManager)
                .createWifiLock(WifiManager.WIFI_MODE_FULL, "confluxWifiLock")
    }

    private val wakeLock by lazy {
        // Make sure the media player will acquire a wake-lock while
        // playing. If we don't do that, the CPU might go to sleep while the
        // song is playing, causing playback to stop.
        (context.getSystemService(Context.POWER_SERVICE) as PowerManager)
                .newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "confluxWakeLock")
    }

    private val audioFocusManager: AudioFocusManager = AudioFocusManager(service, { onAudioFocusChanged() })

    override fun onMetadataReceived(data: ShoutcastMetadata) {
        Timber.i("Metadata Received")
        callback.onMetadataReceived(data)
    }

    override fun onPlayerError(error: ExoPlaybackException?) {
        Timber.e(error)
        if (!playlist.isEmpty()) {
            play(playlist.pop())
        } else {
            callback.onError(PlaybackStateCompat.STATE_ERROR, "No working URLs found")
        }
        if (error?.sourceException is HttpDataSource.InvalidResponseCodeException) {
            val responseCode = (error.sourceException as HttpDataSource.InvalidResponseCodeException).responseCode
            when (responseCode) {
                404 -> {
                    callback.onError(PlaybackStateCompat.STATE_ERROR, "Url not found")
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
                audioState = if (playWhenReady) {
                    PlaybackStateCompat.STATE_PLAYING
                } else {
                    PlaybackStateCompat.STATE_STOPPED
                }
            }
            else -> audioState = PlaybackStateCompat.STATE_NONE
        }
        callback.onPlaybackStateChanged(audioState)
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

            // Give up Audio focus
            audioFocusManager.abandonAudioFocus()

            player.stop()
            player.playWhenReady = false
            audioSource?.releaseSource()

            callback?.onPlaybackStateChanged(PlaybackStateCompat.STATE_STOPPED)
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

        audioFocusManager.requestAudioFocus().let {
            if (it == AudioManager.AUDIOFOCUS_GAIN) {
                uri?.let {
                    if (it != Uri.EMPTY) {
                        currentUri = it
                        if (playlist.isEmpty() && PlaylistService.isPlayList(it)) {
                            async(CommonPool) {
                                PlaylistService.getPlaylist(it).let { list ->
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
        }
    }

    private fun onAudioFocusChanged() {
        when (audioFocusManager.audioFocus) {
            AudioManager.AUDIOFOCUS_GAIN -> {
                player.volume = VOLUME_NORMAL
                if (playOnFocusGain) {
                    play()
                    playOnFocusGain = false
                }
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                player.volume = VOLUME_DUCK
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                if (audioState == PlaybackStateCompat.STATE_PLAYING) {
                    playOnFocusGain = true
                    stop()
                }
            }
            AudioManager.AUDIOFOCUS_LOSS -> {
                playOnFocusGain = false
                stop()
            }
        }
    }

    private fun playUri(uri: Uri) {
        try {
            // This is the MediaSource representing the media to be played.
            audioSource = ExtractorMediaSource(uri,
                    dataSourceFactory, DefaultExtractorsFactory(), null, null)
            player.prepare(audioSource)
            player.playWhenReady = true


            // Prevent wifi from going to sleep, since we´re streaming from internet
            wifiLock.acquire()

            // Prevent CPU from going to sleep while screen is off
            wakeLock.acquire()

            callback.onPlaybackStateChanged(PlaybackStateCompat.STATE_BUFFERING)

        } catch (e: IOException) {
            callback.onError(PlaybackStateCompat.ERROR_CODE_UNKNOWN_ERROR, e.message ?: "Unknown error")
            Timber.e(e)
        }
    }
}