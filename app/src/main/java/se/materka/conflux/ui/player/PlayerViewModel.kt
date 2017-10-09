package se.materka.conflux.ui.player

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.MutableLiveData
import android.graphics.Bitmap
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import org.jetbrains.anko.coroutines.experimental.bg
import se.materka.conflux.AppDatabase
import se.materka.conflux.domain.Station
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

class PlayerViewModel(application: Application) : AndroidViewModel(application) {

    val currentStation = MutableLiveData<Station>()

    val metadata = MutableLiveData<MediaMetadataCompat>()
    val artistArt = MutableLiveData<Bitmap>()
    val isPlaying = MutableLiveData<Boolean>()

    val mediaControllerCallback: MediaControllerCompat.Callback = object : MediaControllerCompat.Callback() {
        override fun onMetadataChanged(data: MediaMetadataCompat?) {
            Timber.i("New metadata")
            this@PlayerViewModel.metadata.value = data
            this@PlayerViewModel.artistArt.value = data?.getBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART)
            currentStation.value?.let { station ->
                if (station.isPersisted) {
                    station.bitrate = data?.getLong(ShoutcastMetadata.METADATA_KEY_BITRATE)
                    station.format = data?.getString(ShoutcastMetadata.METADATA_KEY_FORMAT)
                    bg {
                        AppDatabase.Companion.instance(application).stationDao().update(station)
                    }
                }
            }
        }

        override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
            this@PlayerViewModel.isPlaying.value = when (state?.state) {
                PlaybackStateCompat.STATE_PLAYING, PlaybackStateCompat.STATE_BUFFERING -> {
                    true
                }
                else -> {
                    false
                }
            }
        }
    }

    fun play(station: Station?) {
        currentStation.postValue(station)
    }
}