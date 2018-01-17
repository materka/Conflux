package se.materka.conflux.ui.list

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_list.*
import se.materka.conflux.R
import se.materka.conflux.domain.Station
import se.materka.conflux.ui.DividerItemDecoration
import se.materka.conflux.ui.action.ActionFragment
import se.materka.conflux.ui.player.PlayerViewModel

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

    private val listViewModel: ListViewModel? by lazy {
        if (activity != null) ViewModelProviders.of(activity!!).get(ListViewModel::class.java) else null
    }

    private val playerViewModel: PlayerViewModel? by lazy {
        if (activity != null) ViewModelProviders.of(activity!!).get(PlayerViewModel::class.java) else null
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater?.inflate(R.layout.fragment_list, container, false)
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

        listViewModel?.getStations()?.observe(this, Observer<List<Station>> { stations ->
            if (stations != null) {
                listAdapter.updateDataSet(stations)
            }
        })
    }

    private fun itemClicked(station: Station) {
        listViewModel?.select(station)
        playerViewModel?.play(station)
    }

    private fun itemLongClicked(station: Station) {
        listViewModel?.select(station)
        ActionFragment().show(activity?.supportFragmentManager, "ActionFragment")
    }

}