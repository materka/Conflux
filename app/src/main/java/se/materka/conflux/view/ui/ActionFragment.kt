package se.materka.conflux.view.ui

import android.os.Bundle
import android.support.design.widget.BottomSheetDialogFragment
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.franmontiel.fullscreendialog.FullScreenDialogFragment
import kotlinx.android.synthetic.main.menu_action.*
import org.koin.android.ext.android.inject
import se.materka.conflux.R
import se.materka.conflux.service.model.Station
import se.materka.conflux.service.datasource.StationDataSource

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

class ActionFragment : BottomSheetDialogFragment() {

    private var station: Station? = null

    private val stationRepository: StationDataSource by inject()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if (arguments != null) {
            station = arguments!!.getParcelable(InfoFragment.ARG_STATION)
        }
        return inflater.inflate(R.layout.menu_action, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (activity != null) {
            info.setOnClickListener {
                station?.let {
                    FullScreenDialogFragment.Builder(activity!!)
                            .setTitle("Information")
                            .setContent(InfoFragment::class.java, Bundle().apply { putParcelable(InfoFragment.ARG_STATION, station) })
                            .build()
                            .show(fragmentManager, "InfoFragment")
                }
                dismiss()
            }

            edit.setOnClickListener {
                station?.let {
                    FullScreenDialogFragment.Builder(activity!!)
                            .setTitle("Edit")
                            .setContent(EditFragment::class.java, Bundle().apply { putParcelable(EditFragment.ARG_STATION, station) })
                            .setConfirmButton("SAVE")
                            .build()
                            .show(fragmentManager, "EditFragment")
                }
                dismiss()
            }

            delete.setOnClickListener {
                station?.let {
                    AlertDialog.Builder(activity!!, R.style.AppTheme_WarningDialog)
                            .setTitle("Remove")
                            .setMessage("Are you sure you want to remove this station?")
                            .setPositiveButton("REMOVE") { _, _ ->
                                stationRepository.delete(it)
                            }
                            .setNegativeButton("CANCEL") { dialog, _ -> dialog.dismiss() }
                            .show()
                }
                dismiss()
            }
        }
    }

    companion object {
        val ARG_STATION = "station"

        fun newInstance(station: Station): ActionFragment {
            val fragment = ActionFragment()
            val args = Bundle()
            args.putParcelable(ARG_STATION, station)
            fragment.arguments = args
            return fragment
        }
    }
}