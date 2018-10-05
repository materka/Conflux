package se.materka.conflux

import android.net.Uri
import android.util.Log
import kotlinx.coroutines.experimental.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import java.io.InputStream

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

object PlaylistUtil {

    enum class PlaylistType(val format: String) {
        M3U("m3u"),
        M3U8("m3u8"),
        PLS("pls")
    }

    private const val HTTP: String = "http"
    private const val HTTPS: String = "https"

    fun getPlaylist(uri: Uri): MutableList<Uri> {
        val playlist: MutableList<Uri> = mutableListOf()

        if (!isPlayList(uri)) return playlist
        val request: Request = Request.Builder()
                .url(uri.toString())
                .get()
                .build()
        var response: Response? = null
        try {
            response = OkHttpClient().newCall(request).execute()
            val inputStream: InputStream? = response.body()?.byteStream()
            inputStream?.use { stream ->
                stream.apply {
                    bufferedReader().use { reader ->
                        reader.readLines().forEach { line ->
                            line.trim().run {
                                if (uri.path.endsWith(PlaylistType.PLS.format, true)) {
                                    if (contains(HTTP, true)) {
                                        playlist.add(Uri.parse(substring(indexOf(HTTP, 0, true))))
                                    }
                                } else if (uri.path.endsWith(PlaylistType.M3U.format, true) || uri.path.endsWith(PlaylistType.M3U8.format, true)) {
                                    if (isNotEmpty() && this[0] != '#' && this[0] != '<') {
                                        playlist.add(Uri.parse(this))
                                    }
                                }

                                }
                            }
                        }
                    }
                }

        } catch (e: IOException) {
            Log.e(TAG, "Playlist parser failed")
        } finally {
            response?.close()
        }
        return playlist
    }


    fun isPlayList(uri: Uri): Boolean {
        return (arrayOf(HTTP, HTTPS).contains(uri.scheme) &&
                uri.path.endsWith(PlaylistType.PLS.format, true) ||
                uri.path.endsWith(PlaylistType.M3U.format, true) ||
                uri.path.endsWith(PlaylistType.M3U8.format, true))
    }
}