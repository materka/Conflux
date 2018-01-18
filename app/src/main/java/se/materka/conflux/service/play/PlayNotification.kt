package se.materka.conflux.service.play

import android.app.Notification
import android.content.Context
import android.support.v4.app.NotificationCompat
import android.support.v4.content.ContextCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaButtonReceiver
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import se.materka.conflux.R

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

object PlayNotification {

    private val DEFAULT_CHANNEL_ID: String = "miscellaneous"

    fun buildNotification(context: Context, mediaSession: MediaSessionCompat): Notification {
        val builder = getBuilder(context, mediaSession)
        val style = getMediaStyle(context, mediaSession)
        return builder.setStyle(style).build()
    }

    private fun getBuilder(context: Context, mediaSession: MediaSessionCompat): NotificationCompat.Builder {
        val controller: MediaControllerCompat = mediaSession.controller
        val mediaMetadata: MediaMetadataCompat? = controller.metadata

        return NotificationCompat.Builder(context, DEFAULT_CHANNEL_ID).apply {
            color = ContextCompat.getColor(context, R.color.primary_dark)

            setContentTitle(mediaMetadata?.getString(MediaMetadataCompat.METADATA_KEY_TITLE))
            setContentText(mediaMetadata?.getString(MediaMetadataCompat.METADATA_KEY_ARTIST))
            setSubText(mediaMetadata?.getString(MediaMetadataCompat.METADATA_KEY_TITLE))
            setLargeIcon(mediaMetadata?.getBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART))
            setSmallIcon(R.drawable.md_play)
            setOngoing(mediaSession.isActive)
            setContentIntent(controller.sessionActivity)
            setDeleteIntent(MediaButtonReceiver.buildMediaButtonPendingIntent(context.applicationContext, PlaybackStateCompat.ACTION_STOP))
            setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            addAction(getAction(context, mediaSession))
        }
    }

    private fun getAction(context: Context, mediaSession: MediaSessionCompat): NotificationCompat.Action {
        return if (!mediaSession.isActive)
            NotificationCompat.Action(
                    R.drawable.md_play, "PLAY",
                    MediaButtonReceiver.buildMediaButtonPendingIntent(context.applicationContext,
                            PlaybackStateCompat.ACTION_PLAY)
            )
        else
            NotificationCompat.Action(
                    R.drawable.md_stop, "STOP",
                    MediaButtonReceiver.buildMediaButtonPendingIntent(context.applicationContext,
                            PlaybackStateCompat.ACTION_STOP)
            )
    }

    private fun getMediaStyle(context: Context, mediaSession: MediaSessionCompat): android.support.v4.media.app.NotificationCompat.MediaStyle {
        return android.support.v4.media.app.NotificationCompat.MediaStyle()
                .setMediaSession(mediaSession.sessionToken)
                .setShowActionsInCompactView(0)
                .setShowCancelButton(!mediaSession.isActive)
                .setCancelButtonIntent(MediaButtonReceiver.buildMediaButtonPendingIntent(context.applicationContext, PlaybackStateCompat.ACTION_STOP))
    }
}