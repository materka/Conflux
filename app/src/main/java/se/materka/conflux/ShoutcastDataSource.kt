package se.materka.conflux

/*
 * Copyright (C) 2016 The Android Open Source Project
 * Copyright (C) 2018 Mattias Karlsson (Modifications)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.net.Uri
import android.util.Log
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.upstream.BaseDataSource
import com.google.android.exoplayer2.upstream.DataSourceException
import com.google.android.exoplayer2.upstream.DataSpec
import com.google.android.exoplayer2.upstream.HttpDataSource
import com.google.android.exoplayer2.util.Assertions
import com.google.android.exoplayer2.util.Predicate
import com.google.android.exoplayer2.util.Util
import com.google.android.exoplayer2.util.Util.castNonNull
import okhttp3.*
import se.materka.conflux.stream.IcyInputStream
import se.materka.conflux.stream.OggInputStream
import se.materka.conflux.stream.ShoutcastStreamListener
import se.materka.exoplayershoutcastdatasource.ShoutcastMetadata
import java.io.EOFException
import java.io.IOException
import java.io.InputStream
import java.io.InterruptedIOException

/** An [HttpDataSource] that delegates to Square's [Call.Factory].  */
open class ShoutcastDataSource
/**
 * @param callFactory A [Call.Factory] (typically an [okhttp3.OkHttpClient]) for use
 * by the source.
 * @param userAgent An optional User-Agent string.
 * @param contentTypePredicate An optional [Predicate]. If a content type is rejected by the
 * predicate then a [InvalidContentTypeException] is thrown from [     ][.open].
 * @param cacheControl An optional [CacheControl] for setting the Cache-Control header.
 * @param defaultRequestProperties The optional default [RequestProperties] to be sent to
 * the server as HTTP headers on every request.
 */
constructor(
        callFactory: Call.Factory,
        private val userAgent: String?,
        private val contentTypePredicate: Predicate<String>?,
        private val cacheControl: CacheControl? = null,
        private val defaultRequestProperties: HttpDataSource.RequestProperties? = null,
        private val metadataListener: ShoutcastMetadataListener) : BaseDataSource(true), HttpDataSource, ShoutcastStreamListener {

    interface ShoutcastMetadataListener {
        fun onMetadataReceived(data: ShoutcastMetadata)
    }

    private val callFactory: Call.Factory = Assertions.checkNotNull(callFactory)
    private val requestProperties: HttpDataSource.RequestProperties = HttpDataSource.RequestProperties().apply { set(ICY_METADATA, "1") }

    private var dataSpec: DataSpec? = null
    private var response: Response? = null
    private var responseByteStream: InputStream? = null
    private var opened: Boolean = false

    private var bytesToSkip: Long = 0
    private var bytesToRead: Long = 0

    private var bytesSkipped: Long = 0
    private var bytesRead: Long = 0

    override fun onMetadataReceived(artist: String?, title: String?, show: String?) {
        val channels = responseHeaders["icy-channels"]?.first()?.toLong()
        val format = ShoutcastDataSource.audioFormat[responseHeaders["Content-Type"]?.first()]
        val station = responseHeaders["icy-name"]?.first()
        val url = responseHeaders["icy-url"]?.first()
        val genre = responseHeaders["icy-genre"]?.first()
        val bitrate = responseHeaders["icy-br"]?.first()?.toLong()

        val metadata = ShoutcastMetadata.Builder()
                .putString(ShoutcastMetadata.METADATA_KEY_ARTIST, artist)
                .putString(ShoutcastMetadata.METADATA_KEY_TITLE, title)
                .putString(ShoutcastMetadata.METADATA_KEY_SHOW, show)
                .putString(ShoutcastMetadata.METADATA_KEY_GENRE, genre)
                .putString(ShoutcastMetadata.METADATA_KEY_STATION, station)
                .putString(ShoutcastMetadata.METADATA_KEY_FORMAT, format)
                .putString(ShoutcastMetadata.METADATA_KEY_URL, url)
                .putLong(ShoutcastMetadata.METADATA_KEY_BITRATE, bitrate)
                .putLong(ShoutcastMetadata.METADATA_KEY_CHANNELS, channels)
                .build()

        Log.d(TAG, "ShoutcastMetadata received\n$metadata")
        this.metadataListener.onMetadataReceived(metadata)
    }

    override fun getUri(): Uri? {
        return if (response == null) null else Uri.parse(response!!.request().url().toString())
    }

    override fun getResponseHeaders(): Map<String, List<String>> {
        return if (response == null) emptyMap() else response!!.headers().toMultimap()
    }

    override fun setRequestProperty(name: String, value: String) {
        Assertions.checkNotNull(name)
        Assertions.checkNotNull(value)
        requestProperties.set(name, value)
    }

    override fun clearRequestProperty(name: String) {
        Assertions.checkNotNull(name)
        requestProperties.remove(name)
    }

    override fun clearAllRequestProperties() {
        requestProperties.clear()
    }

    @Throws(HttpDataSource.HttpDataSourceException::class)
    override fun open(dataSpec: DataSpec): Long {
        this.dataSpec = dataSpec
        this.bytesRead = 0
        this.bytesSkipped = 0
        transferInitializing(dataSpec)

        val request = makeRequest(dataSpec)
        val response: Response?
        val responseBody: ResponseBody
        try {
            this.response = callFactory.newCall(request).execute()
            response = this.response
            responseBody = Assertions.checkNotNull(response!!.body()!!)
            responseByteStream = filter(responseBody.byteStream())
        } catch (e: IOException) {
            throw HttpDataSource.HttpDataSourceException(
                    "Unable to connect to " + dataSpec.uri, e, dataSpec, HttpDataSource.HttpDataSourceException.TYPE_OPEN)
        }

        val responseCode = response.code()

        // Check for a valid response code.
        if (!response.isSuccessful) {
            val headers = response.headers().toMultimap()
            closeConnectionQuietly()
            val exception = HttpDataSource.InvalidResponseCodeException(
                    responseCode, headers, dataSpec)
            if (responseCode == 416) {
                exception.initCause(DataSourceException(DataSourceException.POSITION_OUT_OF_RANGE))
            }
            throw exception
        }

        // Check for a valid content type.
        val mediaType = responseBody.contentType()
        val contentType = mediaType?.toString() ?: ""
        if (contentTypePredicate != null && !contentTypePredicate.evaluate(contentType)) {
            closeConnectionQuietly()
            throw HttpDataSource.InvalidContentTypeException(contentType, dataSpec)
        }

        // If we requested a range starting from a non-zero position and received a 200 rather than a
        // 206, then the server does not support partial requests. We'll need to manually skip to the
        // requested position.
        bytesToSkip = if (responseCode == 200 && dataSpec.position != 0L) dataSpec.position else 0

        // Determine the length of the data to be read, after skipping.
        if (dataSpec.length != C.LENGTH_UNSET.toLong()) {
            bytesToRead = dataSpec.length
        } else {
            val contentLength = responseBody.contentLength()
            bytesToRead = if (contentLength != -1L) contentLength - bytesToSkip else C.LENGTH_UNSET.toLong()
        }

        opened = true
        transferStarted(dataSpec)

        return bytesToRead
    }

    @Throws(HttpDataSource.HttpDataSourceException::class)
    override fun read(buffer: ByteArray, offset: Int, readLength: Int): Int {
        try {
            skipInternal()
            return readInternal(buffer, offset, readLength)
        } catch (e: IOException) {
            throw HttpDataSource.HttpDataSourceException(
                    e, Assertions.checkNotNull(dataSpec!!), HttpDataSource.HttpDataSourceException.TYPE_READ)
        }

    }

    @Throws(HttpDataSource.HttpDataSourceException::class)
    override fun close() {
        if (opened) {
            opened = false
            transferEnded()
            closeConnectionQuietly()
        }
    }

    /**
     * Filter the supplied stream for metadata
     *
     * @param stream The unfiltered shoutcast stream. Supports MP3, AAC, AACP and OGG
     * @return InputStream which has been filtered for metadata
     */
    private fun filter(stream: InputStream): InputStream? {
        var filteredStream: InputStream = stream

        val interval = responseHeaders[ShoutcastDataSource.ICY_METAINT]?.first()?.toInt()
        responseHeaders["Content-Type"]?.first()?.let { contentType ->
            if (ShoutcastDataSource.audioFormat.containsKey(contentType)) {
                filteredStream = when (ShoutcastDataSource.audioFormat[contentType]) {
                    "MP3", "AAC", "AACP" -> {
                        IcyInputStream(stream, interval ?: 0, this)
                    }
                    "OGG" -> OggInputStream(stream, this)
                    else -> {
                        Log.e(TAG, "Unsupported format for extracting metadata")
                        stream
                    }
                }

            }
        }
        return filteredStream
    }

    /**
     * Returns the number of bytes that have been skipped since the most recent call to
     * [.open].
     *
     * @return The number of bytes skipped.
     */
    protected fun bytesSkipped(): Long {
        return bytesSkipped
    }

    /**
     * Returns the number of bytes that have been read since the most recent call to
     * [.open].
     *
     * @return The number of bytes read.
     */
    protected fun bytesRead(): Long {
        return bytesRead
    }

    /**
     * Returns the number of bytes that are still to be read for the current [DataSpec].
     *
     *
     * If the total length of the data being read is known, then this length minus `bytesRead()`
     * is returned. If the total length is unknown, [C.LENGTH_UNSET] is returned.
     *
     * @return The remaining length, or [C.LENGTH_UNSET].
     */
    protected fun bytesRemaining(): Long {
        return if (bytesToRead == C.LENGTH_UNSET.toLong()) bytesToRead else bytesToRead - bytesRead
    }

    /** Establishes a connection.  */
    @Throws(HttpDataSource.HttpDataSourceException::class)
    private fun makeRequest(dataSpec: DataSpec): Request {
        val position = dataSpec.position
        val length = dataSpec.length
        val allowGzip = dataSpec.isFlagSet(DataSpec.FLAG_ALLOW_GZIP)

        val url = HttpUrl.parse(dataSpec.uri.toString())
                ?: throw HttpDataSource.HttpDataSourceException(
                        "Malformed URL", dataSpec, HttpDataSource.HttpDataSourceException.TYPE_OPEN)

        val builder = Request.Builder().url(url)
        if (cacheControl != null) {
            builder.cacheControl(cacheControl)
        }
        if (defaultRequestProperties != null) {
            for ((key, value) in defaultRequestProperties.snapshot) {
                builder.header(key, value)
            }
        }
        for ((key, value) in requestProperties.snapshot) {
            builder.header(key, value)
        }
        if (!(position == 0L && length == C.LENGTH_UNSET.toLong())) {
            var rangeRequest = "bytes=$position-"
            if (length != C.LENGTH_UNSET.toLong()) {
                rangeRequest += position + length - 1
            }
            builder.addHeader("Range", rangeRequest)
        }
        if (userAgent != null) {
            builder.addHeader("User-Agent", userAgent)
        }

        if (!allowGzip) {
            builder.addHeader("Accept-Encoding", "identity")
        }
        var requestBody: RequestBody? = null
        if (dataSpec.httpBody != null) {
            requestBody = RequestBody.create(null, dataSpec.httpBody!!)
        } else if (dataSpec.httpMethod == DataSpec.HTTP_METHOD_POST) {
            // OkHttp requires a non-null body for POST requests.
            requestBody = RequestBody.create(null, Util.EMPTY_BYTE_ARRAY)
        }
        builder.method(dataSpec.httpMethodString, requestBody)
        return builder.build()
    }

    /**
     * Skips any bytes that need skipping. Else does nothing.
     *
     *
     * This implementation is based roughly on `libcore.io.Streams.skipByReading()`.
     *
     * @throws InterruptedIOException If the thread is interrupted during the operation.
     * @throws EOFException If the end of the input stream is reached before the bytes are skipped.
     */
    @Throws(IOException::class)
    private fun skipInternal() {
        if (bytesSkipped == bytesToSkip) {
            return
        }

        while (bytesSkipped != bytesToSkip) {
            val readLength = Math.min(bytesToSkip - bytesSkipped, SKIP_BUFFER.size.toLong()).toInt()
            val read = castNonNull(responseByteStream).read(SKIP_BUFFER, 0, readLength)
            if (Thread.currentThread().isInterrupted) {
                throw InterruptedIOException()
            }
            if (read == -1) {
                throw EOFException()
            }
            bytesSkipped += read.toLong()
            bytesTransferred(read)
        }
    }

    /**
     * Reads up to `length` bytes of data and stores them into `buffer`, starting at
     * index `offset`.
     *
     *
     * This method blocks until at least one byte of data can be read, the end of the opened range is
     * detected, or an exception is thrown.
     *
     * @param buffer The buffer into which the read data should be stored.
     * @param offset The start offset into `buffer` at which data should be written.
     * @param readLength The maximum number of bytes to read.
     * @return The number of bytes read, or [C.RESULT_END_OF_INPUT] if the end of the opened
     * range is reached.
     * @throws IOException If an error occurs reading from the source.
     */
    @Throws(IOException::class)
    private fun readInternal(buffer: ByteArray, offset: Int, readLength: Int): Int {
        var rl = readLength
        if (rl == 0) {
            return 0
        }
        if (bytesToRead != C.LENGTH_UNSET.toLong()) {
            val bytesRemaining = bytesToRead - bytesRead
            if (bytesRemaining == 0L) {
                return C.RESULT_END_OF_INPUT
            }
            rl = Math.min(readLength.toLong(), bytesRemaining).toInt()
        }

        val read = castNonNull(responseByteStream).read(buffer, offset, rl)
        if (read == -1) {
            if (bytesToRead != C.LENGTH_UNSET.toLong()) {
                // End of stream reached having not read sufficient data.
                throw EOFException()
            }
            return C.RESULT_END_OF_INPUT
        }

        bytesRead += read.toLong()
        bytesTransferred(read)
        return read
    }

    /**
     * Closes the current connection quietly, if there is one.
     */
    private fun closeConnectionQuietly() {
        if (response != null) {
            Assertions.checkNotNull(response!!.body()!!).close()
            response = null
        }
        responseByteStream = null
    }

    companion object {

        private val SKIP_BUFFER = ByteArray(4096)
        val audioFormat = hashMapOf(
                Pair("audio/mpeg", "MP3"),
                Pair("audio/aac", "AAC"),
                Pair("audio/aacp", "AACP"),
                Pair("application/ogg", "OGG")
        )

        const val ICY_METADATA = "Icy-Metadata"
        const val ICY_METAINT = "icy-metaint"
    }

}