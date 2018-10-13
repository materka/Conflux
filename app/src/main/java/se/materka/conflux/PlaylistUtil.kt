package se.materka.conflux

import android.net.Uri
import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import se.materka.conflux.PlaylistUtil.PlaylistType.*
import se.materka.conflux.parsers.M3UParser
import se.materka.conflux.parsers.PLSParser
import java.io.File
import java.io.FileInputStream
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

    data class Channel(val name: String?, val uri: Uri?)

    enum class PlaylistType(val format: String) {
        M3U("m3u"),
        M3U8("m3u8"),
        PLS("pls");

        companion object {
            fun fromUri(uri: String): PlaylistType? {
                return when {
                    uri.endsWith(M3U.format) -> M3U
                    uri.endsWith(M3U8.format) -> M3U8
                    uri.endsWith(PLS.format) -> PLS
                    else -> null
                }
            }
        }
    }

    private const val HTTP: String = "http"
    private const val HTTPS: String = "https"

    fun getPlaylist(uri: Uri): List<Channel> {
        val playlist: MutableList<Channel> = mutableListOf()
        val playlistType: PlaylistType = PlaylistType.fromUri(uri.toString()) ?: return playlist
        var inputStream: InputStream? = null

        var response: Response? = null
        try {
            if (listOf(HTTP, HTTPS).any { scheme -> scheme == uri.scheme }) {
                Request.Builder().url(uri.toString()).get().build().also { request ->
                    response = OkHttpClient().newCall(request).execute()
                    if (response?.code() == 200) {
                        inputStream = response?.body()?.byteStream()
                    }
                }
            } else {
                inputStream = FileInputStream(File(uri.toString()))
            }
            inputStream?.let { stream -> parse(stream, playlistType) }
                    ?.also { playlist.addAll(it) }
        } catch (e: Exception) {
            Log.e(TAG, "Request for playlist failed")
        } finally {
            response?.close()
        }
        return playlist
    }

    private fun parse(stream: InputStream, type: PlaylistType): List<Channel>? {
        return when (type) {
            M3U, M3U8 -> {
                val result = M3UParser.parse(stream)
                result.segments.map { Channel(it.title, it.uri) }
            }
            PLS -> {
                val result = PLSParser.parse(stream)
                result.tracks.map { Channel(it.title, Uri.parse(it.file)) }
            }
        }
    }
}