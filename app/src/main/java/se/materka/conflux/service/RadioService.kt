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

import android.app.Service
import android.content.Intent
import android.net.Uri
import android.os.Binder
import android.os.IBinder
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
import se.materka.conflux.R
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

    override fun onMetadataReceived(data: Metadata) {
        info("MetadataBindable Received")
        currentMetadata = data
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
