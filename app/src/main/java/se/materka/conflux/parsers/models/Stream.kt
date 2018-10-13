package se.materka.conflux.parsers.models

import android.net.Uri

/**
 * https://pypkg.com/pypi/pytoutv/src/toutv/m3u8.py
 */
class Stream private constructor(builder: Builder) {

    val bandWidth: Int
    val codecs: MutableList<String>?
    val resolution: String?
    val programId: String?
    val type: String?
    val uri: Uri?

    init {
        this.bandWidth = builder.bandWidth
        this.codecs = builder.codecs
        this.programId = builder.programId
        this.resolution = builder.resolution
        this.type = builder.type
        this.uri = builder.uri
    }

    override fun toString(): String {
        return """Stream{
                    bandWidth=$bandWidth,
                    codecs=${codecs?.joinToString()},
                    resolution=$resolution,
                    programId=$programId,
                    type=$type,
                    uri=$uri
               }"""
    }

    class Builder(val bandWidth: Int) {
        var codecs: MutableList<String>? = null
            private set
        var resolution: String? = null
            private set
        var programId: String? = null
            private set
        var type: String? = null
            private set
        var uri: Uri? = null
            private set

        fun withType(value: String?): Builder {
            this.type = value
            return this
        }

        fun withCodecs(values: MutableList<String>?): Builder {
            codecs = values
            return this
        }

        fun withResolution(value: String?): Builder {
            this.resolution = value
            return this
        }

        fun withProgramId(value: String?): Builder {
            this.programId = value
            return this
        }

        fun withUri(value: Uri?): Builder {
            this.uri = value
            return this
        }

        fun build(): Stream {
            return Stream(this)
        }
    }
}
