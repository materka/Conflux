package se.materka.conflux.ui.viewmodel

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.MutableLiveData
import android.graphics.Bitmap
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.PlaybackStateCompat
import se.materka.conflux.db.entity.Station
import se.materka.conflux.db.repository.StationRepository
import se.materka.exoplayershoutcastdatasource.ShoutcastMetadata
import timber.log.Timber

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

class MetadataViewModel(application: Application, private val stationRepository: StationRepository) : AndroidViewModel(application) {

    val metadata = MutableLiveData<MediaMetadataCompat>()
    val isPlaying = MutableLiveData<Boolean>()
    private val artistArt = MutableLiveData<Bitmap>()

    fun onMetadataChanged(data: MediaMetadataCompat?, currentStation: Station?) {
        Timber.i("New metadata")
        metadata.value = data
        val bitrate = data?.getLong(ShoutcastMetadata.METADATA_KEY_BITRATE)
        val format = data?.getString(ShoutcastMetadata.METADATA_KEY_FORMAT)

        currentStation?.let { station ->
            if (station.isPersisted && (station.bitrate != bitrate || station.format != format)) {
                station.bitrate = bitrate
                station.format = format
                stationRepository.update(station)
            }
        }

    }

    fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
        isPlaying.value = when (state?.state) {
            PlaybackStateCompat.STATE_PLAYING -> true
            else -> false
        }
    }
}
