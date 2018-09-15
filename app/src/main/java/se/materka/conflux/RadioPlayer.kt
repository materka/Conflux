package se.materka.conflux

import android.annotation.SuppressLint
import android.content.Context
import android.media.AudioManager
import android.net.Uri
import android.net.wifi.WifiManager
import android.os.PowerManager
import android.support.v4.media.session.PlaybackStateCompat
import androidx.media.MediaBrowserServiceCompat
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.Player.*
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.TrackGroupArray
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.trackselection.TrackSelectionArray
import com.google.android.exoplayer2.util.Util
import kotlinx.coroutines.experimental.launch
import se.materka.exoplayershoutcastdatasource.ShoutcastDataSourceFactory
import se.materka.exoplayershoutcastdatasource.ShoutcastMetadata
import se.materka.exoplayershoutcastdatasource.ShoutcastMetadataListener
import java.io.IOException
import java.util.*

@SuppressLint("WifiManagerPotentialLeak")

/**
 * Copyright Mattias Karlsson

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

class RadioPlayer(mediaBrowser: MediaBrowserServiceCompat, private val callback: Callback) : ShoutcastMetadataListener {

    companion object {
        // The volume we set the media player to when we lose audio focus, but are
        // allowed to reduce the volume instead of stopping playback.
        private const val VOLUME_DUCK = 0.2f
        // The volume we set the media player when we have audio focus.
        private const val VOLUME_NORMAL = 1.0f
    }

    interface Callback {
        fun onPlaybackStateChanged(state: Int)
        fun onError(errorCode: Int, error: String)
        fun onMetadataReceived(metadata: ShoutcastMetadata)
    }

    private val context = mediaBrowser.applicationContext
    private var currentUri: Uri? = null
    private var audioSource: MediaSource? = null
    private var audioState: Int = PlaybackStateCompat.STATE_NONE
    private var playOnFocusGain: Boolean = false
    private val playlist: Stack<Uri> = Stack()

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
                .newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "conflux:WakeLock")
    }

    private val audioFocusManager: AudioFocusManager = AudioFocusManager.newInstance(mediaBrowser) { audioFocus ->  onAudioFocusChanged(audioFocus) }

    private val playerListener: com.google.android.exoplayer2.Player.EventListener = object : com.google.android.exoplayer2.Player.EventListener {

        override fun onPlayerError(error: ExoPlaybackException?) {
            // TODO: Log error
            if (!playlist.isEmpty()) {
                play(playlist.pop())
                return
            }
            callback.onError(PlaybackStateCompat.STATE_ERROR, "Could not play provided station")
        }

        override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
            when (playbackState) {
                STATE_BUFFERING -> {
                    // TODO : info("PlaybackState: Buffering")
                    audioState = PlaybackStateCompat.STATE_BUFFERING
                }
                STATE_ENDED -> {
                    // TODO : info("PlaybackState: Ended")
                    audioState = PlaybackStateCompat.STATE_STOPPED
                }
                STATE_IDLE -> {
                    // TODO : info("PlaybackState: Idle")
                    audioState = PlaybackStateCompat.STATE_NONE
                }
                STATE_READY -> {
                    // TODO : info("PlaybackState: Ready")
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
            // TODO : debug("Loading: $isLoading")
        }

        override fun onTimelineChanged(timeline: Timeline?, manifest: Any?) {
            // TODO : debug("onTimelineChanged: ${timeline?.toString()} ${manifest?.toString()}")
        }

        override fun onTracksChanged(trackGroups: TrackGroupArray?, trackSelections: TrackSelectionArray?) {
            // TODO : debug("onTracksChanged")
        }

        override fun onPlaybackParametersChanged(playbackParameters: PlaybackParameters?) {
            // TODO : debug("onPlaybackParameterChanged")
        }

        override fun onRepeatModeChanged(repeatMode: Int) {
            // TODO : debug("onRepeatModeChanged")
        }

        override fun onSeekProcessed() {
            // TODO : debug("onSeekProcessed")
        }

        override fun onPositionDiscontinuity(reason: Int) {
            // TODO : debug("onPositionDiscontinuity")
        }

        override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
            // TODO : debug("onShuffleModeEnabledChanged")
        }
    }

    private val player: SimpleExoPlayer by lazy {
        ExoPlayerFactory.newSimpleInstance(context,
                DefaultTrackSelector()).apply {
            addListener(playerListener)
            audioAttributes = AudioAttributes.Builder()
                    .setUsage(C.USAGE_MEDIA)
                    .setContentType(C.CONTENT_TYPE_MUSIC)
                    .build()
        }
    }

    override fun onMetadataReceived(data: ShoutcastMetadata) {
        // TODO : info("Metadata Received")
        callback.onMetadataReceived(data)
    }

    fun stop(releasePlayer: Boolean = false) {
        if (player.playWhenReady) {
            // TODO : info("Stopping playback")

            // Give up Audio focus
            audioFocusManager.abandonAudioFocus()

            player.stop()
            player.playWhenReady = false
            audioSource?.releaseSource()

            if (!releasePlayer) {
                callback.onPlaybackStateChanged(PlaybackStateCompat.STATE_STOPPED)
            }
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
        playlist.clear()
        if (player.playWhenReady) {
            stop()
        }

        audioFocusManager.requestAudioFocus().let {
            if (it == AudioManager.AUDIOFOCUS_GAIN && uri != null && uri != Uri.EMPTY) {
                currentUri = uri
                if (playlist.isEmpty() && PlaylistUtil.isPlayList(uri)) {
                    launch {
                        PlaylistUtil.getPlaylist(uri).let { list ->
                            if (!list.isEmpty()) {
                                playlist.addAll(list)
                                playUri(playlist.pop())
                            }
                        }
                    }
                } else {
                    playUri(uri)
                }
            }
        }
    }

    private fun onAudioFocusChanged(audioFocus: Int) {
        when (audioFocus) {
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
            audioSource = ExtractorMediaSource.Factory(dataSourceFactory).createMediaSource(uri)
            player.prepare(audioSource)
            player.playWhenReady = true


            // Prevent wifi from going to sleep, since we´re streaming from internet
            wifiLock.acquire()

            // Prevent CPU from going to sleep while screen is off
            wakeLock.acquire(1000)

            callback.onPlaybackStateChanged(PlaybackStateCompat.STATE_BUFFERING)

        } catch (e: IOException) {
            // TODO : error("Playing URL", e)
            callback.onError(PlaybackStateCompat.ERROR_CODE_UNKNOWN_ERROR, e.message
                    ?: "Unknown error")
        }
    }
}