package se.materka.conflux.ui.player

import android.arch.lifecycle.LifecycleFragment
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.media.session.MediaControllerCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_player_compact.*
import se.materka.conflux.MetadataBinding
import se.materka.conflux.databinding.FragmentPlayerCompactBinding
import se.materka.conflux.utils.hideIfEmpty

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

class PlayerCompactFragment : LifecycleFragment() {

    private val playerViewModel: PlayerViewModel by lazy {
        ViewModelProviders.of(activity).get(PlayerViewModel::class.java)
    }

    private val metadata = MetadataBinding()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = FragmentPlayerCompactBinding.inflate(inflater, container, false)
        playerViewModel.metadata.observe(this, Observer {
            metadata.setArtist(it?.artist)
            metadata.setTitle(it?.title)
            metadata.setShow(it?.show)
        })

        playerViewModel.isPlaying.observe(this, Observer {
            if (it == true) {
                btn_toggle_play.apply {
                    showPause()
                    setOnClickListener { MediaControllerCompat.getMediaController(activity).transportControls.stop() }
                }
            } else {
                btn_toggle_play.apply {
                    showPlay()
                    setOnClickListener { MediaControllerCompat.getMediaController(activity).transportControls.play() }
                }
                metadata.clear()
            }
        })

        playerViewModel.cover.observe(this, Observer {
            Picasso.with(activity).load(it).into(image_cover)
        })

        binding.metadata = metadata
        return binding.root
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        text_artist.hideIfEmpty(true)
        text_title.hideIfEmpty(true)
        text_show.hideIfEmpty(true)
        btn_toggle_play.showPlay()
    }
}