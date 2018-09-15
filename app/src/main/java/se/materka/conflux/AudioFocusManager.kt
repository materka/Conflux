package se.materka.conflux

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build
import androidx.annotation.RequiresApi

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

open class AudioFocusManager private constructor(context: Context, private val onAudioFocusChangeCallback: (audioFocus: Int) -> Unit) : AudioManager.OnAudioFocusChangeListener {

    companion object {
        fun newInstance(context: Context, onAudioFocusChangeCallback: (audioFocus: Int) -> Unit): AudioFocusManager {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                AudioFocusManagerFromSdk26(context, onAudioFocusChangeCallback)
            } else {
                AudioFocusManagerBeforeSdk26(context, onAudioFocusChangeCallback)
            }
        }
    }

    protected val audioManager: AudioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    override fun onAudioFocusChange(focusChange: Int) = onAudioFocusChangeCallback(focusChange)

    open fun requestAudioFocus(): Int = AudioManager.AUDIOFOCUS_REQUEST_FAILED

    open fun abandonAudioFocus(): Int = AudioManager.AUDIOFOCUS_REQUEST_FAILED

    @Suppress("DEPRECATION")
    private class AudioFocusManagerBeforeSdk26(context: Context, onAudioFocusChangeCallback: (audioFocus: Int) -> Unit) :
            AudioFocusManager(context, onAudioFocusChangeCallback) {
        override fun requestAudioFocus(): Int {
            val requestGranted = audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC,
                    AudioManager.AUDIOFOCUS_GAIN)
            return if (requestGranted == AudioManager.AUDIOFOCUS_REQUEST_GRANTED)
                AudioManager.AUDIOFOCUS_GAIN
            else
                AudioManager.AUDIOFOCUS_LOSS
        }

        override fun abandonAudioFocus(): Int = audioManager.abandonAudioFocus(this)

    }

    @RequiresApi(Build.VERSION_CODES.O)
    private class AudioFocusManagerFromSdk26(context: Context, onAudioFocusChangeCallback: (audioFocus: Int) -> Unit) :
            AudioFocusManager(context, onAudioFocusChangeCallback) {
        private var audioFocusRequest: AudioFocusRequest

        init {
            val audioAttributes = AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .build()

            audioFocusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                    .setAudioAttributes(audioAttributes)
                    .setFocusGain(AudioManager.AUDIOFOCUS_GAIN)
                    .build()
        }

        override fun requestAudioFocus(): Int {
            return if (audioManager.requestAudioFocus(audioFocusRequest) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED)
                AudioManager.AUDIOFOCUS_GAIN
            else
                AudioManager.AUDIOFOCUS_LOSS
        }

        override fun abandonAudioFocus(): Int = audioManager.abandonAudioFocusRequest(audioFocusRequest)
    }
}