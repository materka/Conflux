package se.materka.conflux.parsers

import android.net.Uri
import se.materka.conflux.parsers.models.*
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

//https://developer.apple.com/library/content/technotes/tn2288/_index.html#//apple_ref/doc/uid/DTS40012238-CH1-ALTERNATE_MEDIA

object M3UParser {
    const val SIGNATURE = "#EXTM3U"
    const val EXT_PREFIX = "#EXT"

    @Throws(IOException::class)
    fun parse(uri: Uri): M3UPlaylist? {
        var stream: InputStream? = null
        if (listOf("http", "https").any { scheme -> scheme == uri.scheme }) {
            try {
                val url = URL(uri.toString())
                val con = (url.openConnection() as HttpURLConnection).apply {
                    requestMethod = "GET"
                }
                if (con.responseCode == 200) {
                    stream = con.inputStream
                    return parse(stream)
                }
            } finally {
                stream?.close()
            }
        } else {
            try {
                stream = FileInputStream(File(uri.toString()))
                return parse(stream)
            } finally {
                stream?.close()
            }
        }
        return null
    }

    fun parse(inputStream: InputStream): M3UPlaylist {
        val lines = inputStream.bufferedReader().readLines()
        if (lines.size <= 1) {
            throw IllegalArgumentException("input stream is empty")
        }

        val playlist = M3UPlaylist()

        var currentKey: Key? = null

        //if (!M3U.SIGNATURE.equals(lines[0]))
        //    throw new IllegalArgumentException("Invalid M3U8 file");

        lines.forEachIndexed { index, line ->
            if (!isTagLine(line)) return@forEachIndexed

            val attributes = getAttributes(line)

            when (getTag(line)) {
                Tag.EXT_X_TARGETDURATION -> {
                    val duration = getAttributeValueAsDouble(attributes, 0, -1.0)
                    playlist.targetDuration = duration
                }

                Tag.EXT_X_MEDIA_SEQUENCE -> {
                    val sequence = getAttributeValueAsInt(attributes, 0, -1)
                    playlist.mediaSequence = sequence
                }

                Tag.EXT_X_KEY -> {
                    currentKey = Key().apply {
                        method = getAttributeValue(attributes, "METHOD")
                        iv = getAttributeValue(attributes, "IV")
                        uri = Uri.parse(getAttributeValue(attributes, "URI"))
                    }
                }

                Tag.EXT_X_ALLOW_CACHE -> {
                    playlist.isAllowCache = attributes.getOrNull(0)?.equals("YES", true) ?: false
                }

                Tag.EXT_X_PLAYLIST_TYPE -> {
                    playlist.playListType = attributes.getOrNull(0)
                }

                Tag.EXT_X_MEDIA -> {
                    playlist.media.add(
                            Media.Builder(getAttributeValue(attributes, "TYPE"))
                                    .withUri(getAttributeValue(attributes, "URI"))
                                    .withGroupId(getAttributeValue(attributes, "GROUP-ID"))
                                    .build())
                }

                Tag.EXT_X_STREAM_INF -> {
                    val bandWidth = getAttributeValue(attributes, "BANDWIDTH")
                    playlist.streams.add(
                            Stream.Builder(bandWidth?.toInt() ?: -1)
                                    .withUri(Uri.parse(lines[index + 1]))
                                    .withProgramId(getAttributeValue(attributes, "PROGRAM-ID"))
                                    .withResolution(getAttributeValue(attributes, "RESOLUTION"))
                                    .withCodecs(getAttributeValue(attributes, "CODECS")?.split(",")?.toMutableList())
                                    .withType(getAttributeValue(attributes, "TYPE"))
                                    .build())
                }

                Tag.EXT_X_VERSION -> {
                    playlist.version = attributes.getOrNull(0)
                }

                Tag.EXTINF -> {
                    val segment = Segment().apply {
                        duration = getAttributeValueAsDouble(attributes, 0, -1.0)
                        title = getAttributeValueAsString(attributes, 1, "")
                        uri = Uri.parse(lines[index + 1])
                        key = currentKey
                    }

                    playlist.segments.add(segment)
                }
                else -> {
                }
            }
        }

        return playlist
    }

    /**
     * If the line contains a tag return the name without the '#'
     * @param line
     * @return the tag name without the '#'
     */
    private fun getTag(line: String): Tag {
        val index = if (!line.isEmpty() && line.length > 1) line.indexOf(':') else -1
        val tagName = if (index != -1) line.substring(1, index) else ""
        return Tag.fromTagName(tagName)
    }

    private fun getAttributes(line: String): List<String> {
        val attributes: MutableList<String> = mutableListOf()
        line.takeIf { !it.isEmpty() }?.also { ln ->
            ln.indexOf(':').takeIf { it != -1 }?.also { index ->
                line.substring(index + 1).split(",").forEach { pair ->
                    attributes.add(pair.trim())
                }
            }
        }
        return attributes
    }

    private fun isTagLine(line: String): Boolean {
        return !line.isEmpty() && line.length > 3 && line.substring(0, 4) == EXT_PREFIX && line != SIGNATURE
    }

    private fun getAttributeValueAsInt(attributes: List<String>,
                                       position: Int,
                                       defaultValue: Int): Int {
        try {
            return attributes.getOrNull(position)?.toInt() ?: defaultValue
        } catch (err: Exception) {
            throw IllegalArgumentException("while coercing int value for attribute: " + attributes.getOrNull(position), err)
        }

    }

    private fun getAttributeValueAsDouble(attributes: List<String>,
                                          position: Int,
                                          defaultValue: Double): Double {
        try {
            return attributes.getOrNull(position)?.toDouble() ?: defaultValue
        } catch (err: Exception) {
            throw IllegalArgumentException("while coercing double value for attribute: " + attributes.getOrNull(position), err)
        }

    }

    private fun getAttributeValueAsString(attributes: List<String>,
                                          position: Int,
                                          defaultValue: String): String? {
        try {
            return attributes.getOrNull(position) ?: defaultValue
        } catch (err: Exception) {
            throw IllegalArgumentException("while coercing string value for attribute: " + attributes.getOrNull(position), err)
        }

    }

    private fun getAttributeValue(attributes: List<String>?, name: String): String? {
        attributes?.forEach { attr ->
            attr.indexOf('=').takeIf { it != -1 }?.also { index ->
                val key = attr.substring(0, index).trim()
                val value = attr.substring(index + 1).trim()
                if (key.equals(name, true)) {
                    return value.trim('\\')
                }
            }
        }
        return ""
    }

    enum class Tag(private val tagName: String) {
        EXT_X_BYTERANGE("EXT-X-BYTERANGE"),
        EXT_X_TARGETDURATION("EXT-X-TARGETDURATION"),
        EXT_X_MEDIA_SEQUENCE("EXT-X-MEDIA-SEQUENCE"),
        EXT_X_KEY("EXT-X-KEY"),
        EXT_X_PROGRAM_DATE_TIME("EXT-X-PROGRAM-DATE-TIME"),
        EXT_X_ALLOW_CACHE("EXT-X-ALLOW-CACHE"),
        EXT_X_PLAYLIST_TYPE("EXT-X-PLAYLIST-TYPE"),
        EXT_X_ENDLIST("EXT-X-ENDLIST"),
        EXT_X_MEDIA("EXT-X-MEDIA"),
        EXT_X_STREAM_INF("EXT-X-STREAM-INF"),
        EXT_X_DISCONTINUITY("EXT-X-DISCONTINUITY"),
        EXT_X_I_FRAMES_ONLY("EXT-X-I-FRAMES-ONLY"),
        EXT_X_I_FRAME_STREAM_INF("EXT-X-I-FRAME-STREAM-INF"),
        EXT_X_VERSION("EXT-X-VERSION"),
        EXTINF("EXTINF"),
        UNKNOWN("");

        override fun toString(): String {
            return this.tagName
        }

        companion object {
            fun fromTagName(tagName: String?): Tag {
                return Tag.valueOf(tagName ?: "")
            }
        }
    }
}
