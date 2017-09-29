package se.materka.conflux.ui.player

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.ComponentName
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_player.*
import se.materka.conflux.MetadataBinding
import se.materka.conflux.PlayService
import se.materka.conflux.databinding.FragmentPlayerBinding
import se.materka.exoplayershoutcastdatasource.ShoutcastMetadata
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

class PlayerFragment : Fragment() {

    private lateinit var mediaBrowser: MediaBrowserCompat
    private val metadata = MetadataBinding()

    private val playerViewModel: PlayerViewModel by lazy {
        ViewModelProviders.of(activity).get(PlayerViewModel::class.java)
    }

    private val connectionCallback = object : MediaBrowserCompat.ConnectionCallback() {
        override fun onConnected() {
            // Create a MediaControllerCompat
            MediaControllerCompat(activity, mediaBrowser.sessionToken).let { controller ->
                MediaControllerCompat.setMediaController(activity, controller)
                controller.registerCallback(playerViewModel.mediaControllerCallback)
            }
        }

        override fun onConnectionSuspended() {
            MediaControllerCompat.getMediaController(activity)?.let { controller ->
                controller.unregisterCallback(playerViewModel.mediaControllerCallback)
                MediaControllerCompat.setMediaController(activity, null)
            }
        }

        override fun onConnectionFailed() {
            Timber.e("connection failed")
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = FragmentPlayerBinding.inflate(inflater, container, false)
        playerViewModel.metadata.observe(this, Observer {
            metadata.setArtist(it?.getString(MediaMetadataCompat.METADATA_KEY_ARTIST))
            metadata.setTitle(it?.getString(MediaMetadataCompat.METADATA_KEY_TITLE))
            metadata.setShow(it?.getString(ShoutcastMetadata.METADATA_KEY_SHOW))
            Picasso.with(activity)
                    .load(it?.getString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI))
                    .resize(image_cover.width, image_cover.height)
                    .centerCrop()
                    .into(image_cover)
        })

        playerViewModel.isPlaying.observe(this, Observer {
            if (it == true) {
                btn_toggle_play.apply {
                    showPause()
                    setOnClickListener { MediaControllerCompat.getMediaController(activity).transportControls.stop() }
                    image_cover.setImageBitmap(null)
                }
            } else {
                btn_toggle_play.apply {
                    showPlay()
                    setOnClickListener { MediaControllerCompat.getMediaController(activity).transportControls.play() }
                }
                metadata.clear()
            }
        })

        playerViewModel.currentStation.observe(this, Observer {
            val uri: Uri = Uri.parse(it?.url)
            MediaControllerCompat.getMediaController(activity).transportControls?.playFromUri(uri, null)
        })

        binding.metadata = metadata
        return binding.root
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        text_artist.addTextChangedListener(hideIfEmptyWatcher(text_artist, true))
        text_title.addTextChangedListener(hideIfEmptyWatcher(text_title, true))
        text_show.addTextChangedListener(hideIfEmptyWatcher(text_show, true))
        btn_toggle_play.showPlay()
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        mediaBrowser = MediaBrowserCompat(activity,
                ComponentName(activity, PlayService::class.java),
                connectionCallback, null)
        mediaBrowser.connect()
    }

    private fun hideIfEmptyWatcher(view: View, hide: Boolean): TextWatcher {
        return object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                view.visibility = if ((s == null || s.isNullOrEmpty()) && hide) View.GONE else View.VISIBLE
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        }
    }
}