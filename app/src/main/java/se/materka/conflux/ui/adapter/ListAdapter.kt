package se.materka.conflux.ui.adapter

import android.databinding.DataBindingUtil
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.RecyclerView.ViewHolder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import se.materka.conflux.R
import se.materka.conflux.databinding.FragmentListItemBinding
import se.materka.conflux.db.model.Station


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

class ListAdapter(val onItemClicked: (station: Station) -> Unit?,
                  val onItemLongClicked: (station: Station) -> Unit?) : RecyclerView.Adapter<ViewHolder>() {
    companion object {
        private val FOOTER_VIEW = 1
    }

    // Define a view holder for Footer view
    inner class FooterViewHolder(itemView: View) : ViewHolder(itemView)

    inner class StationViewHolder(private val listItemBinding: FragmentListItemBinding) : ViewHolder(listItemBinding.root) {

        init {
            listItemBinding.root.setOnClickListener {
                onItemClicked(stations[adapterPosition])
            }
            listItemBinding.root.setOnLongClickListener {
                onItemLongClicked(stations[adapterPosition])
                true
            }
        }

        fun bind(station: Station) {
            listItemBinding.station = station
            listItemBinding.executePendingBindings()
        }
    }

    private var stations: List<Station> = mutableListOf()

    fun updateDataSet(items: List<Station>) {
        stations = items
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: ViewHolder?, position: Int) {
        try {
            if (holder is StationViewHolder) {
                stations[position].let { station ->
                    holder.bind(station)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun getItemCount(): Int {
        return if (stations.isEmpty()) 1 else stations.size + 1
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == stations.size) FOOTER_VIEW else R.layout.fragment_list_item
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder? {
        val inflater = LayoutInflater.from(parent.context)
        val vh: ViewHolder
        if (viewType == FOOTER_VIEW) {
            val view = inflater.inflate(R.layout.fragment_list_footer, parent, false)
            vh = FooterViewHolder(view)
        } else {
            val binding: FragmentListItemBinding = DataBindingUtil.inflate(LayoutInflater.from(parent.context), viewType, parent, false)
            vh = StationViewHolder(binding)
        }
        return vh
    }
}