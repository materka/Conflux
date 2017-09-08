package se.materka.conflux.ui.browse

import android.support.v7.widget.RecyclerView
import android.support.v7.widget.RecyclerView.ViewHolder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.station_list_item_layout.view.*
import se.materka.conflux.R
import se.materka.conflux.database.Station

class BrowseAdapter(val onItemClicked: (station: Station) -> Unit?,
                    val onItemLongClicked: (station: Station) -> Unit?) : RecyclerView.Adapter<ViewHolder>() {
    private val FOOTER_VIEW = 1

    // Define a view holder for Footer view
    inner class FooterViewHolder(itemView: View) : ViewHolder(itemView)

    inner class StationViewHolder(view: View) : ViewHolder(view) {
        init {
            view.setOnClickListener {
                onItemClicked(stations[adapterPosition])
            }
            view.setOnLongClickListener {
                onItemLongClicked(stations[adapterPosition])
                true
            }
        }
    }

    var stations: List<Station> = mutableListOf()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onBindViewHolder(holder: ViewHolder?, position: Int) {
        try {
            if (holder is StationViewHolder) {
                val vh = holder
                stations.get(position).let { station ->
                    holder.itemView.text_name.text = station.name
                    holder.itemView.text_url.text = station.url
                    holder.itemView.text_bitrate.text = "${station.bitrate.toString()} kbps"
                    holder.itemView.text_format.text = station.format
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun getItemCount(): Int {
        return if (stations.isEmpty()) 1 else stations.size + 1;
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == stations.size) FOOTER_VIEW else super.getItemViewType(position)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder? {
        val inflater = LayoutInflater.from(parent.context)
        val vh: ViewHolder
        if (viewType == FOOTER_VIEW) {
            val view = inflater.inflate(R.layout.station_list_footer, parent, false)
            vh = FooterViewHolder(view)
        } else {
            val view = inflater.inflate(R.layout.station_list_item_layout, parent, false)
            vh = StationViewHolder(view)
        }
        return vh
    }
}