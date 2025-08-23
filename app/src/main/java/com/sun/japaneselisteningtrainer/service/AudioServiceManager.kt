package com.sun.japaneselisteningtrainer.service

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
import com.sun.japaneselisteningtrainer.data.model.Audio
import com.sun.japaneselisteningtrainer.data.repository.AudioRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlin.time.Duration.Companion.seconds

/**
 * Manager class để tương tác với AudioService
 * Cung cấp API dễ sử dụng cho UI components
 * Integrated với AudioRepository cho database operations
 */
class AudioServiceManager(
    private val context: Context,
    private val audioRepository: AudioRepository
) : ServiceConnection {

    companion object {
        private const val TAG = "AudioServiceManager"
    }

    private var audioService: AudioService? = null
    private var managerScope: CoroutineScope? = null
    private var lastPlayedAudioId: Int? = null // Track last played audio để tránh duplicate increment

    // Service connection state
    private val _isServiceConnected = MutableStateFlow(false)
    val isServiceConnected: StateFlow<Boolean> = _isServiceConnected.asStateFlow()

    // Playback states - synced from AudioService
    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentPosition = MutableStateFlow(0L)
    val currentPosition: StateFlow<Long> = _currentPosition.asStateFlow()

    private val _duration = MutableStateFlow(0L)
    val duration: StateFlow<Long> = _duration.asStateFlow()

    private val _currentAudio = MutableStateFlow<Audio?>(null)
    val currentAudio: StateFlow<Audio?> = _currentAudio.asStateFlow()

    fun bindToService() {
        val intent = Intent(context, AudioService::class.java)
        context.bindService(intent, this, Context.BIND_AUTO_CREATE)
    }

    fun unbindFromService() {
        stopFlowCollection()
        try {
            context.unbindService(this)
        } catch (e: IllegalArgumentException) {
            Log.w(TAG, "Service was not bound: ${e.message}")
        }
        audioService = null
        _isServiceConnected.value = false
    }

    /**
     * Start flow collection from AudioService
     */
    private fun startFlowCollection(service: AudioService) {
        managerScope = CoroutineScope(Job() + Dispatchers.Main)
        managerScope?.launch {
            launch {
                service.isPlaying.collectLatest { _isPlaying.value = it }
            }
            launch {
                service.currentPosition.collectLatest { _currentPosition.value = it }
            }
            launch {
                service.duration.collectLatest { _duration.value = it }
            }
            launch {
                service.currentAudio.collectLatest { _currentAudio.value = it }
            }
        }
    }

    /**
     * Stop flow collection
     */
    private fun stopFlowCollection() {
        managerScope?.cancel()
        managerScope = null
    }

    // ServiceConnection callbacks
    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        val binder = service as AudioService.AudioServiceBinder
        audioService = binder.getService()
        _isServiceConnected.value = true
        
        // Start collecting states from service
        audioService?.let { startFlowCollection(it) }
    }

    override fun onServiceDisconnected(name: ComponentName?) {
        Log.d(TAG, "Service disconnected")
        stopFlowCollection()
        audioService = null
        _isServiceConnected.value = false
    }

    // ============ PLAYBACK CONTROL METHODS ============

    fun playAudio(audioId: Int, withFolder: Boolean = false) {
        audioService?.playAudio(audioId, withFolder)
    }

    fun playPlaylist(playlistId: Int, startIndex: Int = 0) {
        audioService?.playPlaylist(playlistId, startIndex)
    }

    fun togglePlayPause() {
        audioService?.togglePlayPause()
    }

    fun seekTo(position: Long) {
        audioService?.seekTo(position)
    }

    fun nextTrack() {
        audioService?.nextTrack()
    }

    fun previousTrack() {
        audioService?.previousTrack()
    }

    fun toggleShuffle() {
        audioService?.toggleShuffle()
    }

    fun isShuffleEnabled(): Boolean {
        return audioService?.isShuffleEnabled() ?: false
    }

    fun getProgress(): Float {
        val dur = _duration.value
        val pos = _currentPosition.value
        return if (dur > 0) pos.toFloat() / dur else 0f
    }

    // ============ DATABASE OPERATIONS ============

    /**
     * Load audio from database and play
     */
    suspend fun loadAndPlayAudio(audioId: Int, forceReload: Boolean = false) = withContext(Dispatchers.IO) {
        playAudio(audioId)
        if (forceReload || lastPlayedAudioId != audioId) {
            incrementListenTimes(audioId)
            lastPlayedAudioId = audioId
        }
    }


    /**
     * Toggle favorite status and update database
     */
    suspend fun toggleFavoriteStatus(audioId: Int) {
        try {
            val audio = audioRepository.getAudioStream(audioId).first() ?: throw Exception("Not found audio")
            val updatedAudio = audio.copy(isFavorite = !audio.isFavorite)
            audioRepository.update(updatedAudio)
            
            // Update currentAudio state để UI sync
            if (_currentAudio.value?.id == audio.id) {
                _currentAudio.value = updatedAudio
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to toggle favorite: ${e.message}")
            throw e
        }
    }

    /**
     * Increment listen times and update database
     */
    private suspend fun incrementListenTimes(audioId: Int) {
        try {
            val audio = audioRepository.getAudioStream(audioId).first() ?: throw Exception("Not found audio id")
            val updatedAudio = audio.copy(listenTimes = audio.listenTimes + 1)
            audioRepository.update(updatedAudio)
        } catch (e: Exception) {
            throw e
        }
    }

    fun playPause(audioId: Int) {
        if (audioId == currentAudio.value?.id) {
            togglePlayPause()
        } else {
            managerScope?.launch {
                loadAndPlayAudio(audioId)
            }
        }
    }
}
