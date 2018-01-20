package se.materka.conflux.service.play

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.NotificationManagerCompat
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaBrowserServiceCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaButtonReceiver
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import com.squareup.picasso.Picasso
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.async
import org.jetbrains.anko.coroutines.experimental.bg
import se.materka.conflux.R
import se.materka.conflux.service.ArtistArtService
import se.materka.exoplayershoutcastdatasource.ShoutcastMetadata
import timber.log.Timber
import java.lang.IllegalStateException


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

class PlayService : MediaBrowserServiceCompat(), PlaybackService.Callback {

    private val SERVICE_ID: Int = 1

    private val mediaSession: MediaSessionCompat by lazy {
        MediaSessionCompat(this@PlayService, PlayService::class.java.name).apply {
            setPlaybackState(stateBuilder.build())
            setCallback(MediaSessionCallback())
            setFlags(MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS
                    or MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS)
        }
    }

    private val stateBuilder: PlaybackStateCompat.Builder by lazy {
        PlaybackStateCompat.Builder()
                .setActions(PlaybackStateCompat.ACTION_PLAY
                        or PlaybackStateCompat.ACTION_STOP)
    }

    private val metadataBuilder: MediaMetadataCompat.Builder by lazy {
        MediaMetadataCompat.Builder()
    }

    private val notificationManager: NotificationManagerCompat by lazy {
        NotificationManagerCompat.from(applicationContext)
    }

    private val audioBecomingNoisyIntentFilter: IntentFilter = IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY)

    private var audioBecomingNoisyReceiver: BecomingNoisyReceiver? = null

    private val player: PlaybackService by lazy {
        PlaybackService(this, this@PlayService)
    }

    override fun onPlaybackStateChanged(state: Int) {
        stateBuilder.setState(state, 0L, 0f).build().also {
            setPlaybackState(it)
        }
    }

    override fun onError(errorCode: Int, error: String) {
        val state = stateBuilder
                .setState(PlaybackStateCompat.STATE_ERROR, 0L, 0f)
                .setErrorMessage(404, "No working url found")
                .build()
        setPlaybackState(state)
    }

    override fun onMetadataReceived(metadata: ShoutcastMetadata) {
        val mediaMetadataBuilder: MediaMetadataCompat.Builder = metadataBuilder
                .putString(ShoutcastMetadata.METADATA_KEY_TITLE, metadata.getString(ShoutcastMetadata.METADATA_KEY_TITLE))
                .putString(ShoutcastMetadata.METADATA_KEY_ARTIST, metadata.getString(ShoutcastMetadata.METADATA_KEY_ARTIST))
                .putString(ShoutcastMetadata.METADATA_KEY_SHOW, metadata.getString(ShoutcastMetadata.METADATA_KEY_SHOW))
                .putLong(ShoutcastMetadata.METADATA_KEY_BITRATE, metadata.getLong(ShoutcastMetadata.METADATA_KEY_BITRATE))
                .putLong(ShoutcastMetadata.METADATA_KEY_CHANNELS, metadata.getLong(ShoutcastMetadata.METADATA_KEY_CHANNELS))
                .putString(ShoutcastMetadata.METADATA_KEY_FORMAT, metadata.getString(ShoutcastMetadata.METADATA_KEY_FORMAT))
                .putString(ShoutcastMetadata.METADATA_KEY_STATION, metadata.getString(ShoutcastMetadata.METADATA_KEY_STATION))
                .putString(ShoutcastMetadata.METADATA_KEY_URL, metadata.getString(ShoutcastMetadata.METADATA_KEY_URL))

        ArtistArtService(getString(R.string.spotify_client_id), getString(R.string.spotify_client_secret))
                .getArt(metadata.getString(ShoutcastMetadata.METADATA_KEY_ARTIST)) { uri ->
                    async(CommonPool) {
                        val bm = bg {
                            Picasso
                                    .with(this@PlayService)
                                    .load(uri)
                                    .placeholder(R.drawable.md_streaming_radio)
                                    .get()
                        }
                        val mediaMetadata = mediaMetadataBuilder
                                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI, uri.toString())
                                .putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, bm.await())
                                .build()
                        mediaSession.setMetadata(mediaMetadata)
                        notificationManager.notify(SERVICE_ID, PlayNotification.buildNotification(this@PlayService, mediaSession))
                    }
                }
        mediaSession.setMetadata(mediaMetadataBuilder.build())
        notificationManager.notify(SERVICE_ID, PlayNotification.buildNotification(this, mediaSession))
    }

    override fun onLoadChildren(parentId: String, result: Result<MutableList<MediaBrowserCompat.MediaItem>>) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onGetRoot(clientPackageName: String, clientUid: Int, rootHints: Bundle?): BrowserRoot? {
        return MediaBrowserServiceCompat.BrowserRoot("", null)
    }

    override fun onCreate() {
        super.onCreate()
        sessionToken = mediaSession.sessionToken
    }

    override fun onDestroy() {
        handleStopRequest(true)
        super.onDestroy()

    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        MediaButtonReceiver.handleIntent(mediaSession, intent)
        return super.onStartCommand(intent, flags, startId)
    }

    private fun handlePlayRequest(uri: Uri? = null) {
        audioBecomingNoisyReceiver = audioBecomingNoisyReceiver ?: BecomingNoisyReceiver()
        registerReceiver(audioBecomingNoisyReceiver, audioBecomingNoisyIntentFilter)
        if (uri != null) {
            player.play(uri)
        } else {
            player.play()
        }
        mediaSession.isActive = true
        startForeground(SERVICE_ID, PlayNotification.buildNotification(this, mediaSession))
    }

    private fun handleStopRequest(releasePlayer: Boolean = false) {
        player.stop(releasePlayer)
        mediaSession.isActive = false
        audioBecomingNoisyReceiver?.let { unregisterReceiver(it) }
        stopForeground(false)
        notificationManager.notify(SERVICE_ID, PlayNotification.buildNotification(this, mediaSession))
    }

    private fun setPlaybackState(state: PlaybackStateCompat) {
        try {
            mediaSession.setPlaybackState(state)
        } catch (e: IllegalStateException) {
            // TODO: Investigate why for some unknown reason we occasionally select an exception for
            // "beginBroadcast() called while already in a broadast"
            Timber.e(e)
        }
    }

    private inner class MediaSessionCallback : MediaSessionCompat.Callback() {

        override fun onPlayFromUri(uri: Uri?, extras: Bundle?) {
            handlePlayRequest(uri)
        }

        override fun onPlay() {
            handlePlayRequest()
        }

        override fun onPause() {
            handleStopRequest()
        }

        override fun onStop() {
            /**
             * Some extra sugar for handling pre-lollipop.
             * Service notification cant not be set dismissible if service is started in foreground.
             * (It can only be removed using stopForeground(true))
             * So we use Media Style cancel button to send ACTION_STOP only if the media session is inactive.
             * Which means the playback has already been stopped once, and this second call indicate the user
             * wants to select rid of the notification.
             */
            if (!mediaSession.isActive) {
                stopForeground(true)
            } else {
                handleStopRequest()
            }
        }
    }

    private inner class BecomingNoisyReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (AudioManager.ACTION_AUDIO_BECOMING_NOISY == intent.action) {
                handleStopRequest()
            }
        }
    }

}
