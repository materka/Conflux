package se.materka.conflux

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build
import com.google.android.exoplayer2.C
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

class AudioFocusManager(context: Context, private val onAudioFocusChangeCallback: () -> Unit) : AudioManager.OnAudioFocusChangeListener {

    private val audioManager: AudioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    private val audioAttributes: AudioAttributes? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .build()
    } else {
        null
    }
    private val audioFocusRequest: AudioFocusRequest? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                .setAudioAttributes(audioAttributes)
                .setFocusGain(AudioManager.AUDIOFOCUS_GAIN)
                .build()
    } else {
        null
    }
    var audioFocus: Int = AudioManager.AUDIOFOCUS_LOSS
        private set

    override fun onAudioFocusChange(focusChange: Int) {
        Timber.i("onAudioFocusChange. focusChange=$focusChange")
        audioFocus = focusChange
        onAudioFocusChangeCallback()
    }

    fun requestAudioFocus(): Int {
        Timber.i("requestAudioFocus")
        audioFocus = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (audioManager.requestAudioFocus(audioFocusRequest) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED)
                AudioManager.AUDIOFOCUS_GAIN
            else
                AudioManager.AUDIOFOCUS_LOSS
        } else {
            if (audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC,
                    AudioManager.AUDIOFOCUS_GAIN) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED)
                AudioManager.AUDIOFOCUS_GAIN
            else
                AudioManager.AUDIOFOCUS_LOSS
        }
        return audioFocus
    }

    fun abandonAudioFocus() {
        Timber.i("abandonAudioFocus")
        if (audioFocus == AudioManager.AUDIOFOCUS_LOSS) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                audioManager.abandonAudioFocusRequest(audioFocusRequest)
            } else {
                audioManager.abandonAudioFocus(this)
            }
        }
    }
}