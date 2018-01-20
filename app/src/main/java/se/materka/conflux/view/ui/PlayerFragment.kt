package se.materka.conflux.view.ui

import android.arch.lifecycle.Observer
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.media.MediaMetadataCompat
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_player.*
import org.koin.android.architecture.ext.getViewModel
import se.materka.conflux.databinding.FragmentPlayerBinding
import se.materka.conflux.ui.MetadataBinding
import se.materka.conflux.viewmodel.PlayerViewModel
import se.materka.exoplayershoutcastdatasource.ShoutcastMetadata


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

    val viewModel: PlayerViewModel? by lazy {
        activity?.getViewModel<PlayerViewModel>()
    }

    private val metadata = MetadataBinding()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding: FragmentPlayerBinding = FragmentPlayerBinding.inflate(inflater, container, false)
        viewModel?.metadata?.observe(this, Observer {
            text_artist.text = it?.getString(MediaMetadataCompat.METADATA_KEY_ARTIST)
            text_title.text = it?.getString(MediaMetadataCompat.METADATA_KEY_TITLE)
            text_show.text = it?.getString(ShoutcastMetadata.METADATA_KEY_SHOW)

            val albumArtUri = it?.getString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI) ?: ""
            if (albumArtUri.isNotEmpty()) {
                Picasso.with(context)
                        .load(albumArtUri)
                        .resize(image_cover.width, image_cover.height)
                        .centerCrop()
                        .into(image_cover)
                image_cover.visibility = View.VISIBLE
            }
        })

        binding.metadata = metadata
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        text_artist.addTextChangedListener(hideIfEmptyWatcher(text_artist, true))
        text_title.addTextChangedListener(hideIfEmptyWatcher(text_title, true))
        text_show.addTextChangedListener(hideIfEmptyWatcher(text_show, true))
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