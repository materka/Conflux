package se.materka.conflux.ui.viewmodel

import android.app.Application
import android.net.Uri
import android.support.v4.media.MediaBrowserCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.github.salomonbrys.kotson.fromJson
import com.google.gson.Gson
import org.koin.core.parameter.parametersOf
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject
import se.materka.conflux.RadioSession
import se.materka.conflux.db.entity.Station
import se.materka.conflux.db.repository.StationRepository

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

class MainActivityViewModel(application: Application, private val repository: StationRepository) : AndroidViewModel(application), KoinComponent {
    private val radioSession: RadioSession by inject { parametersOf("context" to application.applicationContext) }

    val items = MutableLiveData<List<MediaBrowserCompat.MediaItem>>().apply {
        postValue(listOf())
    }

    init {
        radioSession.subscribe("root", object : MediaBrowserCompat.SubscriptionCallback() {
            override fun onChildrenLoaded(parentId: String, children: MutableList<MediaBrowserCompat.MediaItem>) {
                super.onChildrenLoaded(parentId, children)
                items.postValue(children)
            }
        })
    }

    override fun onCleared() {
        super.onCleared()
        radioSession.unsubscribe("root")
    }

    fun select(item: MediaBrowserCompat.MediaItem) {
        radioSession.play(item)
    }

    fun select(uri: Uri) {
        radioSession.play(uri)
    }

    fun saveUri(uri: Uri, name: String) {
        repository.create(Gson().fromJson("{ 'name': '$name', 'url': '$uri'}"))
    }
}