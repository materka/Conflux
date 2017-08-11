package se.materka.conflux.database

import android.arch.persistence.room.Database
import android.arch.persistence.room.Room
import android.arch.persistence.room.RoomDatabase
import android.content.Context
import se.materka.conflux.model.Station


/**
 * Created by Privat on 5/20/2017.
 */

@Database(entities = arrayOf(Station::class), version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun stationDao(): StationDao

    companion object {
        private var instance: AppDatabase? = null
        fun instance(context: Context): AppDatabase {
            if (instance == null) {
                instance = Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, "monkey").build()
            }
            return instance!!
        }
    }
}