package se.materka.conflux.ui.viewmodel

import android.app.Application
import android.media.session.PlaybackState
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import org.koin.core.parameter.parametersOf
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject
import se.materka.conflux.RadioSession
import se.materka.exoplayershoutcastdatasource.ShoutcastMetadata

/**
 * Copyright Mattias Karlsson

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

class MetadataViewModel(application: Application) : AndroidViewModel(application), KoinComponent {

    private val radioSession: RadioSession by inject()

    val artist: LiveData<String>
    val title: LiveData<String>
    val station: LiveData<String>

    val isPlaying: LiveData<Boolean> = Transformations.map(radioSession.playbackState) { playbackState ->
        playbackState.state == PlaybackState.STATE_PLAYING
    }

    init {
        artist = Transformations.map(radioSession.nowPlaying) { metadata ->
            metadata.getString(ShoutcastMetadata.METADATA_KEY_ARTIST)
        }
        title = Transformations.map(radioSession.nowPlaying) { metadata ->
            metadata.getString(ShoutcastMetadata.METADATA_KEY_TITLE)
        }
        station = Transformations.map(radioSession.nowPlaying) { metadata ->
            metadata.getString(ShoutcastMetadata.METADATA_KEY_STATION)
        }
    }
}
