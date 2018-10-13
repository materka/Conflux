package se.materka.conflux.parsers.models

class M3UPlaylist {

    val streams: MutableList<Stream> = mutableListOf()
    val segments: MutableList<Segment> = mutableListOf()
    val media: MutableList<Media> = mutableListOf()

    var targetDuration = 0.0
    var isAllowCache = false
    var playListType: String? = null
    var version: String? = null
    var mediaSequence = 0

    override fun toString(): String {
        return """
            PlayList{
                streams=$streams,
                segments=$segments,
                targetDuration=$targetDuration,
                allowCache=$isAllowCache,
                playListType=$playListType,
                version=$version,
                mediaSequence=$mediaSequence
            }"""
    }
}
