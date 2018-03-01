package se.materka.conflux.service

import android.app.PendingIntent
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
import se.materka.conflux.ui.view.MainActivity
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

class MediaBrowserService : MediaBrowserServiceCompat() {

    companion object {
        val SERVICE_ID: Int = 1
    }

    private val mediaSession: MediaSessionCompat by lazy {

        val notificationIntent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val intent = PendingIntent.getActivity(applicationContext, 0,
                notificationIntent, 0)
        MediaSessionCompat(this@MediaBrowserService, MediaBrowserService::class.java.name).apply {
            setSessionActivity(intent)
            setPlaybackState(stateBuilder.build())
            setCallback(mediaSessionCallback)
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

    private val player: Player by lazy {
        Player(this, PlaybackCallback())
    }

    private val mediaSessionCallback: MediaSessionCompat.Callback = object : MediaSessionCompat.Callback() {

        override fun onPlayFromUri(uri: Uri?, extras: Bundle?) {
            play(uri)
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
        stop(true)
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
        player.stop(releasePlayer)
        mediaSession.isActive = false
        try {
            unregisterReceiver(audioBecomingNoisyReceiver)
        } catch (e: IllegalArgumentException) {
            Timber.i(e, "AudioBecomingNoisyReceiver already unregistered")
        }
        stopForeground(releasePlayer)
        if (!releasePlayer) {
            notificationManager.notify(SERVICE_ID, NotificationUtil.build(this, mediaSession))
        }
    }

    private fun setPlaybackState(state: PlaybackStateCompat) {
        try {
            mediaSession.setPlaybackState(state)
        } catch (e: IllegalStateException) {
            // TODO: Investigate why for some unknown reason we occasionally select an exception for
            // "beginBroadcast() called while already in a broadast"
            // RemoteCallbackList.beginBroadcast -> Måste kallas på samma tråd hela tiden. Kan vara så att den kallas på annan tråd
            Timber.e(e)
        }
    }

    private inner class PlaybackCallback : Player.Callback {
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
            val mediaMetadataBuilder: MediaMetadataCompat.Builder = MediaMetadataCompat.Builder()
                    .putString(ShoutcastMetadata.METADATA_KEY_TITLE, metadata.getString(ShoutcastMetadata.METADATA_KEY_TITLE))
                    .putString(ShoutcastMetadata.METADATA_KEY_ARTIST, metadata.getString(ShoutcastMetadata.METADATA_KEY_ARTIST))
                    .putString(ShoutcastMetadata.METADATA_KEY_SHOW, metadata.getString(ShoutcastMetadata.METADATA_KEY_SHOW))
                    .putLong(ShoutcastMetadata.METADATA_KEY_BITRATE, metadata.getLong(ShoutcastMetadata.METADATA_KEY_BITRATE))
                    .putLong(ShoutcastMetadata.METADATA_KEY_CHANNELS, metadata.getLong(ShoutcastMetadata.METADATA_KEY_CHANNELS))
                    .putString(ShoutcastMetadata.METADATA_KEY_FORMAT, metadata.getString(ShoutcastMetadata.METADATA_KEY_FORMAT))
                    .putString(ShoutcastMetadata.METADATA_KEY_STATION, metadata.getString(ShoutcastMetadata.METADATA_KEY_STATION))
                    .putString(ShoutcastMetadata.METADATA_KEY_URL, metadata.getString(ShoutcastMetadata.METADATA_KEY_URL))
            mediaSession.setMetadata(mediaMetadataBuilder.build())
            notificationManager.notify(SERVICE_ID, NotificationUtil.build(this@MediaBrowserService, mediaSession))
        }

    }
}
