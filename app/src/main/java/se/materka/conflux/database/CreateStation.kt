package se.materka.conflux.database

import se.materka.conflux.database.Station

class CreateStation(val dao: StationDao) {

    fun call(): List<Long?> {

        val stations: List<Station> = listOf(
                Station().apply {
                    name = "BANDIT"
                    url = "http://stream-ice.mtgradio.com:8080/stat_bandit"
                    bitrate = 96
                    format = "MP3"
                },
                Station().apply {
                    name = "P3"
                    url = "http://http-live.sr.se/p3-mp3-192"
                    bitrate = 128
                    format = "MP3"
                },
                Station().apply {
                    name = "EAST FM"
                    url = "http://www.listenlive.eu/eastfm.m3u"
                    bitrate = 128
                    format = "MP3"
                },
                Station().apply {
                    name = "P2"
                    url = "http://sverigesradio.se/topsy/direkt/2562-hi-mp3.pls"
                    bitrate = 128
                    format = "MP3"
                }
        )
        stations.forEach { station ->
            dao.insert(station)
            dao.insert(station)
            dao.insert(station)
        }
        return stations.map { it.id }
    }
}