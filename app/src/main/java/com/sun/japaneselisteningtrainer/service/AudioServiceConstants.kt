package com.sun.japaneselisteningtrainer.service

/**
 * Constants cho Audio Service
 * Chứa các action, notification channel, và các constants khác
 */
object AudioServiceConstants {
    
    // Service Actions
    const val ACTION_PLAY = "com.sun.japaneselisteningtrainer.ACTION_PLAY"
    const val ACTION_PAUSE = "com.sun.japaneselisteningtrainer.ACTION_PAUSE"
    const val ACTION_NEXT = "com.sun.japaneselisteningtrainer.ACTION_NEXT"
    const val ACTION_PREVIOUS = "com.sun.japaneselisteningtrainer.ACTION_PREVIOUS"
    const val ACTION_STOP = "com.sun.japaneselisteningtrainer.ACTION_STOP"
    const val ACTION_SEEK = "com.sun.japaneselisteningtrainer.ACTION_SEEK"
    
    // Notification
    const val NOTIFICATION_CHANNEL_ID = "audio_playback_channel"
    const val NOTIFICATION_CHANNEL_NAME = "Audio Playback"
    const val NOTIFICATION_ID = 1001
    
    // Intent extras
    const val EXTRA_AUDIO_ID = "extra_audio_id"
    const val EXTRA_SEEK_POSITION = "extra_seek_position"
    
    // Playback states
    const val STATE_IDLE = 0
    const val STATE_PLAYING = 1
    const val STATE_PAUSED = 2
    const val STATE_STOPPED = 3
    const val STATE_ERROR = 4
}
