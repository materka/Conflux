package se.materka.conflux.utils

import android.text.Editable
import android.text.TextWatcher
import android.view.View


/**
 * Created by Privat on 6/10/2017.
 */
class GenericTextWatcher constructor(private val view: View) : TextWatcher {

    override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
    override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}

    override fun afterTextChanged(editable: Editable) {
        val text = editable.toString()
        view.visibility =  if (text.isNullOrEmpty()) View.GONE else View.VISIBLE
    }
}