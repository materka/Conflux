package se.materka.conflux

import android.arch.persistence.room.Database
import android.arch.persistence.room.Room
import android.arch.persistence.room.RoomDatabase
import android.content.Context
import se.materka.conflux.service.model.Station
import se.materka.conflux.service.repository.StationRepository

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

@Database(entities = [(Station::class)], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun stationRepository(): StationRepository

    companion object {
        private var db: AppDatabase? = null
        fun instance(context: Context): AppDatabase {
            if (db == null) {
                val name = context.resources.getString(R.string.app_name).decapitalize()
                db = Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, name).build()
            }
            return db!!
        }
    }
}