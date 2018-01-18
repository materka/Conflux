package se.materka.conflux.ui

import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager

/**
 * Created by Mattias on 1/18/2018.
 */

fun View.hideKeyboard() {
    val manager = this.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    manager.hideSoftInputFromWindow(this.windowToken, 0)
}