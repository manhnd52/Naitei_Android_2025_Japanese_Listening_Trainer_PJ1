package com.sun.japaneselisteningtrainer.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.toArgb
import androidx.core.app.NotificationCompat
import androidx.media.app.NotificationCompat as MediaNotificationCompat
import androidx.media3.common.Player
import com.google.common.primitives.UnsignedBytes.toInt
import com.sun.japaneselisteningtrainer.MainActivity
import com.sun.japaneselisteningtrainer.R
import com.sun.japaneselisteningtrainer.data.model.Audio
import com.sun.japaneselisteningtrainer.ui.theme.md_theme_light_surfaceVariant

class AudioNotificationManager(private val context: Context) {
    
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    
    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                AudioServiceConstants.NOTIFICATION_CHANNEL_ID,
                AudioServiceConstants.NOTIFICATION_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Hiển thị trạng thái phát nhạc"
                setShowBadge(false)
                setSound(null, null)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun createNotification(
        audio: Audio,
        isPlaying: Boolean,
        position: Long,
        duration: Long
    ): Notification {
        
        // Intent để mở app khi tap vào notification
        val contentIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val contentPendingIntent = PendingIntent.getActivity(
            context, 0, contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val largeIcon = BitmapFactory.decodeResource(context.resources, R.drawable.mint_green_folder_with_blossom)

        return NotificationCompat.Builder(context, AudioServiceConstants.NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_headphones)
            .setColorized(true)
            .setColor(md_theme_light_surfaceVariant.toArgb())
            .setContentTitle(audio.title)
            .setContentIntent(contentPendingIntent)
            .setLargeIcon(largeIcon)
            .setOnlyAlertOnce(true)
            .setOngoing(isPlaying) // đang phát thì “ghim” để tránh vuốt tắt nhầm
            .addAction(createPreviousAction())
            .addAction(createPlayPauseAction(isPlaying))
            .addAction(createNextAction())
            .setStyle(
                MediaNotificationCompat.MediaStyle().setShowActionsInCompactView(0, 1, 2) // ⏪10s, Play/Pause, Next
            )
            .build()
    }

    private fun createPreviousAction(): NotificationCompat.Action {
        val intent = Intent(context, AudioService::class.java).apply {
            action = AudioServiceConstants.ACTION_PREVIOUS
        }
        val pendingIntent = PendingIntent.getService(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        return NotificationCompat.Action(
            R.drawable.ic_prev,
            "Previous",
            pendingIntent
        )
    }

    private fun createPlayPauseAction(isPlaying: Boolean): NotificationCompat.Action {
        val action = if (isPlaying) {
            AudioServiceConstants.ACTION_PAUSE
        } else {
            AudioServiceConstants.ACTION_PLAY
        }
        
        val intent = Intent(context, AudioService::class.java).apply {
            this.action = action
        }
        val pendingIntent = PendingIntent.getService(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val iconRes = if (isPlaying) {
            R.drawable.ic_pause
        } else {
            R.drawable.ic_play
        }
        
        val title = if (isPlaying) "Pause" else "Play"
        
        return NotificationCompat.Action(iconRes, title, pendingIntent)
    }

    private fun createNextAction(): NotificationCompat.Action {
        val intent = Intent(context, AudioService::class.java).apply {
            action = AudioServiceConstants.ACTION_NEXT
        }
        val pendingIntent = PendingIntent.getService(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        return NotificationCompat.Action(
            R.drawable.ic_next,
            "Next",
            pendingIntent
        )
    }

    fun updateNotification(notification: Notification) {
        notificationManager.notify(AudioServiceConstants.NOTIFICATION_ID, notification)
    }

    fun cancelNotification() {
        notificationManager.cancel(AudioServiceConstants.NOTIFICATION_ID)
    }
}
