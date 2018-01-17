package se.materka.conflux.ui

import android.arch.lifecycle.Observer
import android.media.AudioManager
import android.os.Bundle
import android.support.design.widget.BottomSheetBehavior
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import com.franmontiel.fullscreendialog.FullScreenDialogFragment
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.toolbar.*
import org.koin.android.architecture.ext.getViewModel
import se.materka.conflux.R
import se.materka.conflux.service.model.Station
import se.materka.conflux.view.ui.PlayFragment
import se.materka.conflux.viewmodel.ListViewModel
import se.materka.conflux.viewmodel.PlayerViewModel


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

