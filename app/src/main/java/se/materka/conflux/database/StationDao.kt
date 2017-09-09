package se.materka.conflux.database

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.*
import se.materka.conflux.database.Station

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
interface StationDao {
    @get:Query("SELECT * FROM station ORDER BY name ASC")
    val all: LiveData<List<Station>>

    @Query("SELECT * FROM station WHERE id IN (:stationIds)")
    fun loadAllByIds(stationIds: IntArray): LiveData<List<Station>>

    @Query("SELECT * FROM station WHERE id = :id")
    fun get(id: Long?): Station?

    @Query("SELECT 1 FROM station WHERE id = :id")
    fun exists(id: Long?): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(station: Station): Long

    @Update
    fun update(station: Station): Int

    @Delete
    fun delete(station: Station): Int
}