package se.materka.conflux.ui.view

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import kotlinx.android.synthetic.main.fragment_edit.*
import org.koin.androidx.viewmodel.ext.android.getViewModel
import se.materka.conflux.R
import se.materka.conflux.databinding.FragmentEditBinding
import se.materka.conflux.db.entity.Station
import se.materka.conflux.ui.viewmodel.StationViewModel

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

class EditFragment : DialogFragment() {

    private lateinit var station: Station

    private val stationViewModel: StationViewModel? by lazy {
        activity?.getViewModel<StationViewModel>()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        station = arguments!!.getParcelable(InfoFragment.ARG_STATION)
        val binding: FragmentEditBinding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.fragment_edit, null, false)
        binding.station = station

        val alertDialog = AlertDialog.Builder(requireContext())
                .setTitle(R.string.title_edit)
                .setView(binding.root)
                .setPositiveButton(R.string.btn_save) { _, _ -> }
                .setNegativeButton(R.string.btn_cancel) { dialog, _ -> dialog?.dismiss() }
                .create()

        alertDialog.setOnShowListener { dialog: DialogInterface ->
            (dialog as AlertDialog).getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                if (isValid(station.url)) {
                    station.name = station.name ?: station.url
                    stationViewModel?.update(station)
                    dialog.dismiss()
                } else {
                    dialog.text_url.error = "Invalid URL, please provide a valid URL"
                    dialog.text_url.requestFocus()
                }
            }
        }
        return alertDialog
    }

    private fun isValid(url: String?): Boolean {
        return url != null && Patterns.WEB_URL.matcher(url).matches()
    }

    companion object {
        private val ARG_STATION = "station"

        fun newInstance(station: Station): EditFragment {
            val fragment = EditFragment()
            val args = Bundle()
            args.putParcelable(ARG_STATION, station)
            fragment.arguments = args
            return fragment
        }
    }
}
