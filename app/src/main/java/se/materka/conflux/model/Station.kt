package se.materka.conflux.model

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey

@Entity
class Station {
    @PrimaryKey(autoGenerate = true)
    var id: Long? = null
    var name: String? = null
    var url: String? = null
    var channels: Int? = null
    var bitrate: Int? = null
    var genre: String? = null
    var format: String? = null

    val isPersisted: Boolean
        get() = id != null
}