package se.materka.conflux

import android.content.ComponentName
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.lifecycle.MutableLiveData

class RadioSession(context: Context, componentName: ComponentName) {
    @Suppress("PropertyName")
    val STATE_NONE: PlaybackStateCompat = PlaybackStateCompat.Builder()
            .setState(PlaybackStateCompat.STATE_NONE, 0, 0f)
            .build()

    @Suppress("PropertyName")
    val NOTHING_PLAYING: MediaMetadataCompat = MediaMetadataCompat.Builder()
            .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, "")
            .build()

    val connected = MutableLiveData<Boolean>()
            .apply { postValue(false) }

    val playbackState = MutableLiveData<PlaybackStateCompat>()
            .apply { postValue(STATE_NONE) }

    val nowPlaying = MutableLiveData<MediaMetadataCompat>()
            .apply { postValue(NOTHING_PLAYING) }

    private val mediaBrowser: MediaBrowserCompat by lazy {
        MediaBrowserCompat(context,
                componentName,
                connectionCallback,
                null).apply {
            connect()
        }
    }

    private lateinit var mediaController: MediaControllerCompat

    val mediaControllerCallback: MediaControllerCompat.Callback = object : MediaControllerCompat.Callback() {


        override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
            nowPlaying.postValue(metadata)
        }

        override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
            playbackState.postValue(state)
            /*val bottomSheetState: Int = if (playbackState?.state == PlaybackStateCompat.STATE_PLAYING) {
                BottomSheetBehavior.STATE_COLLAPSED
            } else {
                BottomSheetBehavior.STATE_HIDDEN
            }

            //setBottomSheetState(bottomSheetState)
            //metadataViewModel.onPlaybackStateChanged(playbackState)

            if (playbackState?.state == PlaybackStateCompat.STATE_ERROR) {
                Snackbar.make(coordinator, playbackState.errorMessage, Snackbar.LENGTH_LONG).show()
            }*/
        }
    }

    fun subscribe(parentId: String, callback: MediaBrowserCompat.SubscriptionCallback) {
        mediaBrowser.subscribe(parentId, callback)
    }

    fun unsubscribe(parentId: String) {
        mediaBrowser.unsubscribe(parentId)
    }

    fun unsubscribe(parentId: String, callback: MediaBrowserCompat.SubscriptionCallback) {
        mediaBrowser.unsubscribe(parentId, callback)
    }

    fun play(item: MediaBrowserCompat.MediaItem) {
        mediaController.transportControls.playFromMediaId(item.mediaId, Bundle.EMPTY)
    }

    fun play(uri: Uri) {
        mediaController.transportControls.playFromUri(uri, Bundle.EMPTY)
    }

    fun stop() {
        mediaController.transportControls.stop()
    }

    private val connectionCallback = object : MediaBrowserCompat.ConnectionCallback() {
        override fun onConnected() {
            mediaController = MediaControllerCompat(context, mediaBrowser.sessionToken).apply {
                registerCallback(mediaControllerCallback)
            }
            connected.postValue(true)
        }

        override fun onConnectionSuspended() {
            mediaController.unregisterCallback(mediaControllerCallback)
            connected.postValue(false)
        }

        override fun onConnectionFailed() {
            error("Connection failed")
        }
    }
}