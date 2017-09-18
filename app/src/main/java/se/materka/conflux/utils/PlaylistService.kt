package se.materka.conflux.utils

import android.net.Uri
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.launch
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

    suspend fun getPlaylist(uri: Uri): MutableList<Uri> {
        val playlist: MutableList<Uri> = mutableListOf()

        if (!isPlayList(uri)) return playlist

        launch(CommonPool) {
            val request: Request = Request.Builder()
                    .url(uri.toString())
                    .get()
                    .build()

            val response = OkHttpClient().newCall(request).execute()
            val stream: InputStream? = response.body()?.byteStream()
            stream?.use { stream ->
                stream.apply {
                    bufferedReader().use {
                        it.readLines().forEach { line ->
                            line.trim().let {
                                if (uri.path.endsWith("pls")) {
                                    if (it.contains("http")) {
                                        playlist.add(Uri.parse(it.substring(it.indexOf("http"))))
                                    }
                                } else if (uri.path.endsWith("m3u") || uri.path.endsWith("m3u8")) {
                                    if (it.isNotEmpty() && it[0] != '#' && it[0] != '<') {
                                        playlist.add(Uri.parse(it))
                                    }
                                }

                            }
                        }
                    }
                }
            }
        }.join()
        return playlist
    }


    fun isPlayList(uri: Uri): Boolean {
        return (arrayOf("http", "https").contains(uri.scheme) &&
                uri.path.endsWith("pls", true) ||
                uri.path.endsWith("m3u", true) ||
                uri.path.endsWith("m3u8", true))
    }
}