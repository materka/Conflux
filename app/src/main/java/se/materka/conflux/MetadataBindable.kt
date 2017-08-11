package se.materka.conflux

import android.databinding.BaseObservable
import android.databinding.Bindable

/**
 * Created by Privat on 6/9/2017.
 */

class MetadataBindable : BaseObservable() {
    private var artist: String? = null
    private var song: String? = null
    private var show: String? = null

    @Bindable
    fun getArtist(): String? {
        return this.artist
    }

    @Bindable
    fun getShow(): String? {
        return this.show
    }

    @Bindable
    fun getSong(): String? {
        return this.song
    }

    fun setArtist(artist: String?) {
        this.artist = artist
        notifyPropertyChanged(BR.artist)
    }

    fun setShow(show: String?) {
        this.show = show
        notifyPropertyChanged(BR.show)
    }

    fun setSong(song: String?) {
        this.song = song
        notifyPropertyChanged(BR.song)
    }
}