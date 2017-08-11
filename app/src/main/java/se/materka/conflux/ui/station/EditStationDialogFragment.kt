package se.materka.conflux.ui.station

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import kotlinx.android.synthetic.main.fragment_edit_station.*
import se.materka.conflux.R
import se.materka.conflux.custom.FullScreenDialogFragment

/**
 * Created by Privat on 5/22/2017.
 */

class EditStationDialogFragment : FullScreenDialogFragment() {

    private val stationViewModel: StationViewModel by lazy {
        ViewModelProviders.of(activity).get(StationViewModel::class.java)
    }

    private val station by lazy {
        stationViewModel.selected.value
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
                stationViewModel.updateStation()
                dismiss()
                true
            }
        }
        text_name.setText(station?.name, TextView.BufferType.EDITABLE)
        text_url.setText(station?.url, TextView.BufferType.EDITABLE)
    }
}
