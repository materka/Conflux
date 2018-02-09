package se.materka.conflux.ui.view

import android.arch.lifecycle.Observer
import android.content.ComponentName
import android.content.Context
import android.media.AudioManager
import android.net.Uri
import android.os.Bundle
import android.support.design.widget.AppBarLayout
import android.support.design.widget.BottomSheetBehavior
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import com.mikepenz.iconics.context.IconicsContextWrapper
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.toolbar.*
import org.koin.android.architecture.ext.getViewModel
import se.materka.conflux.R
import se.materka.conflux.db.model.Station
import se.materka.conflux.service.MediaBrowserService
import se.materka.conflux.ui.viewmodel.MetadataViewModel
import se.materka.conflux.ui.viewmodel.StationViewModel
import timber.log.Timber
import java.lang.IllegalArgumentException


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

class MainActivity : AppCompatActivity(), MetadataFragment.Listener {

    private val metadataViewModel: MetadataViewModel by lazy {
        getViewModel<MetadataViewModel>()
    }

    private val stationViewModel: StationViewModel by lazy {
        getViewModel<StationViewModel>()
    }

    private val mediaBrowser: MediaBrowserCompat by lazy {
        MediaBrowserCompat(this,
                ComponentName(this, MediaBrowserService::class.java),
                connectionCallback,
                null)
    }

    private val mediaController: MediaControllerCompat by lazy {
        MediaControllerCompat(this@MainActivity, mediaBrowser.sessionToken)
    }

    val mediaControllerCallback: MediaControllerCompat.Callback = object : MediaControllerCompat.Callback() {

        override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
            metadataViewModel.onMetadataChanged(metadata, stationViewModel.selected.value)
        }

        override fun onPlaybackStateChanged(playbackState: PlaybackStateCompat?) {
            val bottomSheetState: Int = if (playbackState?.state == PlaybackStateCompat.STATE_PLAYING) {
                BottomSheetBehavior.STATE_COLLAPSED
            } else {
                BottomSheetBehavior.STATE_HIDDEN
            }
            setBottomSheetState(bottomSheetState)
            metadataViewModel.onPlaybackStateChanged(playbackState)
        }
    }


    private val connectionCallback = object : MediaBrowserCompat.ConnectionCallback() {
        override fun onConnected() {
            // Create a MediaControllerCompat
            MediaControllerCompat.setMediaController(this@MainActivity, mediaController)
            mediaController.registerCallback(mediaControllerCallback)
        }

        override fun onConnectionSuspended() {
            mediaController.unregisterCallback(mediaControllerCallback)
        }

        override fun onConnectionFailed() {
            Timber.e("connection failed")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        volumeControlStream = AudioManager.STREAM_MUSIC
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar as Toolbar)

        btn_toggle_play.showPlay()

        metadataViewModel.isPlaying.observe(this, Observer { playing ->
            if (playing == true) {
                setBottomSheetState(BottomSheetBehavior.STATE_COLLAPSED)
                btn_toggle_play.apply {
                    showPause()
                    setOnClickListener { mediaController.transportControls?.stop() }
                }
            } else {
                setBottomSheetState(BottomSheetBehavior.STATE_HIDDEN)
                btn_toggle_play.apply {
                    showPlay()
                    setOnClickListener { mediaController.transportControls?.play() }
                }
            }
        })

        stationViewModel.selected.observe(this, Observer
        {
            val uri: Uri = Uri.parse(it?.url)
            try {
                if (it?.url?.isEmpty() == true) throw IllegalArgumentException("URL is not set")
                mediaController.transportControls?.playFromUri(uri, null)
            } catch (e: IllegalArgumentException) {
                AlertDialog.Builder(this, R.style.AppTheme_ErrorDialog)
                        .setTitle("Error")
                        .setMessage(e.message)
                        .setNegativeButton("OK") { dialog, _ -> dialog.dismiss() }
                        .show()
            }
        })

        setBottomSheetState(BottomSheetBehavior.STATE_HIDDEN)
    }

    override fun onResume() {
        super.onResume()
        if (!mediaBrowser.isConnected) {
            mediaBrowser.connect()
        }
    }

    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(IconicsContextWrapper.wrap(newBase))
    }

    override fun onViewStateChanged(state: MetadataFragment.Companion.ViewState) {
        when (state) {
            MetadataFragment.Companion.ViewState.COLLAPSED -> setBottomSheetState(BottomSheetBehavior.STATE_COLLAPSED)
            MetadataFragment.Companion.ViewState.EXPANDED -> setBottomSheetState(BottomSheetBehavior.STATE_EXPANDED)
        }
    }

    private fun showPlayDialog() {
        /*FullScreenDialogFragment.Builder(this@MainActivity)
                .setTitle("Play")
                .setContent(PlayFragment::class.java, null)
                .setOnConfirmListener { bundle ->
                    val station: Station? = bundle?.getParcelable(PlayFragment.EXTRA_STATION)
                    if (bundle?.getBoolean(PlayFragment.EXTRA_SAVE_STATION, false) == true && station != null) {
                        stationViewModel.save(station)
                    }
                }
                .setConfirmButton("PLAY")
                .build()
                .show(supportFragmentManager, PlayFragment::class.java.name)*/
    }

    private fun setBottomSheetState(state: Int) {
        BottomSheetBehavior.from(metadata.view).let {
            if (it.state != state) {
                it.state = state
                (toolbar.parent as AppBarLayout).setExpanded(state != BottomSheetBehavior.STATE_EXPANDED, true)

            }
        }
    }
}

