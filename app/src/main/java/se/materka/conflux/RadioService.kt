package se.materka.conflux

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager
import android.net.Uri
import android.os.Bundle
import android.os.IBinder
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.lifecycle.ServiceLifecycleDispatcher
import androidx.media.MediaBrowserServiceCompat
import androidx.media.session.MediaButtonReceiver
import org.koin.android.ext.android.inject
import se.materka.conflux.db.entity.Station
import se.materka.conflux.db.repository.StationRepository
import se.materka.conflux.ui.view.MainActivity
import se.materka.exoplayershoutcastdatasource.ShoutcastMetadata
import java.lang.IllegalStateException


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

class RadioService : MediaBrowserServiceCompat(), LifecycleOwner {
    override fun getLifecycle(): Lifecycle {
        return lifecycleDispatcher.lifecycle
    }

    companion object {
        const val SERVICE_ID: Int = 1
    }

    private val lifecycleDispatcher = ServiceLifecycleDispatcher(this)

    private val stationRepository: StationRepository by inject()

    private var nowPlaying: Station? = null

    private val mediaSession: MediaSessionCompat by lazy {

        val notificationIntent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val intent = PendingIntent.getActivity(applicationContext, 0,
                notificationIntent, 0)
        MediaSessionCompat(this@RadioService, RadioService::class.java.name).apply {
            setSessionActivity(intent)

            // Set an initial PlaybackState with ACTION_PLAY, so media buttons can start the player
            setPlaybackState(stateBuilder.build())

            // MediaSessionCallback has methods that handle callbacks from a media controller
            setCallback(mediaSessionCallback)

            // Enable callbacks from MediaButtons and TransportControls
            setFlags(MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS
                    or MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS)
        }
    }

    private val stateBuilder: PlaybackStateCompat.Builder by lazy {
        PlaybackStateCompat.Builder()
                .setActions(PlaybackStateCompat.ACTION_PLAY
                        or PlaybackStateCompat.ACTION_STOP)
    }

    private val notificationManager: NotificationManagerCompat by lazy {
        NotificationManagerCompat.from(applicationContext)
    }

    private val audioBecomingNoisyIntentFilter: IntentFilter = IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY)

    private val audioBecomingNoisyReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (AudioManager.ACTION_AUDIO_BECOMING_NOISY == intent.action) {
                stop()
            }
        }
    }

    private val player: RadioPlayer by lazy {
        RadioPlayer(this, PlaybackCallback())
    }

    // MediaSessionCallback() has methods that handle callbacks from a media controller
    private val mediaSessionCallback: MediaSessionCompat.Callback = object : MediaSessionCompat.Callback() {

        override fun onPlayFromMediaId(mediaId: String?, extras: Bundle?) {
            super.onPlayFromMediaId(mediaId, extras)
            if (mediaId != null) {
                stationRepository.getStation(mediaId.toLong()).observeOnce(Observer {
                    play(Uri.parse(it?.url))
                })
            }
        }

        override fun onPlayFromUri(uri: Uri?, extras: Bundle?) {
            play(uri)
            // Use application class loader to un-marshall custom class, else system class loader will be used which does not
            // recognize custom class and produce BadParcelableException: ClassNotFoundException
            extras?.classLoader = classLoader
            nowPlaying = extras?.getParcelable("EXTRA_STATION") ?: Station().apply { name = uri?.toString() }
        }

        override fun onPlay() {
            play()
        }

        override fun onPause() {
            stop()
        }

        override fun onStop() {
            stop()
        }
    }

    override fun onLoadChildren(parentId: String, result: Result<MutableList<MediaBrowserCompat.MediaItem>>) {
        result.detach()
        stationRepository.getStations().observeOnce(Observer { stations ->
            val mediaItems = mutableListOf<MediaBrowserCompat.MediaItem>()
            stations?.forEach { station ->
                val description: MediaDescriptionCompat = MediaDescriptionCompat.Builder()
                        .setMediaId(station.id?.toString())
                        .setMediaUri(Uri.parse(station.url))
                        .setTitle(station.name)
                        .build()
                mediaItems.add(MediaBrowserCompat.MediaItem(description, MediaBrowserCompat.MediaItem.FLAG_PLAYABLE))
            }
            result.sendResult(mediaItems)
        })
    }

    override fun onGetRoot(clientPackageName: String, clientUid: Int, rootHints: Bundle?): BrowserRoot? {
        return MediaBrowserServiceCompat.BrowserRoot("", null)
    }

    override fun onCreate() {
        lifecycleDispatcher.onServicePreSuperOnCreate()
        super.onCreate()
        sessionToken = mediaSession.sessionToken

    }

    @Suppress("OverridingDeprecatedMember", "DEPRECATION")
    override fun onStart(intent: Intent?, startId: Int) {
        lifecycleDispatcher.onServicePreSuperOnStart()
        super.onStart(intent, startId)
    }

    override fun onBind(intent: Intent?): IBinder {
        lifecycleDispatcher.onServicePreSuperOnBind()
        return super.onBind(intent)
    }

    override fun onDestroy() {
        stop(true)
        lifecycleDispatcher.onServicePreSuperOnDestroy()
        super.onDestroy()
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        MediaButtonReceiver.handleIntent(mediaSession, intent)
        return super.onStartCommand(intent, flags, startId)
    }

    private fun play(uri: Uri? = null) {
        registerReceiver(audioBecomingNoisyReceiver, audioBecomingNoisyIntentFilter)
        if (uri != null) {
            player.play(uri)
        } else {
            player.play()
        }
        mediaSession.isActive = true
        startForeground(SERVICE_ID, NotificationUtil.build(this, mediaSession))
    }

    private fun stop(releasePlayer: Boolean = false) {
        try {
            unregisterReceiver(audioBecomingNoisyReceiver)
        } catch (e: IllegalArgumentException) {
            // Do nothing, the receiver has already been unregistered
        }

        player.stop(releasePlayer)
        mediaSession.isActive = false
        stopForeground(true)
    }

    private fun setPlaybackState(state: PlaybackStateCompat) {
        try {
            mediaSession.setPlaybackState(state)
        } catch (e: IllegalStateException) {
            // TODO: Investigate why for some unknown reason we occasionally selectAll an exception for
            // "beginBroadcast() called while already in a broadast"
            // RemoteCallbackList.beginBroadcast -> Måste kallas på samma tråd hela tiden. Kan vara så att den kallas på annan tråd
            // TODO: error("Illegal state detected", e)
        }
    }

    private inner class PlaybackCallback : RadioPlayer.Callback {
        override fun onPlaybackStateChanged(state: Int) {
            stateBuilder.setState(state, 0L, 0f).build().also {
                setPlaybackState(it)
            }
        }

        override fun onError(errorCode: Int, error: String) {
            val state = stateBuilder
                    .setState(PlaybackStateCompat.STATE_ERROR, 0L, 0f)
                    .setErrorMessage(PlaybackStateCompat.ERROR_CODE_UNKNOWN_ERROR, "No working url found")
                    .build()
            setPlaybackState(state)
        }

        override fun onMetadataReceived(metadata: ShoutcastMetadata) {
            val mediaMetadataBuilder: MediaMetadataCompat.Builder = MediaMetadataCompat.Builder()
                    .putString(ShoutcastMetadata.METADATA_KEY_TITLE, metadata.getString(ShoutcastMetadata.METADATA_KEY_TITLE))
                    .putString(ShoutcastMetadata.METADATA_KEY_ARTIST, metadata.getString(ShoutcastMetadata.METADATA_KEY_ARTIST))
                    .putString(ShoutcastMetadata.METADATA_KEY_SHOW, metadata.getString(ShoutcastMetadata.METADATA_KEY_SHOW))
                    .putLong(ShoutcastMetadata.METADATA_KEY_BITRATE, metadata.getLong(ShoutcastMetadata.METADATA_KEY_BITRATE))
                    .putLong(ShoutcastMetadata.METADATA_KEY_CHANNELS, metadata.getLong(ShoutcastMetadata.METADATA_KEY_CHANNELS))
                    .putString(ShoutcastMetadata.METADATA_KEY_FORMAT, metadata.getString(ShoutcastMetadata.METADATA_KEY_FORMAT))
                    .putString(ShoutcastMetadata.METADATA_KEY_STATION, nowPlaying?.name
                            ?: metadata.getString(ShoutcastMetadata.METADATA_KEY_URL))
                    .putString(ShoutcastMetadata.METADATA_KEY_URL, metadata.getString(ShoutcastMetadata.METADATA_KEY_URL))
            mediaSession.setMetadata(mediaMetadataBuilder.build())
            notificationManager.notify(SERVICE_ID, NotificationUtil.build(this@RadioService, mediaSession))
        }
    }
}
