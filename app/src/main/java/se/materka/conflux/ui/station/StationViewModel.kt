package se.materka.conflux.ui.station

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import org.jetbrains.anko.doAsync
import se.materka.conflux.database.AppDatabase
import se.materka.conflux.database.StationDao
import se.materka.conflux.model.Station


/**
 * Created by Privat on 5/20/2017.
 */

class StationViewModel(application: Application) : AndroidViewModel(application) {

    private val dao: StationDao by lazy {
        AppDatabase.Companion.instance(application).stationDao()
    }

    val selected = MutableLiveData<Station>()

    fun select(station: Station?) {
        selected.value = station
    }

    fun getStations(): LiveData<List<Station>>? {
        return dao.all
    }

    fun saveStation(): LiveData<Long> {
        val result = MutableLiveData<Long>()
        selected.value?.let { s ->
            doAsync {
                val id = dao.insert(s)
                if (id > -1) {
                    selected.postValue(dao.get(id))
                }
                result.postValue(id)
            }
        }
        return result
    }

    fun saveStation(station: Station): LiveData<Long> {
        selected.value = station
        return saveStation()
    }

    fun deleteStation(): LiveData<Boolean> {
        val result = MutableLiveData<Boolean>()
        selected.value?.let { station ->
            doAsync {
                if (dao.delete(station) == 1) {
                    station.id = null
                    selected.postValue(station)
                    result.postValue(true)
                } else {
                    result.postValue(false)
                }
            }
        }
        return result
    }

    fun updateStation(): LiveData<Boolean> {
        val result = MutableLiveData<Boolean>()
        selected.value?.let { station ->
            doAsync {
                result.postValue(dao.update(station) == 1)
            }
        }
        return result
    }
}