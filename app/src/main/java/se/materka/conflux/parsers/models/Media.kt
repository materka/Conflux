package se.materka.conflux.parsers.models

import android.net.Uri

class Media private constructor(builder: Builder) {

    val type: Type?
    val name: String?
    val groupId: String?
    val uri: Uri?

    init {
        this.type = builder.type
        this.name = builder.name
        this.groupId = builder.groupId
        this.uri = builder.uri
    }

    enum class Type(internal val value: String) {
        AUDIO("AUDIO"),
        SUBTITLES("SUBTITLES"),
        VIDEO("VIDEO");

        companion object {
            fun fromValue(value: String?): Type? {
                return value?.let { Type.valueOf(it) }
            }
        }
    }

    override fun toString(): String {
        return "Media{" +
                "type=" + type +
                ", name='" + name + '\''.toString() +
                ", groupId='" + groupId + '\''.toString() +
                ", uri=" + uri +
                '}'.toString()
    }

    class Builder(type: String?) {
        val type: Type? = Type.fromValue(type)

        var name: String? = null
            private set
        var groupId: String? = null
            private set
        var uri: Uri? = null
            private set

        fun withName(value: String?): Builder {
            this.name = value
            return this
        }

        fun withGroupId(value: String?): Builder {
            this.groupId = value
            return this
        }

        fun withUri(value: String?): Builder {
            value?.takeIf { value.isNotEmpty() }?.run{ this@Builder.uri = Uri.parse(value) }
            return this
        }

        fun withUri(value: Uri?): Builder {
            this.uri = value
            return this
        }

        fun build(): Media {
            return Media(this)
        }
    }
}
