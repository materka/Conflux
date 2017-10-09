package se.materka.conflux.ui

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.ComponentName
import android.media.AudioManager
import android.os.Bundle
import android.support.design.widget.BottomSheetBehavior
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import com.franmontiel.fullscreendialog.FullScreenDialogFragment
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.toolbar.*
import se.materka.conflux.PlayService
import se.materka.conflux.R
import se.materka.conflux.domain.Station
import se.materka.conflux.ui.action.PlayFragment
import se.materka.conflux.ui.list.ListViewModel
import se.materka.conflux.ui.player.PlayerViewModel
import timber.log.Timber


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

    private val mediaBrowser: MediaBrowserCompat by lazy {
        MediaBrowserCompat(this,
                ComponentName(this, PlayService::class.java),
                connectionCallback,
                null)
    }

    private val playerViewModel: PlayerViewModel by lazy {
        ViewModelProviders.of(this).get(PlayerViewModel::class.java)
    }

    private val listViewModel: ListViewModel by lazy {
        ViewModelProviders.of(this).get(ListViewModel::class.java)
    }

    private val connectionCallback = object : MediaBrowserCompat.ConnectionCallback() {
        override fun onConnected() {
            // Create a MediaControllerCompat
            MediaControllerCompat(this@MainActivity, mediaBrowser.sessionToken).let { controller ->
                MediaControllerCompat.setMediaController(this@MainActivity, controller)
                controller.registerCallback(playerViewModel.mediaControllerCallback)
            }
        }

        override fun onConnectionSuspended() {
            MediaControllerCompat.getMediaController(this@MainActivity)?.let { controller ->
                controller.unregisterCallback(playerViewModel.mediaControllerCallback)
                MediaControllerCompat.setMediaController(this@MainActivity, null)
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

        play.setOnClickListener { showPlayDialog() }

        playerViewModel.isPlaying.observe(this, Observer { playing ->
            if (playing != false)
                setBottomSheetState(BottomSheetBehavior.STATE_COLLAPSED)
            else
                setBottomSheetState(BottomSheetBehavior.STATE_HIDDEN)
        })

        setBottomSheetState(BottomSheetBehavior.STATE_HIDDEN)
    }

    override fun onResume() {
        super.onResume()
        if (!mediaBrowser.isConnected) {
            mediaBrowser.connect()
        }
    }

    override fun onPause() {
        super.onPause()
        if (mediaBrowser.isConnected) {
            mediaBrowser.disconnect()
        }

    }

    private fun showPlayDialog() {
        FullScreenDialogFragment.Builder(this@MainActivity)
                .setTitle("Play")
                .setContent(PlayFragment::class.java, null)
                .setOnConfirmListener { bundle ->
                    val station: Station = Station().apply {
                        url = bundle?.getString(PlayFragment.EXTRA_STATION_URL)
                        name = bundle?.getString(PlayFragment.EXTRA_STATION_NAME, "")
                    }
                    if (bundle?.getBoolean(PlayFragment.EXTRA_SAVE_STATION, false) == true) {
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

