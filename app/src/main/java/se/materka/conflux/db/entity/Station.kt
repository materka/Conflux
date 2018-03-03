package se.materka.conflux.db.entity

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import android.os.Parcel
import android.os.Parcelable

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

@Entity(tableName = "stations")
class Station() : Parcelable {
    @PrimaryKey(autoGenerate = true)
    var id: Long? = null
    var name: String? = null
    var url: String? = null
    var channels: Long? = null
    var bitrate: Long? = null
    var genre: String? = null
    var format: String? = null

    val isPersisted: Boolean
        get() = id != null

    constructor(parcel: Parcel) : this() {
        id = parcel.readValue(Long::class.java.classLoader) as? Long
        name = parcel.readString()
        url = parcel.readString()
        channels = parcel.readValue(Long::class.java.classLoader) as? Long
        bitrate = parcel.readValue(Long::class.java.classLoader) as? Long
        genre = parcel.readString()
        format = parcel.readString()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeValue(id)
        parcel.writeString(name)
        parcel.writeString(url)
        parcel.writeValue(channels)
        parcel.writeValue(bitrate)
        parcel.writeString(genre)
        parcel.writeString(format)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Station> {
        override fun createFromParcel(parcel: Parcel): Station {
            return Station(parcel)
        }

        override fun newArray(size: Int): Array<Station?> {
            return arrayOfNulls(size)
        }
    }
}