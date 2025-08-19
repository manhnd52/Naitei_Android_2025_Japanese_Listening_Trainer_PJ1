package com.sun.japaneselisteningtrainer.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.media.app.NotificationCompat as MediaNotificationCompat
import androidx.media3.common.Player
import com.sun.japaneselisteningtrainer.MainActivity
import com.sun.japaneselisteningtrainer.R
import com.sun.japaneselisteningtrainer.data.model.Audio

/**
 * Quản lý notification cho audio playback
 * Hiển thị thông tin bài hát và controls trên notification
 */
class AudioNotificationManager(private val context: Context) {
    
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    
    init {
        createNotificationChannel()
    }
    
    /**
     * Tạo notification channel
     */
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
    
    /**
     * Tạo notification cho audio playback
     */
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
        
        return NotificationCompat.Builder(context, AudioServiceConstants.NOTIFICATION_CHANNEL_ID)
            .setContentTitle(audio.title)
            .setContentText("Japanese Listening Trainer")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(contentPendingIntent)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setShowWhen(false)
            .setOngoing(isPlaying)
            .addAction(createPreviousAction())
            .addAction(createPlayPauseAction(isPlaying))
            .addAction(createNextAction())
            .setStyle(
                MediaNotificationCompat.MediaStyle()
                    .setShowActionsInCompactView(0, 1, 2)
            )
            .build()
    }
    
    /**
     * Tạo action Previous
     */
    private fun createPreviousAction(): NotificationCompat.Action {
        val intent = Intent(context, AudioService::class.java).apply {
            action = AudioServiceConstants.ACTION_PREVIOUS
        }
        val pendingIntent = PendingIntent.getService(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        return NotificationCompat.Action(
            android.R.drawable.ic_media_previous,
            "Previous",
            pendingIntent
        )
    }
    
    /**
     * Tạo action Play/Pause
     */
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
            android.R.drawable.ic_media_pause
        } else {
            android.R.drawable.ic_media_play
        }
        
        val title = if (isPlaying) "Pause" else "Play"
        
        return NotificationCompat.Action(iconRes, title, pendingIntent)
    }
    
    /**
     * Tạo action Next
     */
    private fun createNextAction(): NotificationCompat.Action {
        val intent = Intent(context, AudioService::class.java).apply {
            action = AudioServiceConstants.ACTION_NEXT
        }
        val pendingIntent = PendingIntent.getService(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        return NotificationCompat.Action(
            android.R.drawable.ic_media_next,
            "Next",
            pendingIntent
        )
    }
    
    /**
     * Cập nhật notification hiện tại
     */
    fun updateNotification(notification: Notification) {
        notificationManager.notify(AudioServiceConstants.NOTIFICATION_ID, notification)
    }
    
    /**
     * Hủy notification
     */
    fun cancelNotification() {
        notificationManager.cancel(AudioServiceConstants.NOTIFICATION_ID)
    }
}
