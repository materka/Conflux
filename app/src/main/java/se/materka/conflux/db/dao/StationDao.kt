package se.materka.conflux.db.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import se.materka.conflux.db.entity.Station

/**
 * Copyright Mattias Karlsson

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

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(station: Station): Long

    @Query("SELECT * FROM stations ORDER BY name ASC")
    fun selectAll(): LiveData<List<Station>>

    @Query("SELECT * FROM stations WHERE id = :id")
    fun select(id: Long?): LiveData<Station>

    @Query("SELECT * FROM stations WHERE url = :url")
    fun selectWithUrl(url: String): LiveData<Station>

    @Update
    fun update(station: Station): Int

    @Delete
    fun delete(station: Station): Int

    @Query("SELECT 1 FROM stations WHERE url = :uri")
    fun exists(uri: String?): Int
}