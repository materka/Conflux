package se.materka.conflux.service.datasource

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.*
import se.materka.conflux.service.model.Station

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

@Dao
interface StationDataSource {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(station: Station): Long

    @Query("SELECT * FROM station ORDER BY name ASC")
    fun select(): LiveData<List<Station>>

    @Query("SELECT * FROM station WHERE id IN (:arg0)")
    fun select(stationIds: LongArray): LiveData<List<Station>>

    @Query("SELECT * FROM station WHERE id = :arg0")
    fun select(id: Long?): Station

    @Update
    fun update(station: Station): Int

    @Delete
    fun delete(station: Station): Int

    @Query("SELECT 1 FROM station WHERE id = :arg0")
    fun exists(id: Long?): Int
}