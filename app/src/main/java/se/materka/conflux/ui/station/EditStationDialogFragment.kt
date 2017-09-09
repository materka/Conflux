package se.materka.conflux.ui.station

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import kotlinx.android.synthetic.main.fragment_edit_station.*
import se.materka.conflux.FullScreenDialogFragment
import se.materka.conflux.R
import se.materka.conflux.ui.browse.BrowseViewModel

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

class EditStationDialogFragment : FullScreenDialogFragment() {

    private val browseViewModel: BrowseViewModel by lazy {
        ViewModelProviders.of(activity).get(BrowseViewModel::class.java)
    }

    private val station by lazy {
        browseViewModel.selected.value
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_edit_station, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        toolbar.let { t ->
            t.inflateMenu(R.menu.menu_edit_station)
            t.setOnMenuItemClickListener {
                station?.name = text_name.text.toString()
                station?.url = text_url.text.toString()
                browseViewModel.updateStation()
                dismiss()
                true
            }
        }
        text_name.setText(station?.name, TextView.BufferType.EDITABLE)
        text_url.setText(station?.url, TextView.BufferType.EDITABLE)
    }
}
