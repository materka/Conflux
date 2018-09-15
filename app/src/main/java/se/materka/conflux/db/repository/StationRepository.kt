package se.materka.conflux.db.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.experimental.launch
import se.materka.conflux.db.dao.StationDao
import se.materka.conflux.db.entity.Station

/**
 * Created by Mattias on 1/18/2018.
 */

interface StationRepository {
    fun create(station: Station): LiveData<Long>
    fun getStations(): LiveData<List<Station>>
    fun getStation(stationId: Long): LiveData<Station>
    fun update(station: Station): LiveData<Boolean>
    fun delete(station: Station): LiveData<Boolean>
    fun exists(station: Station): LiveData<Boolean>

}

class StationRepositoryImpl(private val dao: StationDao) : StationRepository {

    override fun create(station: Station): MutableLiveData<Long> {
        val id: MutableLiveData<Long> = MutableLiveData()
        launch {
            id.postValue(dao.insert(station))
        }
        return id
    }

    override fun getStations(): LiveData<List<Station>> {
        return dao.selectAll()
    }

    override fun getStation(stationId: Long): LiveData<Station> {
        return dao.select(stationId)
    }

    override fun update(station: Station): LiveData<Boolean> {
        val updated: MutableLiveData<Boolean> = MutableLiveData()
        launch {
            updated.postValue(dao.update(station) > 0)
        }
        return updated
    }

    override fun delete(station: Station): LiveData<Boolean> {
        val deleted = MutableLiveData<Boolean>()
        launch {
            deleted.postValue(dao.delete(station) == 1)
        }
        return deleted
    }

    override fun exists(station: Station): LiveData<Boolean> {
        val exists = MutableLiveData<Boolean>()
        launch {
            exists.postValue(dao.exists(station.id) == 1)
        }
        return exists
    }
}