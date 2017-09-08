package se.materka.conflux.database

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.*
import se.materka.conflux.database.Station


/**
 * Created by Privat on 5/20/2017.
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