package se.materka.conflux.parsers.models

import android.net.Uri

class Segment {

    var key: Key? = null
    var duration: Double = -1.0
    var title: String? = null
    var uri: Uri? = null

    override fun toString(): String {
        return "Segment{key=$key, duration=$duration, title=$title"
    }
}
