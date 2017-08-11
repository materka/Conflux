package se.materka.conflux.utils

import android.content.Context
import android.graphics.Color
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.TextView
import com.mikepenz.iconics.IconicsDrawable
import com.mikepenz.iconics.typeface.IIcon

fun icon(context: Context, icon: IIcon, color: Int = Color.WHITE, sizeDp: Int = 24, paddingDp: Int = 0): IconicsDrawable {
    return IconicsDrawable(context).icon(icon).color(color).sizeDp(sizeDp).paddingDp(paddingDp)
}

fun <T> nvl(value: T?, default: T): T {
    return value ?: default
}

fun TextView.hideIfEmpty(hide: Boolean) = addTextChangedListener(object : TextWatcher {
    override fun afterTextChanged(s: Editable?) {
        this@hideIfEmpty.visibility =  if ((s == null || s.isNullOrEmpty()) && hide) View.GONE else View.VISIBLE
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
})

