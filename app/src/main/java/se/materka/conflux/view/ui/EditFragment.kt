package se.materka.conflux.view.ui

import android.arch.lifecycle.ViewModelProviders
import android.databinding.DataBindingUtil
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
import se.materka.conflux.databinding.FragmentEditBinding
import se.materka.conflux.service.model.Station
import se.materka.conflux.viewmodel.ListViewModel
import se.materka.conflux.ui.Common

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

    private var station: Station? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            station = arguments!!.getParcelable(EditFragment.ARG_STATION)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding: FragmentEditBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_edit, container, false)
        binding.station = station
        return binding.root
    }

    override fun onConfirmClick(dialogController: FullScreenDialogController?): Boolean {
        view?.let {
            Common.hideKeyboard(context!!, it)
        }
        return false
    }

    override fun onDialogCreated(dialogController: FullScreenDialogController?) {}

    override fun onDiscardClick(dialogController: FullScreenDialogController?): Boolean {
        view?.let {
            Common.hideKeyboard(context!!, it)
        }
        return false
    }

    companion object {
        val ARG_STATION = "station"

        fun newInstance(station: String): EditFragment {
            val fragment = EditFragment()
            val args = Bundle()
            args.putString(ARG_STATION, station)
            fragment.arguments = args
            return fragment
        }
    }
}
