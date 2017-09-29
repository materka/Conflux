package se.materka.conflux.service

import android.net.Uri
import kaaes.spotify.webapi.android.SpotifyApi
import kaaes.spotify.webapi.android.SpotifyCallback
import kaaes.spotify.webapi.android.SpotifyError
import kaaes.spotify.webapi.android.models.ArtistsPager
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.async
import okhttp3.Credentials
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jetbrains.anko.coroutines.experimental.bg
import org.json.JSONObject
import retrofit.client.Response
import timber.log.Timber


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

class ArtistArtService {
    private var userName: String? = null
    private var password: String? = null

    private lateinit var callback: (Uri?) -> Unit


    fun setApiCredentials(userName: String, password: String): ArtistArtService {
        this.userName = userName
        this.password = password
        return this
    }

    private val searchArtists: SpotifyCallback<ArtistsPager> = object : SpotifyCallback<ArtistsPager>() {
        override fun success(artist: ArtistsPager?, response: Response?) {
            artist?.artists?.items?.let { artists ->
                if (!artists.isEmpty()) {
                    artists.sortedWith(compareBy { it.popularity })
                    val images = artists[0].images
                    if (!images.isEmpty()) {
                        callback.invoke(Uri.parse(images[0].url))
                    } else {
                        callback.invoke(null)
                    }
                }
            }
        }

        override fun failure(error: SpotifyError?) {
            Timber.d(error)
            callback.invoke(null)
        }
    }

    fun getArt(artist: String?, callback: (Uri?) -> Unit) {
        this.callback = callback
        try {
            async(CommonPool) {
                val api = SpotifyApi()
                val token = bg { getAccessToken() }
                api.setAccessToken(token.await())
                api.service.searchArtists(artist, searchArtists)
            }
        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    private fun getAccessToken(): String {
        val auth = Credentials.basic(userName, password)
        val request = Request.Builder()
                .url("https://accounts.spotify.com/api/token")
                .header("Authorization", auth)
                .post(FormBody.Builder().add("grant_type", "client_credentials").build())
                .build()
        val response = OkHttpClient().newCall(request).execute()
        return JSONObject(response.body()?.string())["access_token"].toString()
    }
}
