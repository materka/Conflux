/**
 * Copyright 2016 Mattias Karlsson

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

package se.materka.conflux

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.NotificationCompat
import android.support.v4.content.ContextCompat
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaBrowserServiceCompat
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaButtonReceiver
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import se.materka.conflux.utils.TAG
import se.materka.exoplayershoutcastdatasource.ShoutcastMetadata

@Suppress("JoinDeclarationAndAssignment")
class PlayService : MediaBrowserServiceCompat(), Playback.Callback {

    private val SERVICE_ID: Int = 42

    private val mediaSession: MediaSessionCompat by lazy {
        MediaSessionCompat(this@PlayService, TAG).apply {
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

    private val player: Playback by lazy {
        Playback(this).apply {
            callback = this@PlayService
        }
    }

    override fun onPlaybackStateChanged(state: Int) {
        mediaSession.setPlaybackState(stateBuilder.setState(state, 0L, 0f).build())
    }

    override fun onError(errorCode: Int, error: String) {
        val state = stateBuilder
                .setState(PlaybackStateCompat.STATE_ERROR, 0L, 0f)
                .setErrorMessage(404, "No working url found")
                .build()
        mediaSession.setPlaybackState(state)
    }

    override fun onMetadataReceived(data: ShoutcastMetadata) {
        val metadata: MediaMetadataCompat = metadataBuilder
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, data.song)
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, data.artist)
                .build()
        mediaSession.setMetadata(metadata)
        buildNotification()
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
        player.stop(true)
        super.onDestroy()

    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        MediaButtonReceiver.handleIntent(mediaSession, intent)
        return super.onStartCommand(intent, flags, startId)
    }

    private fun buildNotification() {
        // Given a media session and its context (usually the component containing the session)
        // Create a NotificationCompat.Builder

        // Get the session's metadata
        val controller: MediaControllerCompat = mediaSession.controller
        val mediaMetadata: MediaMetadataCompat? = controller.metadata
        val description: MediaDescriptionCompat? = mediaMetadata?.description

        val builder: NotificationCompat.Builder = NotificationCompat.Builder(applicationContext, "miscellaneous")

        builder
                // Add the metadata for the currently playing track
                .setContentTitle(description?.title)
                .setContentText(description?.subtitle)
                .setSubText(description?.description)
                .setLargeIcon(description?.iconBitmap)
                .setSmallIcon(R.drawable.md_play)
                .setColor(ContextCompat.getColor(this, R.color.primary_dark))
                // Enable launching the player by clicking the notification
                .setContentIntent(controller.sessionActivity)
                // Stop the service when the notification is swiped away
                .setDeleteIntent(MediaButtonReceiver.buildMediaButtonPendingIntent(applicationContext,
                        PlaybackStateCompat.ACTION_STOP))
                // Make the transport controls visible on the lockscreen
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                // Add a pause button
                .addAction(
                        if (!mediaSession.isActive)
                            NotificationCompat.Action(
                                    R.drawable.md_play, "PLAY",
                                    MediaButtonReceiver.buildMediaButtonPendingIntent(applicationContext,
                                            PlaybackStateCompat.ACTION_PLAY)
                            )
                        else
                            NotificationCompat.Action(
                                    R.drawable.md_pause, "PAUS",
                                    MediaButtonReceiver.buildMediaButtonPendingIntent(applicationContext,
                                            PlaybackStateCompat.ACTION_STOP)
                            )
                )

                // Take advantage of MediaStyle features
                .setStyle(android.support.v4.media.app.NotificationCompat.MediaStyle()
                        .setMediaSession(mediaSession.sessionToken)
                        .setShowActionsInCompactView(0)
                        .setShowCancelButton(true)
                        .setCancelButtonIntent(
                                MediaButtonReceiver.buildMediaButtonPendingIntent(applicationContext,
                                        PlaybackStateCompat.ACTION_STOP)))
        startForeground(SERVICE_ID, builder.build())
    }

    private inner class MediaSessionCallback : MediaSessionCompat.Callback() {

        override fun onPlayFromUri(uri: Uri?, extras: Bundle?) {
            uri?.let {
                mediaSession.isActive = true
                player.play(uri)
                buildNotification()
            }
        }

        override fun onPlay() {
            mediaSession.isActive = true
            player.play()
            buildNotification()
        }

        override fun onStop() {
            mediaSession.isActive = false
            player.stop()
            buildNotification()
        }
    }

}
