package se.materka.conflux.service

import android.net.Uri
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.InputStream

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

object PlaylistService {

    private val M3U: String = "m3u"
    private val M3U8: String = "m3u8"
    private val PLS: String = "pls"
    private val HTTP: String = "http"
    private val HTTPS: String = "https"

    suspend fun getPlaylist(uri: Uri): MutableList<Uri> {
        val playlist: MutableList<Uri> = mutableListOf()

        if (!isPlayList(uri)) return playlist
        val request: Request = Request.Builder()
                .url(uri.toString())
                .get()
                .build()

        val response = OkHttpClient().newCall(request).execute()
        val inputStream: InputStream? = response.body()?.byteStream()
        inputStream?.use { stream ->
            stream.apply {
                bufferedReader().use {
                    it.readLines().forEach { line ->
                        line.trim().let {
                            if (uri.path.endsWith(PLS, true)) {
                                if (it.contains(HTTP, true)) {
                                    playlist.add(Uri.parse(it.substring(it.indexOf(HTTP, 0, true))))
                                }
                            } else if (uri.path.endsWith(M3U, true) || uri.path.endsWith(M3U8, true)) {
                                if (it.isNotEmpty() && it[0] != '#' && it[0] != '<') {
                                    playlist.add(Uri.parse(it))
                                }
                            }

                        }
                    }
                }
            }
        }
        return playlist
    }


    fun isPlayList(uri: Uri): Boolean {
        return (arrayOf(HTTP, HTTPS).contains(uri.scheme) &&
                uri.path.endsWith(PLS, true) ||
                uri.path.endsWith(M3U, true) ||
                uri.path.endsWith(M3U8, true))
    }
}