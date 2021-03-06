package se.materka.conflux.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import se.materka.conflux.R
import se.materka.conflux.db.dao.StationDao
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

@Database(entities = [(Station::class)], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun stationDao(): StationDao

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