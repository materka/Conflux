package se.materka.conflux.ui.player

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.MutableLiveData
import se.materka.conflux.database.Station
import se.materka.exoplayershoutcastdatasource.ShoutcastMetadata

/**
 * Created by Privat on 5/20/2017.
 */

class PlayerViewModel(application: Application) : AndroidViewModel(application) {

    val currentStation = MutableLiveData<Station>()

    val metadata = MutableLiveData<ShoutcastMetadata>()
        get() = field
    val isPlaying = MutableLiveData<Boolean>()
        get() = field

    fun play(station: Station?) {
        currentStation.postValue(station)
    }

    fun setMetadata(metadata: ShoutcastMetadata) {
        this.metadata.value = metadata
    }

    fun isPlaying(playing: Boolean) {
        this.isPlaying.value = playing
    }
}