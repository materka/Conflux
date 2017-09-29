package se.materka.conflux.ui.station

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.franmontiel.fullscreendialog.FullScreenDialogContent
import com.franmontiel.fullscreendialog.FullScreenDialogController
import kotlinx.android.synthetic.main.fragment_edit.*
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

class EditFragment : Fragment(), FullScreenDialogContent {
    private val browseViewModel: BrowseViewModel by lazy {
        ViewModelProviders.of(activity).get(BrowseViewModel::class.java)
    }

    private val station by lazy {
        browseViewModel.selected.value
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_edit, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        text_name.setText(station?.name, TextView.BufferType.EDITABLE)
        text_url.setText(station?.url, TextView.BufferType.EDITABLE)
    }

    override fun onConfirmClick(dialogController: FullScreenDialogController?): Boolean {
        station?.name = text_name.text.toString()
        station?.url = text_url.text.toString()
        browseViewModel.updateStation()
        return false
    }

    override fun onDialogCreated(dialogController: FullScreenDialogController?) {}

    override fun onDiscardClick(dialogController: FullScreenDialogController?): Boolean {
        return false
    }
}
