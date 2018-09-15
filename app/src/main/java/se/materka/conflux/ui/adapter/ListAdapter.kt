package se.materka.conflux.ui.adapter

import android.support.v4.media.MediaBrowserCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.fragment_list_item.view.*
import se.materka.conflux.R
import se.materka.conflux.databinding.FragmentListItemBinding

/**
 * Copyright Mattias Karlsson

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

class ListAdapter(val onItemClicked: (item: MediaBrowserCompat.MediaItem) -> Unit?,
                  val onItemLongClicked: (item: MediaBrowserCompat.MediaItem) -> Unit?) : RecyclerView.Adapter<RecyclerView.ViewHolder>(), Filterable {

    override fun getFilter(): Filter = object : Filter() {
        override fun performFiltering(constraint: CharSequence?): FilterResults {
            val filtered = if (constraint != null && !constraint.isEmpty()) {
                val query: String = constraint.toString().toLowerCase()
                items.filter { it.description.title != null && it.description.title.toString().toLowerCase().contains(query) }.toMutableList()
            } else {
                items.toMutableList()
            }
            return FilterResults().apply {
                count = filtered.size
                values = filtered
            }
        }

        override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
            results?.let {filter ->
                this@ListAdapter.filtered = filter.values as List<MediaBrowserCompat.MediaItem>
                notifyDataSetChanged()
            }
        }
    }

    private var selected: MediaBrowserCompat.MediaItem? = null
    private lateinit var parent: RecyclerView

    var items: List<MediaBrowserCompat.MediaItem> = mutableListOf()
    private var filtered: List<MediaBrowserCompat.MediaItem> = mutableListOf()

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        parent = recyclerView
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ItemViewHolder) {
            filtered[position].let { item ->
                holder.bind(item)
                holder.itemView.selected?.visibility = if (item.mediaId == selected?.mediaId) View.VISIBLE else View.GONE
            }
        }
    }

    override fun getItemCount(): Int {
        return if (filtered.isEmpty()) 1 else filtered.size + 1
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == filtered.size) FOOTER_VIEW else R.layout.fragment_list_item
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == FOOTER_VIEW) {
            val view = inflater.inflate(R.layout.fragment_list_footer, parent, false)
            FooterViewHolder(view)
        } else {
            val binding: FragmentListItemBinding = DataBindingUtil.inflate(LayoutInflater.from(parent.context), viewType, parent, false)
            ItemViewHolder(binding)
        }
    }

    fun updateDataSet(items: List<MediaBrowserCompat.MediaItem>) {
        this.items = items
        this.filtered = this.items
        notifyDataSetChanged()
    }

    fun clearSelection() {
        select(-1)
    }

    private fun select(position: Int) {
        for (i in 0 until itemCount) {
            parent.findViewHolderForAdapterPosition(i)?.itemView?.selected?.visibility = View.GONE
        }
        selected = if (position == -1) null else filtered[position]
        notifyDataSetChanged()
    }

    inner class FooterViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    inner class ItemViewHolder(private val listItemBinding: FragmentListItemBinding) : RecyclerView.ViewHolder(listItemBinding.root) {

        init {
            listItemBinding.root.setOnClickListener {
                onItemClicked(filtered[adapterPosition])
                select(adapterPosition)
            }
            listItemBinding.root.setOnLongClickListener {
                onItemLongClicked(filtered[adapterPosition])
                true
            }
        }

        fun bind(item: MediaBrowserCompat.MediaItem) {
            listItemBinding.item = item
            listItemBinding.executePendingBindings()
        }
    }

    companion object {
        private const val FOOTER_VIEW = 1
    }
}