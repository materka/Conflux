package se.materka.conflux.parsers

import se.materka.conflux.parsers.models.PLSPlaylist
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.lang.IndexOutOfBoundsException
import java.util.*

/**
 * M3UPlaylist file (*.pls) parser.
 *
 * See https://en.wikipedia.org/wiki/PLS_(file_format)
 */
object PLSParser {
    private val FOOTER_NUMBER_OF_ENTRIES_PATTERN = "\\s*NumberOfEntries=(.*)".toRegex()
    private val FOOTER_VERSION_PATTERN = "\\s*Version=(.*)".toRegex()

    private val FILE_PATTERN = "\\s*File(\\d+)=(.*)".toRegex()
    private val TITLE_PATTERN = "\\s*Title(\\d+)=(.*)".toRegex()
    private val LENGTH_PATTERN = "\\s*Length(\\d+)=(.*)".toRegex()

    /**
     * Parses a playlist file (*.pls) provided as [InputStream].
     *
     * @param stream PLS file [InputStream]
     * @return [M3UPlaylist] instance
     * @throws IllegalArgumentException If stream is null
     */
    fun parse(stream: InputStream): PLSPlaylist {
        val tracks = ArrayList<PLSPlaylist.Track>()
        var version = -1
        var entries = -1

        BufferedReader(InputStreamReader(stream))
        stream.bufferedReader().use { reader ->
            reader.forEachLine { line ->
                when {
                    line.matches(FILE_PATTERN) -> add(tracks, FILE_PATTERN.matchEntire(line), ::addFile)
                    line.matches(TITLE_PATTERN) -> add(tracks, TITLE_PATTERN.matchEntire(line), ::addTitle)
                    line.matches(LENGTH_PATTERN) -> add(tracks, LENGTH_PATTERN.matchEntire(line), ::addLength)
                    line.matches(FOOTER_NUMBER_OF_ENTRIES_PATTERN) -> FOOTER_NUMBER_OF_ENTRIES_PATTERN.matchEntire(line)?.let { match ->
                        entries = match.groups[1]?.value?.toInt() ?: -1
                    }
                    line.matches(FOOTER_VERSION_PATTERN) -> FOOTER_VERSION_PATTERN.matchEntire(line)?.let { match ->
                        version = match.groups[1]?.value?.toInt() ?: -1
                    }
                }
            }
        }
        if (entries != tracks.size) {
            // TODO: Something is fishy!
        }
        return PLSPlaylist(tracks, version)
    }

    private fun add(tracks: MutableList<PLSPlaylist.Track>, matchResult: MatchResult?, adder: (MutableList<PLSPlaylist.Track>, Int, String) -> Unit) {
        val index: Int? = (matchResult?.groups?.get(1)?.value?.toInt() ?: -1) - 1
        val value: String? = matchResult?.groups?.get(2)?.value
        if (index != null && value != null) {
            adder.invoke(tracks, index, value)
        }
    }

    private fun addFile(tracks: MutableList<PLSPlaylist.Track>, index: Int, value: String) {
        try {
            tracks[index].file = value
        } catch (e: IndexOutOfBoundsException) {
            tracks.add(index, PLSPlaylist.Track().apply { file = value })
        }
    }

    private fun addTitle(tracks: MutableList<PLSPlaylist.Track>, index: Int, value: String) {
        val track = tracks[index]
        track.title = value
    }

    private fun addLength(tracks: MutableList<PLSPlaylist.Track>, index: Int, value: String) {
        val track: PLSPlaylist.Track? = tracks[index]
        track?.length = value.toLong()
    }
}