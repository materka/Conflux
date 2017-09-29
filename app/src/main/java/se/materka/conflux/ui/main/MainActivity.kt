package se.materka.conflux.ui.main

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.media.AudioManager
import android.os.Bundle
import android.support.design.widget.BottomSheetBehavior
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.View
import com.franmontiel.fullscreendialog.FullScreenDialogFragment
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.toolbar.*
import se.materka.conflux.R
import se.materka.conflux.ui.browse.BrowseFragment
import se.materka.conflux.ui.player.PlayerViewModel
import se.materka.conflux.ui.station.PlayFragment


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
        ViewModelProviders.of(this).get(PlayerViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        volumeControlStream = AudioManager.STREAM_MUSIC
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar as Toolbar)

        playerViewModel.isPlaying.observe(this, Observer<Boolean> { playing ->
            BottomSheetBehavior.from(player.view).state = if (playing != false)
                BottomSheetBehavior.STATE_EXPANDED
            else
                BottomSheetBehavior.STATE_COLLAPSED
        })

        fab_play.setOnClickListener {
            FullScreenDialogFragment.Builder(this@MainActivity)
                    .setTitle("Play")
                    .setContent(PlayFragment::class.java, null)
                    .setConfirmButton("PLAY")
                    .build()
                    .show(supportFragmentManager, "PlayFragment")
        }


        supportFragmentManager.addOnBackStackChangedListener {
            if (supportFragmentManager.backStackEntryCount > 0) {
                fab_play.visibility =
                        if (supportFragmentManager.fragments.last().javaClass.name == BrowseFragment::class.java.name)
                            View.VISIBLE
                        else
                            View.GONE
            }
        }

        switchFragment(BrowseFragment())
    }

    private fun switchFragment(fragment: Fragment?) {
        if (fragment != null) {
            supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.content, fragment, fragment.javaClass.name)
                    .addToBackStack(null)
                    .commit()
        }
    }
}

