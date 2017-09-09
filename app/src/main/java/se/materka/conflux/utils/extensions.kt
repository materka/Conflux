package se.materka.conflux.utils

import android.content.Context
import android.graphics.Color
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.TextView
import com.mikepenz.iconics.IconicsDrawable
import com.mikepenz.iconics.typeface.IIcon

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

fun <T> nvl(value: T?, default: T): T {
    return value ?: default
}

fun icon(context: Context, icon: IIcon, color: Int = Color.WHITE, sizeDp: Int = 24, paddingDp: Int = 0): IconicsDrawable {
    return IconicsDrawable(context).icon(icon).color(color).sizeDp(sizeDp).paddingDp(paddingDp)
}

fun TextView.hideIfEmpty(hide: Boolean) = addTextChangedListener(object : TextWatcher {
    override fun afterTextChanged(s: Editable?) {
        this@hideIfEmpty.visibility =  if ((s == null || s.isNullOrEmpty()) && hide) View.GONE else View.VISIBLE
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
})