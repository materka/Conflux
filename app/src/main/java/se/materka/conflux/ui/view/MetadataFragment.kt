package se.materka.conflux.ui.view

import android.arch.lifecycle.Observer
import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.media.MediaMetadataCompat
import android.transition.Scene
import android.transition.TransitionManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.koin.android.architecture.ext.getViewModel
import se.materka.conflux.R
import se.materka.conflux.databinding.FragmentMetadataCollapsedBinding
import se.materka.conflux.databinding.FragmentMetadataExpandedBinding
import se.materka.conflux.db.model.Station
import se.materka.conflux.ui.MetadataBinding
import se.materka.conflux.ui.viewmodel.StationViewModel
import se.materka.conflux.ui.viewmodel.MetadataViewModel
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

class MetadataFragment : Fragment() {

    companion object {
        enum class ViewState {
            COLLAPSED,
            EXPANDED
        }
    }

    interface Listener {
        fun onViewStateChanged(state: ViewState)
    }

    private val metadataViewModel: MetadataViewModel? by lazy {
        activity?.getViewModel<MetadataViewModel>()
    }

    private val stationViewModel: StationViewModel? by lazy {
        activity?.getViewModel<StationViewModel>()
    }

    private val metadata = MetadataBinding()

    private var listener: Listener? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        metadataViewModel?.metadata?.observe(this, Observer {
            metadata.artist = it?.getString(MediaMetadataCompat.METADATA_KEY_ARTIST)
            metadata.title = it?.getString(MediaMetadataCompat.METADATA_KEY_TITLE)
            metadata.show = it?.getString(ShoutcastMetadata.METADATA_KEY_SHOW)
            metadata.album = it?.getString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI)
        })

        metadataViewModel?.isPlaying?.observe(this, Observer { playing ->
            if (playing != true) {
                collapse()
            }
        })

        stationViewModel?.selected?.observe(this, Observer { station ->
            metadata.clear()
            metadata.station = station ?: Station().apply { name = ""; url = "" }
        })

        return inflater.inflate(R.layout.fragment_metadata, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        collapse(false)
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        if (context is MetadataFragment.Listener) {
            listener = context
        } else {
            throw RuntimeException(context!!.toString() + " must implement Listener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    private fun expand() {
        val binding = FragmentMetadataExpandedBinding.inflate(layoutInflater, view as ViewGroup, false)
        binding.metadata = metadata
        binding.root.setOnClickListener { collapse() }
        transition(binding.root)
        listener?.onViewStateChanged(ViewState.EXPANDED)
    }

    private fun collapse(updateListener: Boolean = true) {
        val binding = FragmentMetadataCollapsedBinding.inflate(layoutInflater, view as ViewGroup, false)
        binding.metadata = metadata
        binding.root.setOnClickListener { expand() }
        transition(binding.root)
        if (updateListener) {
            listener?.onViewStateChanged(ViewState.COLLAPSED)
        }
    }

    private fun transition(destination: View) {
        val scene = Scene(view as ViewGroup, destination)
        TransitionManager.go(scene)
    }
}