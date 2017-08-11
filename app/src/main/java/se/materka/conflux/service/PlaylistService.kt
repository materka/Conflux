/**
 * Copyright 2016 Mattias Karlsson

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

package se.materka.conflux.service

import android.net.Uri
import io.reactivex.Observable
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.npr.android.util.M3uParser
import org.npr.android.util.PlsParser
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.io.IOException

object PlaylistService {

    fun downloadFile(uri: Uri): Observable<MutableList<Uri>> {
        return Observable.create { observer ->
            val bw: BufferedWriter
            val response: Response

            val playlist: MutableList<Uri> = mutableListOf()
            val file: File = File.createTempFile("playlist", "tmp")
            val client = OkHttpClient.Builder().build()
            val request = Request.Builder().url(uri.toString()).build()

            try {
                response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    bw = BufferedWriter(FileWriter(file))
                    bw.write(response.body().string())
                    bw.close()

                    if (uri.path.endsWith("pls")) {
                        playlist.addAll(PlsParser.getUris(file))
                    } else if (uri.path.endsWith("m3u") || uri.path.endsWith("m3u8")) {
                        playlist.addAll(M3uParser.getUrls(file))
                    }
                    observer.onNext(playlist)
                }
                response.close()
            } catch (e: IOException) {
                observer.onError(e)
            }
            observer.onComplete()
        }
    }

    fun isPlayList(uri: Uri): Boolean {
        return (arrayOf("http", "https").contains(uri.scheme) &&
                uri.path.endsWith("pls", true) || uri.path.endsWith("m3u", true) || uri.path.endsWith("m3u8", true))
    }

    fun getFirst(uri: Uri): Observable<String>? {
        return Observable.create { observer ->
            if (isPlayList(uri)) {
                val fileObserver = downloadFile(uri)
                fileObserver.subscribe({ list ->
                    if (!list.isEmpty()) observer.onNext(list[0].toString()) else observer.onError(Exception())
                })
            }
        }
    }
}

