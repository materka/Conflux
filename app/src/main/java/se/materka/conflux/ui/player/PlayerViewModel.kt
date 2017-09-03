package se.materka.conflux.ui.player

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.MutableLiveData
import se.materka.conflux.model.Station
import se.materka.exoplayershoutcastplugin.Metadata

/**
 * Created by Privat on 5/20/2017.
 */

class PlayerViewModel(application: Application) : AndroidViewModel(application) {

    val currentStation = MutableLiveData<Station>()

    val metadata = MutableLiveData<Metadata>()
        get() = field
    val isPlaying = MutableLiveData<Boolean>()
        get() = field

    fun play(station: Station?) {
        currentStation.postValue(station)
    }

    fun setMetadata(metadata: Metadata) {
        this.metadata.value = metadata
    }

    fun isPlaying(playing: Boolean) {
        this.isPlaying.value = playing
    }
}