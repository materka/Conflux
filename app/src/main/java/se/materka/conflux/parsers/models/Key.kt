package se.materka.conflux.parsers.models

import android.net.Uri

class Key() {

    var method: String? = null
    var uri: Uri? = null
    var iv: String? = null

    init {
        this.method = null
        this.uri = null
        this.iv = null
    }

    constructor(src: Key) : this() {
        this.method = src.method
        this.uri = src.uri
        this.iv = src.iv
    }

    override fun toString(): String {
        return "Key{" +
                "method='" + method + '\''.toString() +
                ", uri=" + uri +
                ", iv='" + iv + '\''.toString() +
                '}'.toString()
    }
}
