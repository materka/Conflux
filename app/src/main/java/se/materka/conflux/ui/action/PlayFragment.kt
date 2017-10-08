package se.materka.conflux.ui.action

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.franmontiel.fullscreendialog.FullScreenDialogContent
import com.franmontiel.fullscreendialog.FullScreenDialogController
import kotlinx.android.synthetic.main.fragment_play.*
import se.materka.conflux.R
import se.materka.conflux.ui.Common

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

class PlayFragment : Fragment(), FullScreenDialogContent {
    private var dialogController: FullScreenDialogController? = null

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater?.inflate(R.layout.fragment_play, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        toggle_save_container.setOnCheckedChangeListener { _, _ ->
            save_container.visibility =  if (save_container.visibility != View.GONE) View.GONE else View.VISIBLE
        }
    }

    override fun onConfirmClick(dialogController: FullScreenDialogController?): Boolean {
        onConfirm()
        return true
    }

    override fun onDialogCreated(dialogController: FullScreenDialogController?) {
        this.dialogController = dialogController
    }

    override fun onDiscardClick(dialogController: FullScreenDialogController?): Boolean {
        view?.let {
            Common.hideKeyboard(context, it)
        }
        return false
    }

    private fun onConfirm() {
        view?.let {
            Common.hideKeyboard(context, it)
        }
        dialogController?.confirm(Bundle().apply {
            putString(EXTRA_STATION_URL, this@PlayFragment.url.text.toString())
            putString(EXTRA_STATION_NAME, this@PlayFragment.url.text.toString())
            putBoolean(EXTRA_SAVE_STATION, toggle_save_container.isChecked)
        })
    }

    companion object {
        val EXTRA_STATION_URL: String = "se.materka.conflux.STATION_URL"
        val EXTRA_STATION_NAME: String = "se.materka.conflux.STATION_NAME"
        val EXTRA_SAVE_STATION: String = "se.materka.conflux.SAVE_STATION"
    }
}