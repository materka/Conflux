package se.materka.conflux

import android.content.Context
import android.media.AudioManager
import android.net.Uri
import android.net.wifi.WifiManager
import android.os.PowerManager
import android.support.v4.media.session.PlaybackStateCompat
import androidx.media.MediaBrowserServiceCompat
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.ExoPlaybackException
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.Player.*
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.util.Util
import kotlinx.coroutines.experimental.*
import okhttp3.OkHttpClient
import se.materka.exoplayershoutcastdatasource.ShoutcastMetadata
import java.io.IOException
import java.util.*

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

class RadioPlayer(mediaBrowser: MediaBrowserServiceCompat, private val listener: RadioPlayerListener) : ShoutcastDataSource.ShoutcastMetadataListener {

    companion object {
        // The volume we set the media player to when we lose audio focus, but are
        // allowed to reduce the volume instead of stopping playback.
        private const val VOLUME_DUCK = 0.2f
        // The volume we set the media player when we have audio focus.
        private const val VOLUME_NORMAL = 1.0f
    }

    interface RadioPlayerListener {
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
    private var metadata: ShoutcastMetadata? = null

    private val shoutcastDataSource: ShoutcastDataSource by lazy {
        ShoutcastDataSource(
                OkHttpClient.Builder().build(),
                Util.getUserAgent(context, context.getString(R.string.app_name)),
                null, null, null, this)
    }

    private val wifiLock by lazy {
        (context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager)
                .createWifiLock(WifiManager.WIFI_MODE_FULL, "${context.getString(R.string.app_name)}:WifiLock")
    }

    private val wakeLock by lazy {
        // Make sure the media player will acquire a wake-lock while
        // playing. If we don't do that, the CPU might go to sleep while the
        // song is playing, causing playback to stop.
        (context.applicationContext.getSystemService(Context.POWER_SERVICE) as PowerManager)
                .newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "${context.getString(R.string.app_name)}:WakeLock")
    }

    private val eventListener: com.google.android.exoplayer2.Player.EventListener = object : com.google.android.exoplayer2.Player.EventListener {

        override fun onPlayerError(error: ExoPlaybackException?) {
            // TODO: Log error
            if (!playlist.isEmpty()) {
                play(playlist.pop())
                return
            }
            listener.onError(PlaybackStateCompat.STATE_ERROR, "Could not play provided station")
        }

        override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
            when (playbackState) {
                STATE_BUFFERING -> {
                    audioState = PlaybackStateCompat.STATE_BUFFERING
                }
                STATE_ENDED -> {
                    audioState = PlaybackStateCompat.STATE_STOPPED
                }
                STATE_IDLE -> {
                    audioState = PlaybackStateCompat.STATE_NONE
                }
                STATE_READY -> {
                    audioState = if (playWhenReady) {
                        this@RadioPlayer.metadata?.let {
                            listener.onMetadataReceived(it)
                        }
                        PlaybackStateCompat.STATE_PLAYING
                    } else {
                        PlaybackStateCompat.STATE_STOPPED
                    }
                }
                else -> audioState = PlaybackStateCompat.STATE_NONE
            }
            listener.onPlaybackStateChanged(audioState)
        }
    }

    private val player: SimpleExoPlayer by lazy {
        ExoPlayerFactory.newSimpleInstance(context,
                DefaultTrackSelector()).apply {
            addListener(eventListener)
            audioAttributes = AudioAttributes.Builder()
                    .setUsage(C.USAGE_MEDIA)
                    .setContentType(C.CONTENT_TYPE_MUSIC)
                    .build()
        }
    }

    override fun onMetadataReceived(data: ShoutcastMetadata) {
        this.metadata = data
        if (this.audioState == PlaybackStateCompat.STATE_PLAYING) {
            listener.onMetadataReceived(data)
        }
    }

    fun stop(releasePlayer: Boolean = false) {
        listener.onMetadataReceived(ShoutcastMetadata.Builder().build())

        if (player.playWhenReady) {
            // TODO : info("Stopping playback")

            player.stop()
            player.playWhenReady = false

            if (!releasePlayer) {
                listener.onPlaybackStateChanged(PlaybackStateCompat.STATE_STOPPED)
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
        if (uri != null) {
            currentUri = uri
            if (playlist.isEmpty()) {
                GlobalScope.launch(Dispatchers.Main) {
                    async(Dispatchers.Default) {
                        PlaylistUtil.getPlaylist(uri).let { list ->
                            if (!list.isEmpty()) {
                                playlist.addAll(list)
                            }
                        }
                    }.await()
                    if (!playlist.empty()) {
                        prepare(playlist.pop())
                    }
                }
            } else {
                prepare(uri)
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

    private fun prepare(uri: Uri) {
        try {
            // This is the MediaSource representing the media to be played.
            audioSource = ExtractorMediaSource.Factory { shoutcastDataSource }.createMediaSource(uri)
            player.prepare(audioSource)
            player.playWhenReady = true


            // Prevent wifi from going to sleep, since we´re streaming from internet
            wifiLock.acquire()

            // Prevent CPU from going to sleep while screen is off
            wakeLock.acquire(1000)

            listener.onPlaybackStateChanged(PlaybackStateCompat.STATE_BUFFERING)

        } catch (e: IOException) {
            // TODO : error("Playing URL", e)
            listener.onError(PlaybackStateCompat.ERROR_CODE_UNKNOWN_ERROR, e.message
                    ?: "Unknown error")
        }
    }
}