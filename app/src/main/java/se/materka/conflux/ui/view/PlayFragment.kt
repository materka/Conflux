package se.materka.conflux.ui.view

import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_play.*
import se.materka.conflux.R
import se.materka.conflux.databinding.FragmentPlayBinding
import se.materka.conflux.db.model.Station
import se.materka.conflux.ui.hideKeyboard

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

class PlayFragment : DialogFragment() {
    private val station: Station = Station()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding: FragmentPlayBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_play, container, false)
        binding.station = station
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        toggle_save_container.setOnCheckedChangeListener { _, _ ->
            save_container.visibility = if (save_container.visibility != View.GONE) View.GONE else View.VISIBLE
        }
        save_and_play.setOnClickListener { onConfirm() }
    }

    private fun onConfirm() {
        view?.hideKeyboard()
        /*dialogController?.confirm(Bundle().apply {
            putParcelable(EXTRA_STATION, station)
            putBoolean(EXTRA_SAVE_STATION, toggle_save_container.isChecked)
        })*/
    }

    companion object {
        val EXTRA_STATION: String = "se.materka.conflux.STATION"
        val EXTRA_SAVE_STATION: String = "se.materka.conflux.SAVE_STATION"
    }
}