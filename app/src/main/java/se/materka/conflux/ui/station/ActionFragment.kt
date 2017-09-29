package se.materka.conflux.ui.station

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.design.widget.BottomSheetDialogFragment
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.franmontiel.fullscreendialog.FullScreenDialogFragment
import kotlinx.android.synthetic.main.menu_action.*
import se.materka.conflux.R
import se.materka.conflux.ui.browse.BrowseViewModel

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

class ActionFragment : BottomSheetDialogFragment() {
    private val browseViewModel: BrowseViewModel by lazy {
        ViewModelProviders.of(activity).get(BrowseViewModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.menu_action, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        info.setOnClickListener {
            FullScreenDialogFragment.Builder(activity)
                    .setTitle("Information")
                    .setContent(InfoFragment::class.java, null)
                    .build()
                    .show(fragmentManager, "InfoFragment")
            dismiss()
        }

        edit.setOnClickListener {
            FullScreenDialogFragment.Builder(activity)
                    .setTitle("Edit")
                    .setContent(EditFragment::class.java, null)
                    .setConfirmButton("SAVE")
                    .build()
                    .show(fragmentManager, "EditFragment")
            dismiss()
        }

        delete.setOnClickListener {
            AlertDialog.Builder(context, R.style.AppTheme_AlertDialog)
                    .setTitle("Remove")
                    .setMessage("Are you sure you want to remove this station?")
                    .setPositiveButton("REMOVE") { _, _ ->
                        browseViewModel.deleteStation()
                        this@ActionFragment.dismiss()
                    }
                    .setNegativeButton("CANCEL") { dialog, _ -> dialog.dismiss() }
                    .show()
        }
    }
}