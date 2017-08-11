package se.materka.conflux.ui.player

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.MutableLiveData
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import se.materka.conflux.ConfluxApplication
import se.materka.conflux.MetadataBindable
import se.materka.conflux.RadioManager
import se.materka.conflux.model.Station

/**
 * Created by Privat on 5/20/2017.
 */

class PlayerViewModel(application: Application) : AndroidViewModel(application) {

    private val radioManager: RadioManager by lazy {
        (application as ConfluxApplication).radioManager
    }

    val currentStation = MutableLiveData<Station>()

    val isPlaying = radioManager.playing

    fun play(station: Station?) {
        radioManager.play(station)
        currentStation.postValue(station)
    }

    fun togglePlay() {
        if (isPlaying.value == true) {
            radioManager.stop()
        } else {
            play(currentStation.value)
        }
    }

    fun bindMetadata(metadata: MetadataBindable) {
        radioManager.metadata()
                .observeOn(Schedulers.newThread())
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe { m ->
                    metadata.setArtist(m.artist)
                    metadata.setSong(m.song)
                    metadata.setShow(m.show)
                }
    }
}