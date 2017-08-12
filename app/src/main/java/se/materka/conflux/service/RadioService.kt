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

import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.net.Uri
import android.os.Binder
import android.os.IBinder
import android.support.v4.app.NotificationCompat
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
import io.reactivex.subjects.PublishSubject
import okhttp3.OkHttpClient
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.info
import org.jetbrains.anko.notificationManager
import se.materka.conflux.R
import se.materka.conflux.ui.main.MainActivity
import se.materka.exoplayershoutcastplugin.Metadata
import se.materka.exoplayershoutcastplugin.ShoutcastDataSourceFactory
import se.materka.exoplayershoutcastplugin.ShoutcastMetadataListener
import java.util.*

@Suppress("JoinDeclarationAndAssignment")
class RadioService : Service(), ShoutcastMetadataListener, AnkoLogger {

    private var currentUri: Uri = Uri.EMPTY
    private var currentMetadata: Metadata? = null
        set(value) {
            field = value
            metadata.onNext(field)
        }
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

    private val binder by lazy {
        StreamServiceBinder()
    }

    private val uris by lazy {
        Stack<Uri>()
    }

    val metadata: PublishSubject<Metadata> = PublishSubject.create()
    val event: PublishSubject<RadioEvent> = PublishSubject.create()
    val isPlaying: PublishSubject<Boolean> = PublishSubject.create()

    override fun onCreate() {
        super.onCreate()
        player.addListener(ExoPlayerEventListener())
    }

    override fun onDestroy() {
        player.release()
        super.onDestroy()

    }

    override fun onBind(intent: Intent): IBinder? {
        return binder
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        if (intent.action != null) {
            when (intent.action) {
                ACTION_PLAY -> preparePlay(currentUri)
                ACTION_STOP -> stop()
            }
        }
        return START_STICKY
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        notificationManager.cancel(1)
    }

    override fun onMetadataReceived(data: Metadata) {
        info("MetadataBindable Received")
        currentMetadata = data
        buildNotification(buildAction(android.R.drawable.ic_media_pause, "Stop", RadioService.ACTION_STOP),
                true, data)
    }



    fun stop() {
        info("Stopping playback")
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
            if (uris.isEmpty() && PlaylistService.isPlayList(currentUri)) {
                PlaylistService.downloadFile(currentUri)
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
                            event.onNext(RadioEvent.ERROR_INVALID_URL)
                        })
            } else {
                play(currentUri)
            }
        }
    }

    private fun play(uri: Uri) {
        info("Starting playback - $uri")

        // This is the MediaSource representing the media to be played.
        audioSource = ExtractorMediaSource(uri,
                dataSourceFactory, DefaultExtractorsFactory(), null, null)
        player.prepare(audioSource)
        player.playWhenReady = true
    }

    private fun buildAction(icon: Int, title: String, intentAction: String): NotificationCompat.Action {
        val intent = Intent(this, RadioService::class.java)
        intent.action = intentAction
        val pendingIntent = PendingIntent.getService(this, 1, intent, 0)
        return NotificationCompat.Action.Builder(icon, title, pendingIntent).build()
    }

    private fun buildNotification(action: NotificationCompat.Action, ongoing: Boolean, metadata: Metadata?) {

        val notificationIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        val builder = NotificationCompat.Builder(this).apply {
            setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            setSmallIcon(R.mipmap.ic_launcher)
            setContentTitle(if (metadata != null) metadata.station else "")
            setContentText(if (metadata != null) metadata.artist + " - " + metadata.song else "")
            setContentIntent(contentIntent)
            setOngoing(ongoing)
            addAction(action)
        }

        if (!ongoing) {
            val intent = Intent(this, RadioService::class.java).apply {
                setAction(RadioService.ACTION_STOP)
            }
            builder.setDeleteIntent(PendingIntent.getService(this, 1, intent, 0))
        }
        notificationManager.notify(1, builder.build())
    }

    inner class StreamServiceBinder : Binder() {
        val service: RadioService
            get() = this@RadioService
    }

    private inner class ExoPlayerEventListener : ExoPlayer.EventListener {
        override fun onPlayerError(error: ExoPlaybackException?) {
            if (!uris.isEmpty()) {
                preparePlay(uris.pop())
            } else {
                event.onNext(RadioEvent.ERROR_NO_VALID_URL_FOUND)
            }
            info("PlaybackError: ${error?.cause.toString()}")
            if (error?.sourceException is HttpDataSource.InvalidResponseCodeException) {
                val responseCode = (error.sourceException as HttpDataSource.InvalidResponseCodeException).responseCode
                when (responseCode) {
                    404 -> event.onNext(RadioEvent.ERROR_INVALID_URL)
                }
            }
        }

        override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
            when (playbackState) {
                ExoPlayer.STATE_BUFFERING -> {
                    info("PlaybackState: Buffering")
                    event.onNext(RadioEvent.STATUS_LOADING)
                }
                ExoPlayer.STATE_ENDED -> {
                    info("PlaybackState: Ended")
                    event.onNext(RadioEvent.STATUS_STOPPED)
                    isPlaying.onNext(false)
                    buildNotification(buildAction(android.R.drawable.ic_media_play, "Play", RadioService.ACTION_PLAY),
                            false, null)
                }
                ExoPlayer.STATE_IDLE -> info("PlaybackState: Idle")
                ExoPlayer.STATE_READY -> {
                    info("PlaybackState: Ready")
                    if (playWhenReady) {
                        event.onNext(RadioEvent.STATUS_PLAYING)
                        isPlaying.onNext(true)
                    } else {
                        event.onNext(RadioEvent.STATUS_STOPPED)
                        isPlaying.onNext(false)
                        buildNotification(buildAction(android.R.drawable.ic_media_play, "Play", RadioService.ACTION_PLAY),
                                false, null)
                    }
                }
            }
        }

        override fun onLoadingChanged(isLoading: Boolean) {
            info("Loading: $isLoading")
        }

        override fun onPositionDiscontinuity() {
            info("onPositionDiscontinuity: discontinuity detected")
        }

        override fun onTimelineChanged(timeline: Timeline?, manifest: Any?) {
            info("onTimelineChanged: ${timeline?.toString()} ${manifest?.toString()}")
        }

        override fun onTracksChanged(trackGroups: TrackGroupArray?, trackSelections: TrackSelectionArray?) {
            info("onTracksChanged")
        }

    }

    companion object {
        val ACTION_PLAY = "action_play"
        val ACTION_STOP = "action_stop"
    }
}
