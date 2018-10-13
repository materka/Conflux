package se.materka.conflux.db.repository

import androidx.lifecycle.LiveData
import se.materka.conflux.db.entity.Station

/**
 * Created by Mattias on 1/18/2018.
 */

interface Repository<T> {
    fun save(item: T): LiveData<Boolean>
    fun get(): LiveData<List<T>>
    fun get(id: Long): LiveData<T>
    fun update(item: T): LiveData<Boolean>
    fun delete(item: T): LiveData<Boolean>
    fun exists(url: String): LiveData<Boolean>
}