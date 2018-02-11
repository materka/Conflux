package se.materka.conflux.ui

import android.databinding.BaseObservable
import android.databinding.Bindable
import se.materka.conflux.BR
import se.materka.conflux.db.model.Station

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

class MetadataBinding : BaseObservable() {
    var artist: String? = null
        @Bindable
        get() = field
        set(value) {
            field = value
            notifyPropertyChanged(BR.artist)
        }
    var title: String? = null
        @Bindable
        get() = field
        set(value) {
            field = value
            notifyPropertyChanged(BR.title)
        }
    var show: String? = null
        @Bindable
        get() = field
        set(value) {
            field = value
            notifyPropertyChanged(BR.show)
        }

    var album: String? = null
        @Bindable
        get() = field
        set(value) {
            field = value
            notifyPropertyChanged(BR.album)
        }

    var station: Station = Station()
        @Bindable
        get() = field
        set(value) {
            field = value
            notifyPropertyChanged(BR.station)
        }

    fun clear() {
        artist = null
        title = null
        show = null
        album = null
        station = Station().apply { name = ""; url = "" }
    }
}