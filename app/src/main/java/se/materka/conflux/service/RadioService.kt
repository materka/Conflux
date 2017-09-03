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

package se.materka.conflux.service

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
import android.util.Log
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.TrackGroupArray
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.trackselection.TrackSelectionArray
import com.google.android.exoplayer2.upstream.HttpDataSource
import com.google.android.exoplayer2.util.Util
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import okhttp3.OkHttpClient
import org.jetbrains.anko.notificationManager
import se.materka.conflux.R
import se.materka.conflux.TAG
import se.materka.exoplayershoutcastplugin.Metadata
import se.materka.exoplayershoutcastplugin.ShoutcastDataSourceFactory
import se.materka.exoplayershoutcastplugin.ShoutcastMetadataListener
import java.util.*

@Suppress("JoinDeclarationAndAssignment")
class RadioService : MediaBrowserServiceCompat(), ShoutcastMetadataListener {
    override fun onLoadChildren(parentId: String, result: Result<MutableList<MediaBrowserCompat.MediaItem>>) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onGetRoot(clientPackageName: String, clientUid: Int, rootHints: Bundle?): BrowserRoot? {
        return MediaBrowserServiceCompat.BrowserRoot("", null)
    }

    private var currentUri: Uri? = null

    private val player: SimpleExoPlayer by lazy {
        ExoPlayerFactory.newSimpleInstance(applicationContext,
                DefaultTrackSelector(),
                DefaultLoadControl())
    }

    private val dataSourceFactory: ShoutcastDataSourceFactory by lazy {
        ShoutcastDataSourceFactory(
                OkHttpClient.Builder().build(),
                Util.getUserAgent(applicationContext, getString(R.string.app_name)),
                null,
                this)
    }

    private var audioSource: MediaSource? = null

    private val uris by lazy {
        Stack<Uri>()
    }

    private val mediaSession: MediaSessionCompat by lazy {
        MediaSessionCompat(this@RadioService, TAG).apply {
            setPlaybackState(stateBuilder.build())
            setCallback(MediaSessionCallback())
            setFlags(MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS
                    or MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS)
        }
    }

    private val stateBuilder: PlaybackStateCompat.Builder by lazy {
        PlaybackStateCompat.Builder()
                .setActions(PlaybackStateCompat.ACTION_PLAY
                        or PlaybackStateCompat.ACTION_PLAY_PAUSE
                        or PlaybackStateCompat.ACTION_STOP)
    }

    private val metadataBuilder: MediaMetadataCompat.Builder by lazy {
        MediaMetadataCompat.Builder()
    }

    override fun onCreate() {
        super.onCreate()
        player.addListener(ExoPlayerEventListener())
        sessionToken = mediaSession.sessionToken
    }

    override fun onDestroy() {
        player.release()
        super.onDestroy()

    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        MediaButtonReceiver.handleIntent(mediaSession, intent)
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        notificationManager.cancel(1)
    }

    override fun onMetadataReceived(data: Metadata) {
        Log.i(TAG, "Metadata Received")
        val metadata: MediaMetadataCompat = metadataBuilder
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, data.song)
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, data.artist)
                .build()
        mediaSession.setMetadata(metadata)
        buildNotification()
    }


    fun stop() {
        Log.i(TAG, "Stopping playback")
        if (player.playWhenReady) {
            player.stop()
            player.playWhenReady = false
            audioSource?.releaseSource()
        }
    }

    fun preparePlay(uri: Uri) {
        if (player.playWhenReady) {
            stop()
        }

        if (uri != Uri.EMPTY) {
            currentUri = uri
            currentUri?.let {
                if (uris.isEmpty() && PlaylistService.isPlayList(it)) {
                    PlaylistService.downloadFile(it)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe({ list ->
                                // OnNext
                                if (list != null && !list.isEmpty()) {
                                    uris.addAll(list)
                                    play(uris.pop())
                                }
                            }, {
                                // OnError
                            })
                } else {
                    play(it)
                }
            }

        }
    }

    private fun play(uri: Uri) {
        Log.i(TAG, "Starting playback - $uri")

        // This is the MediaSource representing the media to be played.
        audioSource = ExtractorMediaSource(uri,
                dataSourceFactory, DefaultExtractorsFactory(), null, null)
        player.prepare(audioSource)
        player.playWhenReady = true
    }

    private fun buildNotification() {
        // Given a media session and its context (usually the component containing the session)
        // Create a NotificationCompat.Builder

        // Get the session's metadata
        val controller: MediaControllerCompat = mediaSession.controller
        val mediaMetadata: MediaMetadataCompat? = controller.metadata
        val description: MediaDescriptionCompat? = mediaMetadata?.description

        val builder: NotificationCompat.Builder = NotificationCompat.Builder(applicationContext, "")

        builder
                // Add the metadata for the currently playing track
                .setContentTitle(description?.title)
                .setContentText(description?.subtitle)
                .setSubText(description?.description)
                .setLargeIcon(description?.iconBitmap)

                // Enable launching the player by clicking the notification
                .setContentIntent(controller.sessionActivity)

                // Stop the service when the notification is swiped away
                .setDeleteIntent(MediaButtonReceiver.buildMediaButtonPendingIntent(applicationContext,
                        PlaybackStateCompat.ACTION_STOP))

                // Make the transport controls visible on the lockscreen
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

                // Add an app icon and set its accent color
                // Be careful about the color
                .setSmallIcon(android.R.drawable.ic_media_play)
                .setColor(ContextCompat.getColor(this, R.color.primary_dark))

                // Add a pause button
                .addAction(NotificationCompat.Action(
                        android.R.drawable.ic_media_pause, "PAUS",
                        MediaButtonReceiver.buildMediaButtonPendingIntent(applicationContext,
                                PlaybackStateCompat.ACTION_STOP)))

                // Take advantage of MediaStyle features
                .setStyle(android.support.v4.media.app.NotificationCompat.MediaStyle()
                        .setMediaSession(mediaSession.sessionToken)
                        .setShowActionsInCompactView(0)
                        .setShowCancelButton(true)
                        .setCancelButtonIntent(
                                MediaButtonReceiver.buildMediaButtonPendingIntent(applicationContext,
                                        PlaybackStateCompat.ACTION_STOP)))
        startForeground(42, builder.build())
    }

    private fun handlePlayerError(error: ExoPlaybackException?) {
        if (!uris.isEmpty()) {
            preparePlay(uris.pop())
        } else {
            val state = stateBuilder
                    .setState(PlaybackStateCompat.STATE_ERROR, 0L, 0f)
                    .setErrorMessage(404, "No working url found")
                    .build()
            mediaSession.setPlaybackState(state)
        }
        Log.i(TAG, "PlaybackError: ${error?.cause.toString()}")
        if (error?.sourceException is HttpDataSource.InvalidResponseCodeException) {
            val responseCode = (error.sourceException as HttpDataSource.InvalidResponseCodeException).responseCode
            when (responseCode) {
                404 -> {
                    val state = stateBuilder
                            .setState(PlaybackStateCompat.STATE_ERROR, 0L, 0f)
                            .setErrorMessage(404, "Url not found")
                            .build()
                    mediaSession.setPlaybackState(state)
                }
            }
        }
    }

    private fun handlePlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
        when (playbackState) {
            ExoPlayer.STATE_BUFFERING -> {
                Log.i(TAG, "PlaybackState: Buffering")
                setPlaybackState(PlaybackStateCompat.STATE_BUFFERING)
            }
            ExoPlayer.STATE_ENDED -> {
                Log.i(TAG, "PlaybackState: Ended")
                setPlaybackState(PlaybackStateCompat.STATE_STOPPED)
                stopForeground(true)
            }
            ExoPlayer.STATE_IDLE -> {
                Log.i(TAG, "PlaybackState: Idle")
                setPlaybackState(PlaybackStateCompat.STATE_NONE)
            }
            ExoPlayer.STATE_READY -> {
                Log.i(TAG, "PlaybackState: Ready")
                if (playWhenReady) {
                    setPlaybackState(PlaybackStateCompat.STATE_PLAYING)
                    buildNotification()
                } else {
                    setPlaybackState(PlaybackStateCompat.STATE_STOPPED)
                    stopForeground(true)
                }
            }
        }
    }

    private fun setPlaybackState(state: Int) {
        mediaSession.setPlaybackState(stateBuilder.setState(state, 0L, 0f).build())
    }

    private inner class ExoPlayerEventListener : ExoPlayer.EventListener {
        override fun onPlayerError(error: ExoPlaybackException?) {
            handlePlayerError(error)
        }

        override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
            handlePlayerStateChanged(playWhenReady, playbackState)
        }

        override fun onLoadingChanged(isLoading: Boolean) {
            Log.i(TAG, "Loading: $isLoading")
        }

        override fun onPositionDiscontinuity() {
            Log.i(TAG, "onPositionDiscontinuity: discontinuity detected")
        }

        override fun onTimelineChanged(timeline: Timeline?, manifest: Any?) {
            Log.i(TAG, "onTimelineChanged: ${timeline?.toString()} ${manifest?.toString()}")
        }

        override fun onTracksChanged(trackGroups: TrackGroupArray?, trackSelections: TrackSelectionArray?) {
            Log.i(TAG, "onTracksChanged")
        }
    }

    private inner class MediaSessionCallback : MediaSessionCompat.Callback() {

        override fun onPlayFromUri(uri: Uri?, extras: Bundle?) {
            uri?.let {
                currentUri = uri
                mediaSession.isActive = true
                play(it)
                buildNotification()
            }
        }

        override fun onPlay() {
            Log.d(TAG, "play")
            currentUri?.let {
                play(it)
            }
        }

        override fun onStop() {
            mediaSession.isActive = false
            stop()
        }
    }

}
