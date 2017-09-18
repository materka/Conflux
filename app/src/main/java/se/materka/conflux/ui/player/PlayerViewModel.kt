package se.materka.conflux.ui.player

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.MutableLiveData
import android.net.Uri
import se.materka.conflux.database.Station
import se.materka.exoplayershoutcastdatasource.ShoutcastMetadata

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

    private val currentStation = MutableLiveData<Station>()

    val metadata = MutableLiveData<ShoutcastMetadata>()
    val cover = MutableLiveData<Uri>()
    val isPlaying = MutableLiveData<Boolean>()

    fun play(station: Station?) {
        currentStation.postValue(station)
    }

    fun setMetadata(metadata: ShoutcastMetadata) {
        this.metadata.value = metadata
    }

    fun setCover(uri: Uri) {
        this.cover.value = uri
    }

    fun isPlaying(playing: Boolean) {
        this.isPlaying.value = playing
    }
}