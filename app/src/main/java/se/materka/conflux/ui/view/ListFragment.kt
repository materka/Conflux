package se.materka.conflux.ui.view

import android.arch.lifecycle.Observer
import android.os.Bundle
import android.support.design.widget.BottomSheetDialog
import android.support.v4.app.Fragment
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.franmontiel.fullscreendialog.FullScreenDialogFragment
import kotlinx.android.synthetic.main.fragment_list.*
import kotlinx.android.synthetic.main.menu_action.view.*
import org.koin.android.architecture.ext.getViewModel
import se.materka.conflux.R
import se.materka.conflux.db.model.Station
import se.materka.conflux.ui.DividerItemDecoration
import se.materka.conflux.ui.adapter.ListAdapter
import se.materka.conflux.ui.viewmodel.StationViewModel


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

class ListFragment : Fragment() {

    private lateinit var listAdapter: ListAdapter

    private val stationViewModel: StationViewModel? by lazy {
        activity?.getViewModel<StationViewModel>()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        listAdapter = ListAdapter({ station -> itemClicked(station) },
                { station -> itemLongClicked(station) })

        list.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            adapter = listAdapter
            addItemDecoration(DividerItemDecoration(context, false, false))
        }

        stationViewModel?.get()?.observe(this, Observer<List<Station>> { stations ->
            if (stations != null) {
                listAdapter.updateDataSet(stations)
            }
        })
    }

    private fun itemClicked(station: Station) {
        stationViewModel?.select(station)
    }

    private fun itemLongClicked(station: Station) {
        stationViewModel?.select(station)
        val actionView = activity!!.layoutInflater.inflate(R.layout.menu_action, null)
        val actionDialog = BottomSheetDialog(activity!!)

        actionView.info.setOnClickListener {
            stationViewModel?.selected?.let {
                FullScreenDialogFragment.Builder(activity!!)
                        .setTitle("Information")
                        .setContent(InfoFragment::class.java, Bundle().apply { putParcelable(InfoFragment.ARG_STATION, it.value) })
                        .build()
                        .show(fragmentManager, "InfoFragment")
            }
            actionDialog.dismiss()
        }

        actionView.edit.setOnClickListener {
            stationViewModel?.selected?.let { station ->
                FullScreenDialogFragment.Builder(activity!!)
                        .setTitle("Edit")
                        .setContent(EditFragment::class.java, Bundle().apply { putParcelable(EditFragment.ARG_STATION, station.value) })
                        .setConfirmButton("SAVE")
                        .setOnConfirmListener {
                            stationViewModel?.update(station.value!!)
                        }
                        .build()
                        .show(fragmentManager, "EditFragment")
            }
            actionDialog.dismiss()
        }

        actionView.delete.setOnClickListener {
            stationViewModel?.selected?.let { station ->
                AlertDialog.Builder(activity!!, R.style.AppTheme_WarningDialog)
                        .setTitle("Remove")
                        .setMessage("Are you sure you want to remove this station?")
                        .setPositiveButton("REMOVE") { _, _ ->
                            stationViewModel?.delete(station.value!!)
                        }
                        .setNegativeButton("CANCEL") { dialog, _ -> dialog.dismiss() }
                        .show()
            }
            actionDialog.dismiss()
        }

        actionDialog.apply {
            setContentView(actionView)
            show()
        }
    }

}