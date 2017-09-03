package se.materka.conflux.ui.station

/**
 * Created by Privat on 6/11/2017.
 */
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


class StationActionDialogFragment : BottomSheetDialogFragment() {
    private val browseViewModel: BrowseViewModel by lazy {
        ViewModelProviders.of(activity).get(BrowseViewModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.station_menu, container, false)
        return v
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
                    .setPositiveButton("DO IT") { dialog, which ->
                        browseViewModel.deleteStation()
                        this@StationActionDialogFragment.dismiss()
                    }
                    .setNegativeButton("CANCEL") { dialog, which -> dialog.dismiss() }
                    .show()
        }
    }
}