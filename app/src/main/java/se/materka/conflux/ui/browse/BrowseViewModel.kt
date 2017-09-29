package se.materka.conflux.ui.browse

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import se.materka.conflux.AppDatabase
import se.materka.conflux.domain.Station
import se.materka.conflux.domain.StationDao


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

class BrowseViewModel(application: Application) : AndroidViewModel(application) {

    private val dao: StationDao by lazy {
        AppDatabase.Companion.instance(application).stationDao()
    }

    val selected = MutableLiveData<Station>()
        get() = field

    fun select(station: Station?) {
        selected.value = station
    }

    fun getStations(): LiveData<List<Station>>? {
        return dao.all
    }

    fun saveStation(): LiveData<Long> {
        val result = MutableLiveData<Long>()
        selected.value?.let { s ->
            async(UI) {
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
            async(UI) {
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
            async(UI) {
                result.postValue(dao.update(station) == 1)
            }
        }
        return result
    }
}