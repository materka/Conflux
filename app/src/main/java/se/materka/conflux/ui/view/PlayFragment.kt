package se.materka.conflux.ui.view

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.util.Patterns
import android.view.LayoutInflater
import kotlinx.android.synthetic.main.fragment_play.*
import kotlinx.android.synthetic.main.fragment_play.view.*
import org.koin.android.architecture.ext.getViewModel
import se.materka.conflux.R
import se.materka.conflux.databinding.FragmentPlayBinding
import se.materka.conflux.db.model.Station
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

class PlayFragment : DialogFragment() {

    private val station: Station = Station()

    private val stationViewModel: StationViewModel? by lazy {
        activity?.getViewModel<StationViewModel>()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val binding: FragmentPlayBinding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.fragment_play, null, false)
        binding.station = station

        val alertDialog = AlertDialog.Builder(activity, R.style.AppTheme_InfoDialog)
                .setTitle(R.string.title_play)
                .setView(binding.root)
                .setPositiveButton(R.string.btn_save, { dialog, which -> })
                .setNegativeButton(R.string.btn_cancel, { dialog, _ -> dialog?.dismiss() })
                .create()

        alertDialog.setOnShowListener { dialog: DialogInterface ->
            (dialog as AlertDialog).getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                if (isValid(station.url)) {
                    station.name = station.name ?: station.url
                    stationViewModel?.save(station)
                    dialog.dismiss()
                } else {
                    dialog.input_url.error = resources.getString(R.string.error_invalid_url)
                    dialog.input_url.requestFocus()
                }
            }
        }

        binding.root.cb_save.setOnClickListener {
            val text = if (binding.root.cb_save.isChecked) {
                R.string.btn_save_and_play
            } else {
                R.string.btn_play
            }
            (dialog as AlertDialog).getButton(AlertDialog.BUTTON_POSITIVE).setText(text)
        }
        return alertDialog
    }

    private fun isValid(url: String?): Boolean {
        return url != null && Patterns.WEB_URL.matcher(url).matches()
    }
}
