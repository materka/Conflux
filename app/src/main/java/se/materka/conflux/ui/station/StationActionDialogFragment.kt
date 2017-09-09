package se.materka.conflux.ui.station

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.design.widget.BottomSheetDialogFragment
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.station_menu.*
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

class StationActionDialogFragment : BottomSheetDialogFragment() {
    private val browseViewModel: BrowseViewModel by lazy {
        ViewModelProviders.of(activity).get(BrowseViewModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.station_menu, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        station_edit.setOnClickListener {
            val dialog = EditStationDialogFragment()
            dialog.setStyle(DialogFragment.STYLE_NORMAL, R.style.DialogFragmentTheme)
            dialog.show(activity.supportFragmentManager, "DialogFragment")
            dismiss()
        }

        station_delete.setOnClickListener {
            AlertDialog.Builder(context, R.style.AppTheme_AlertDialog)
                    .setTitle("REMOVE STATION")
                    .setMessage("Are you sure you want to remove this station?")
                    .setPositiveButton("DO IT") { _, _ ->
                        browseViewModel.deleteStation()
                        this@StationActionDialogFragment.dismiss()
                    }
                    .setNegativeButton("CANCEL") { dialog, _ -> dialog.dismiss() }
                    .show()
        }
    }
}