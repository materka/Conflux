package se.materka.conflux.ui.view

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import se.materka.conflux.R
import se.materka.conflux.databinding.FragmentInfoBinding
import se.materka.conflux.db.entity.Station


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
class InfoFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val binding: FragmentInfoBinding = DataBindingUtil.inflate(LayoutInflater.from(activity), R.layout.fragment_info, null, false)
        binding.station = arguments?.getParcelable(ARG_STATION)
        return AlertDialog.Builder(requireContext())
                .setTitle("StationModel Information")
                .setView(binding.root)
                .setPositiveButton("CLOSE") { dialog, _ -> dialog?.dismiss() }
                .create()
    }

    companion object {
        val ARG_STATION = "station"

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment BlankFragment.
         */
        // TODO: Rename and change types and number of parameters
        fun newInstance(station: Station): InfoFragment {
            val fragment = InfoFragment()
            val args = Bundle()
            args.putParcelable(ARG_STATION, station)
            fragment.arguments = args
            return fragment
        }
    }
}
