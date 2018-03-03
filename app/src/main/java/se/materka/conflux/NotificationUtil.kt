package se.materka.conflux

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.support.v4.app.NotificationCompat
import android.support.v4.content.ContextCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaButtonReceiver
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import se.materka.exoplayershoutcastdatasource.ShoutcastMetadata


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

object NotificationUtil {

    fun build(context: Context, mediaSession: MediaSessionCompat): Notification {
        val builder = getBuilder(context, mediaSession)
        val style = getMediaStyle(context, mediaSession)
        return builder.setStyle(style).build()
    }

    @SuppressLint("NewApi")
    private fun getChannel(context: Context): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            // The id of the channel
            val id = "conflux_channel_1"
            // The user-visible name of the channel
            val name = "Conflux"
            // The user-visible description of the channel
            val description = "Conflux Player"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(id, name, importance)
            // Configure the notification channel
            channel.description = description
            notificationManager.createNotificationChannel(channel)
            id
        } else {
            NotificationChannel.DEFAULT_CHANNEL_ID
        }
    }

    private fun getBuilder(context: Context, mediaSession: MediaSessionCompat): NotificationCompat.Builder {
        val controller: MediaControllerCompat = mediaSession.controller
        val mediaMetadata: MediaMetadataCompat? = controller.metadata

        return NotificationCompat.Builder(context, getChannel(context)).apply {
            color = ContextCompat.getColor(context, R.color.primary_dark)

            // Add the metadata for the currently playing track
            setContentTitle(mediaMetadata?.getString(ShoutcastMetadata.METADATA_KEY_TITLE))
            setContentText(mediaMetadata?.getString(ShoutcastMetadata.METADATA_KEY_ARTIST))
            setSubText(mediaMetadata?.getString(ShoutcastMetadata.METADATA_KEY_STATION))

            // Add an app icon
            setSmallIcon(R.drawable.md_play)

            // Enable launching the player by clicking the notification
            setContentIntent(controller.sessionActivity)

            // Stop the service when the notification is swiped away
            setDeleteIntent(MediaButtonReceiver.buildMediaButtonPendingIntent(context.applicationContext, PlaybackStateCompat.ACTION_STOP))

            // Make the transport controls visible on the lockscreen
            setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

            // Only Alert once with sound, ticker and vibrate when notification is shown
            setOnlyAlertOnce(true)

            // Set notification to be ongoing, meaning it will be placed at the top of notifications
            // and not affected by "Clear All"-button
            setOngoing(mediaSession.isActive)

            // Add action button, either ACTION_PLAY or ACTION_STOP, depending on media session state
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