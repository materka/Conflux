package se.materka.conflux.parsers.models

/**
 * PLSPlaylist file.
 *
 * Contains a list of tracks and a version.
 */
class PLSPlaylist internal constructor(
        /**
         * Get track list.
         *
         * @return [Track] list
         */
        val tracks: List<Track>,
        /**
         * Get playlist file version.
         *
         * @return Version number
         */
        val version: Int) {

    override fun toString(): String {
        return "PLSPlaylist{" +
                "tracks=" + tracks +
                ", version=" + version +
                '}'.toString()
    }

    /**
     * PLSPlaylist file track.
     */
    class Track {
        var file: String? = null
            internal set
        var title: String? = null
            internal set
        var length: Long = 0
            internal set

        override fun toString(): String {
            return "Track{" +
                    "file='" + file + '\''.toString() +
                    ", title='" + title + '\''.toString() +
                    ", length=" + length +
                    '}'.toString()
        }
    }
}