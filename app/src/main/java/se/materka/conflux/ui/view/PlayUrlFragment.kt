package se.materka.conflux.ui.view

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import kotlinx.android.synthetic.main.fragment_play.view.*
import org.jetbrains.anko.alert
import org.jetbrains.anko.cancelButton
import se.materka.conflux.R

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

class PlayUrlFragment : DialogFragment() {

    interface PlayUrlDialogListener {
        fun onDialogFinished(resultCode: Int?, result: Bundle)
    }

    private var listener: PlayUrlDialogListener? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view: View = LayoutInflater.from(context).inflate(R.layout.fragment_play, null, false)
        view.cb_save.setOnCheckedChangeListener { _, isChecked ->
            val text = if (isChecked) {
                R.string.btn_save_and_play
            } else {
                R.string.btn_play
            }
            (dialog as AlertDialog).getButton(AlertDialog.BUTTON_POSITIVE).setText(text)
        }

        return AlertDialog.Builder(requireContext()).apply {
            setTitle(R.string.title_play)
            setView(view)
            setPositiveButton(R.string.btn_play) { _, _ ->
                if (isValid(view.input_url.text.toString())) {
                    val result = Bundle().apply {
                        putString(RESULT_URI, view.input_url.text.toString())
                        putBoolean(RESULT_SAVE, view.cb_save.isChecked)
                        putString(RESULT_NAME, view.input_name.text.toString())
                    }
                    listener?.onDialogFinished(arguments?.getInt(ARG_PARAM_RESULT_CODE, -1), result)
                    dialog.dismiss()
                } else {
                    view.input_url.error = resources.getText(R.string.error_invalid_url)
                    view.input_url.requestFocus()
                }
            }
            setNegativeButton(android.R.string.cancel) { _, _ ->
                dismiss()
            }
        }.create()
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        if (context is PlayUrlFragment.PlayUrlDialogListener) {
            listener = context
        } else {
            throw RuntimeException(context!!.toString() + " must implement Listener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    private fun isValid(url: String?): Boolean {
        return url != null && Patterns.WEB_URL.matcher(url).matches()
    }

    companion object {
        private const val ARG_PARAM_RESULT_CODE = "RESULT_CODE"

        const val RESULT_URI: String = "RESULT_URI"
        const val RESULT_SAVE: String = "RESULT_SAVE"
        const val RESULT_NAME: String = "RESULT_NAME"

        @JvmStatic
        fun newInstance(resultCode: Int) =
                PlayUrlFragment().apply {
                    arguments = Bundle().apply {
                        putInt(ARG_PARAM_RESULT_CODE, resultCode)
                    }
                }
    }
}
