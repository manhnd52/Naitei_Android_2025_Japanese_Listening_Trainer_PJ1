package com.sun.japaneselisteningtrainer.service

import android.content.Context
import android.util.Log
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.sun.japaneselisteningtrainer.data.model.Audio

class AudioPlayer(private val context: Context) {

    companion object {
        private const val TAG = "AudioPlayer"
    }

    private val _exoPlayer = ExoPlayer.Builder(context).build()
    private val exoPlayer: ExoPlayer get() = _exoPlayer
    private val audioAttributes: AudioAttributes = AudioAttributes.Builder()
        .setUsage(C.USAGE_MEDIA)
        .setContentType(C.AUDIO_CONTENT_TYPE_SPEECH)
        .build()

    // Callback interface
    interface AudioPlayerCallback {
        fun onIsPlayingChanged(isPlaying: Boolean)
        fun onPositionChanged(position: Long, duration: Long)
        fun onAudioChanged(audio: Audio?)
        fun onError(error: String)
        fun onAudioCompleted()
    }

    private var callback: AudioPlayerCallback? = null

    init {
        setupPlayerListener()
        setupPlayerAttributes()
    }

    private fun setupPlayerAttributes() {
        exoPlayer.setAudioAttributes(audioAttributes, true)
    }

    private fun setupPlayerListener() {
        exoPlayer.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                updatePlaybackState(playbackState)
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                callback?.onIsPlayingChanged(isPlaying)
            }

            override fun onPositionDiscontinuity(
                oldPosition: Player.PositionInfo,
                newPosition: Player.PositionInfo,
                reason: Int
            ) {
                updatePosition()
            }

            override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                callback?.onError("Playback error: ${error.message}")
            }
        })
    }

    fun setCallback(callback: AudioPlayerCallback) {
        this.callback = callback
    }

    fun prepareAudio(audio: Audio) {
        try {
            val mediaItem = MediaItem.fromUri(audio.filePath)
            exoPlayer.setMediaItem(mediaItem)
            exoPlayer.prepare()
            callback?.onAudioChanged(audio)

        } catch (e: Exception) {
            callback?.onError("Không thể tải audio: ${e.message}")
        }
    }

    fun play() {
        exoPlayer.playWhenReady = true
        exoPlayer.play()
    }

    fun pause() {
        exoPlayer.pause()
    }

    fun stop() {
        exoPlayer.stop()
    }

    fun seekTo(position: Long) {
        exoPlayer.seekTo(position)
        updatePosition()
    }

    fun isCurrentlyPlaying(): Boolean = exoPlayer.isPlaying

    fun getCurrentPosition(): Long = exoPlayer.currentPosition

    fun getDuration(): Long = exoPlayer.duration.takeIf { it > 0 } ?: 0L

    private fun updatePlaybackState(state: Int) {
        when (state) {
            Player.STATE_IDLE -> {}
            Player.STATE_BUFFERING -> {
                // Giữ nguyên state hiện tại khi buffering
            }

            Player.STATE_READY -> {
                updatePosition()
            }

            Player.STATE_ENDED -> {
                callback?.onPositionChanged(0L, getDuration())
                callback?.onAudioCompleted()
            }
        }
    }

    private fun updatePosition() {
        val position = getCurrentPosition()
        val duration = getDuration()

        callback?.onPositionChanged(position, duration)
    }

    fun release() {
        exoPlayer.release()
        callback = null
    }
}
