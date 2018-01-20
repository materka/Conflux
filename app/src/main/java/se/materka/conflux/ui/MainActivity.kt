package se.materka.conflux.ui

import android.arch.lifecycle.Observer
import android.content.ComponentName
import android.media.AudioManager
import android.net.Uri
import android.os.Bundle
import android.support.design.widget.BottomSheetBehavior
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.View
import com.franmontiel.fullscreendialog.FullScreenDialogFragment
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_player.*
import kotlinx.android.synthetic.main.toolbar.*
import org.koin.android.architecture.ext.getViewModel
import se.materka.conflux.R
import se.materka.conflux.service.model.Station
import se.materka.conflux.service.play.PlayService
import se.materka.conflux.view.ui.PlayFragment
import se.materka.conflux.viewmodel.ListViewModel
import se.materka.conflux.viewmodel.PlayerViewModel
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

class MainActivity : AppCompatActivity() {

    private val playerViewModel: PlayerViewModel by lazy {
        getViewModel<PlayerViewModel>()
    }

    private val listViewModel: ListViewModel by lazy {
        getViewModel<ListViewModel>()
    }

    private val mediaBrowser: MediaBrowserCompat by lazy {
        MediaBrowserCompat(this,
                ComponentName(this, PlayService::class.java),
                connectionCallback,
                null)
    }

    val mediaControllerCallback: MediaControllerCompat.Callback = object : MediaControllerCompat.Callback() {

        override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
            playerViewModel.onMetadataChanged(metadata)
        }

        override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
            playerViewModel.onPlaybackStateChanged(state)
        }
    }


    private val connectionCallback = object : MediaBrowserCompat.ConnectionCallback() {
        override fun onConnected() {
            // Create a MediaControllerCompat
            MediaControllerCompat(applicationContext, mediaBrowser.sessionToken).let { controller ->
                MediaControllerCompat.setMediaController(this@MainActivity, controller)
                controller.registerCallback(mediaControllerCallback)
            }
        }

        override fun onConnectionSuspended() {
            MediaControllerCompat.getMediaController(this@MainActivity).let { controller ->
                controller.unregisterCallback(mediaControllerCallback)
            }
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

        playerViewModel.isPlaying.observe(this, Observer { playing ->
            if (playing == true) {
                setBottomSheetState(BottomSheetBehavior.STATE_COLLAPSED)
                btn_toggle_play.apply {
                    showPause()
                    setOnClickListener { MediaControllerCompat.getMediaController(this@MainActivity)?.transportControls?.stop() }
                    image_cover.visibility = View.GONE
                    image_cover.setImageBitmap(null)

                }
            } else {
                setBottomSheetState(BottomSheetBehavior.STATE_HIDDEN)
                btn_toggle_play.apply {
                    showPlay()
                    setOnClickListener { MediaControllerCompat.getMediaController(this@MainActivity)?.transportControls?.play() }
                }
            }
        })

        playerViewModel.currentStation.observe(this, Observer
        {
            val uri: Uri = Uri.parse(it?.url)
            try {
                if (it?.url?.isEmpty() == true) throw IllegalArgumentException("URL is not set")
                MediaControllerCompat.getMediaController(this)?.transportControls?.playFromUri(uri, null)
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

    private fun showPlayDialog() {
        FullScreenDialogFragment.Builder(this@MainActivity)
                .setTitle("Play")
                .setContent(PlayFragment::class.java, null)
                .setOnConfirmListener { bundle ->
                    val station: Station? = bundle?.getParcelable(PlayFragment.EXTRA_STATION)
                    if (bundle?.getBoolean(PlayFragment.EXTRA_SAVE_STATION, false) == true && station != null) {
                        listViewModel.saveStation(station)
                    }
                    playerViewModel.play(station)
                }
                .setConfirmButton("PLAY")
                .build()
                .show(supportFragmentManager, PlayFragment::class.java.name)
    }

    private fun setBottomSheetState(state: Int) {
        BottomSheetBehavior.from(player_fragment.view).state = state
    }
}

