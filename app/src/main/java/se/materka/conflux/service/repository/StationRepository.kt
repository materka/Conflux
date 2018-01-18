package se.materka.conflux.service.repository

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import org.jetbrains.anko.coroutines.experimental.bg
import se.materka.conflux.service.datasource.StationDataSource
import se.materka.conflux.service.model.Station

/**
 * Created by Mattias on 1/18/2018.
 */

interface StationRepository {
    fun create(station: Station): LiveData<Long>
    fun read(): LiveData<List<Station>>
    fun read(stationId: Long): LiveData<Station>
    fun read(stationIds: LongArray): LiveData<List<Station>>
    fun update(station: Station): LiveData<Boolean>
    fun delete(station: Station): LiveData<Boolean>
    fun exists(station: Station): LiveData<Boolean>

}

class StationRepositoryImpl(private val dataSource: StationDataSource): StationRepository {

    override fun create(station: Station): MutableLiveData<Long> {
        val id: MutableLiveData<Long> = MutableLiveData()
        bg {
            id.postValue(dataSource.insert(station))
        }
        return id
    }

    override fun read(): LiveData<List<Station>> {
        return dataSource.select()
    }

    override fun read(stationIds: LongArray): LiveData<List<Station>> {
        return dataSource.select(stationIds)
    }

    override fun read(stationId: Long): LiveData<Station> {
        val station: MutableLiveData<Station> = MutableLiveData()
        bg {
            station.postValue(dataSource.select(stationId))
        }
        return station
    }

    override fun update(station: Station): LiveData<Boolean> {
        val updated: MutableLiveData<Boolean> = MutableLiveData()
        bg {
            updated.postValue(dataSource.update(station) > 0)
        }
        return updated
    }

    override fun delete(station: Station): LiveData<Boolean> {
        val deleted = MutableLiveData<Boolean>()
        bg {
            deleted.postValue(dataSource.delete(station) == 1)
        }
        return deleted
    }

    override fun exists(station: Station): LiveData<Boolean> {
        val exists = MutableLiveData<Boolean>()
        bg {
            exists.postValue(dataSource.exists(station.id) == 1)
        }
        return exists
    }
}