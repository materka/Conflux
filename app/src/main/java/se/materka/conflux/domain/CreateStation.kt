package se.materka.conflux.domain

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

class CreateStation(val dao: StationDao) {

    fun call(): List<Long?> {

        val stations: List<Station> = listOf(
                Station().apply {
                    name = "BANDIT"
                    url = "http://stream-ice.mtgradio.com:8080/stat_bandit"
                },
                Station().apply {
                    name = "P3"
                    url = "http://http-live.sr.se/p3-mp3-192"
                },
                Station().apply {
                    name = "EAST FM"
                    url = "http://www.listenlive.eu/eastfm.m3u"
                },
                Station().apply {
                    name = "P2"
                    url = "http://sverigesradio.se/topsy/direkt/2562-hi-mp3.pls"
                }
        )
        stations.forEach { station ->
            dao.insert(station)
        }
        return stations.map { it.id }
    }
}