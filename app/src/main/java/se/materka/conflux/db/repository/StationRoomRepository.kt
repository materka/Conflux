package se.materka.conflux.db.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.experimental.launch
import se.materka.conflux.db.dao.StationDao
import se.materka.conflux.db.entity.Station

class StationRoomRepository(private val dao: StationDao) : Repository<Station> {

    override fun save(station: Station): MutableLiveData<Boolean> {
        val id: MutableLiveData<Boolean> = MutableLiveData()
        launch {
            id.postValue(dao.insert(station) > 0)
        }
        return id
    }

    override fun get(): LiveData<List<Station>> {
        return dao.selectAll()
    }

    override fun get(id: Long): LiveData<Station> {
        return dao.select(id)
    }

    override fun update(item: Station): LiveData<Boolean> {
        val updated: MutableLiveData<Boolean> = MutableLiveData()
        launch {
            updated.postValue(dao.update(item) > 0)
        }
        return updated
    }

    override fun delete(item: Station): LiveData<Boolean> {
        val deleted = MutableLiveData<Boolean>()
        launch {
            deleted.postValue(dao.delete(item) == 1)
        }
        return deleted
    }

    override fun exists(uri: String): LiveData<Boolean> {
        val exists = MutableLiveData<Boolean>()
        launch {
            exists.postValue(dao.exists(uri) == 1)
        }
        return exists
    }
}