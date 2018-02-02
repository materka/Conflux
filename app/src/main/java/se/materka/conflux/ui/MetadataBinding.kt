package se.materka.conflux.ui

import android.databinding.BaseObservable
import android.databinding.Bindable
import se.materka.conflux.BR

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
    private var artist: String? = null
    private var title: String? = null
    private var show: String? = null
    private var album: String? = null

    @Bindable
    fun getArtist(): String? {
        return this.artist
    }

    @Bindable
    fun getShow(): String? {
        return this.show
    }

    @Bindable
    fun getTitle(): String? {
        return this.title
    }

    @Bindable
    fun getAlbum(): String? {
        return this.album
    }


    fun setArtist(artist: String?) {
        this.artist = artist
        notifyPropertyChanged(BR.artist)
    }

    fun setShow(show: String?) {
        this.show = show
        notifyPropertyChanged(BR.show)
    }

    fun setTitle(title: String?) {
        this.title = title
        notifyPropertyChanged(BR.title)
    }

    fun setAlbum(album: String?) {
        this.album = album
        notifyPropertyChanged(BR.album)
    }

    fun clear() {
        setArtist(null)
        setTitle(null)
        setShow(null)
        setAlbum(null)
    }
}