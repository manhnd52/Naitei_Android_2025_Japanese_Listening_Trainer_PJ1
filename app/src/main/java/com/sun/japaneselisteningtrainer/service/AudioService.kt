package com.sun.japaneselisteningtrainer.service

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.core.app.ServiceCompat
import com.sun.japaneselisteningtrainer.TrainerApplication
import com.sun.japaneselisteningtrainer.data.AppContainer
import com.sun.japaneselisteningtrainer.data.model.Audio
import com.sun.japaneselisteningtrainer.data.repository.AudioRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Foreground Service để phát audio
 * Sử dụng ExoPlayer và hiển thị notification
 */
class AudioService : Service(), AudioPlayer.AudioPlayerCallback {
    
    companion object {
        private const val TAG = "AudioService"
    }
    
    // Binder cho client kết nối
    inner class AudioServiceBinder : Binder() {
        fun getService(): AudioService = this@AudioService
    }
    private val binder = AudioServiceBinder()
    private lateinit var audioPlayer: AudioPlayer
    private lateinit var notificationManager: AudioNotificationManager
    private lateinit var audioRepository: AudioRepository
    
    // Coroutine scope cho service
    private val serviceScope = CoroutineScope(Dispatchers.Main + Job())
    private var positionUpdateJob: Job? = null
    
    // State flows để UI có thể observe
    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()
    
    private val _currentPosition = MutableStateFlow(0L)
    val currentPosition: StateFlow<Long> = _currentPosition.asStateFlow()
    
    private val _duration = MutableStateFlow(0L) 
    val duration: StateFlow<Long> = _duration.asStateFlow()
    
    private val _currentAudioId = MutableStateFlow<Int?>(null)
    @OptIn(ExperimentalCoroutinesApi::class)
    val currentAudio: StateFlow<Audio?> = _currentAudioId.flatMapLatest {id ->
        if (id == null) {
            flowOf(null)
        } else {
            audioRepository.getAudioStream(id)
        }
    }.stateIn(
        scope = serviceScope,
        started = kotlinx.coroutines.flow.SharingStarted.Eagerly,
        initialValue = null
    )
    private val lastedAudioIdStack = ArrayDeque<Int>()

    // Playlist management
    private var _playlistId = MutableStateFlow<Int?>(null)
    @OptIn(ExperimentalCoroutinesApi::class)
    private var playlist: StateFlow<List<Audio>> = _playlistId.flatMapLatest { id ->
        if (id == null) {
            audioRepository.getAllAudioStream()
        } else {
            audioRepository.getFolderAudiosStream(id)
        }
    }.stateIn(
        scope = serviceScope,
        started = kotlinx.coroutines.flow.SharingStarted.Eagerly,
        initialValue = emptyList()
    )

    private var isShuffleEnabled = false
    
    override fun onCreate() {
        super.onCreate()
        audioPlayer = AudioPlayer(this).apply {
            setCallback(this@AudioService)
        }
        notificationManager = AudioNotificationManager(this)
        val appContainer by lazy {
            (application as TrainerApplication).container
        }
        audioRepository = appContainer.audioRepository
        startCollectFlow()
    }

    private fun startCollectFlow() {
        serviceScope.launch {
            combine(
                currentAudio.filterNotNull(),
                isPlaying,
                currentPosition,
                duration
            ) { _, _, _, _ ->
            }.collect {
                updateNotification()
            }
        }

        serviceScope.launch {
            currentAudio.filterNotNull().collect {
                startForegroundService()
                currentAudio.value?.let { audioPlayer.prepareAudio(it) }
                audioPlayer.play()
                lastedAudioIdStack.addLast(it.id.also {id ->
                    Log.d("Mytag", "Last Audio ID: $id")
                })
            }
        }

    }

    override fun onBind(intent: Intent?): IBinder = binder
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        when (intent?.action) {
            AudioServiceConstants.ACTION_PLAY -> handlePlay()
            AudioServiceConstants.ACTION_PAUSE -> handlePause()
            AudioServiceConstants.ACTION_NEXT -> handleNext()
            AudioServiceConstants.ACTION_PREVIOUS -> handlePrevious()
            AudioServiceConstants.ACTION_STOP -> handleStop()
            AudioServiceConstants.ACTION_SEEK -> {
                val position = intent.getLongExtra(AudioServiceConstants.EXTRA_SEEK_POSITION, 0L)
                handleSeek(position)
            }
        }
        
        return START_STICKY
    }
    
    override fun onDestroy() {
        super.onDestroy()
        stopPositionUpdates()
        audioPlayer.release()
        notificationManager.cancelNotification()
        serviceScope.coroutineContext[Job]?.cancel()
    }
    
    // ===== AudioPlayer.AudioPlayerCallback Implementation =====
    
    override fun onIsPlayingChanged(isPlaying: Boolean) {
        _isPlaying.value = isPlaying
        
        if (isPlaying) {
            startPositionUpdates()
            startForegroundService()
        } else {
            stopPositionUpdates()
        }
    }


    override fun onPositionChanged(position: Long, duration: Long) {
        _currentPosition.value = position
        _duration.value = duration
    }
    
    override fun onAudioChanged(audio: Audio?) {
        _currentAudioId.value = audio?.id
    }
    
    override fun onError(error: String) {
        Log.e(TAG, "Audio error: $error")
    }
    
    override fun onAudioCompleted() {
        nextTrack()
    }
    
    // ===== Public API Methods =====

    fun playAudio(audioId: Int, withFolder: Boolean = false) {
        _currentAudioId.value = audioId
        if (withFolder) {
            _playlistId.value = audioId
        }
    }

    fun playPlaylist(playlistId : Int, startIndex: Int = 0) {
        _playlistId.value = playlistId
        _currentAudioId.update { playlist.value[startIndex].id }
    }

    fun togglePlayPause() {
        if (audioPlayer.isCurrentlyPlaying()) {
            audioPlayer.pause()
        } else {
            audioPlayer.play()
        }
    }
    
    /**
     * Seek to position
     */
    fun seekTo(position: Long) {
        audioPlayer.seekTo(position)
    }
    
    /**
     * Next track
     */
    fun nextTrack() {
        if (playlist.value.isNotEmpty()) {
            val currentIndex = playlist.value.indexOf(currentAudio.value)
            if (isShuffleEnabled) {
                val availableIndices = playlist.value.indices.filter { it != currentIndex }
                if (availableIndices.isNotEmpty()) {
                    val newIndex = availableIndices.random()
                    _currentAudioId.update { playlist.value[newIndex].id }
                }
            } else {
                val newIndex = (currentIndex + 1) % playlist.value.size
                _currentAudioId.update { playlist.value[newIndex].id }
                Log.d(TAG, "Next track: ${playlist.value[newIndex].title}")
            }
        }
    }
    
    /**
     * Previous track
     */
    fun previousTrack() {
        fun beforeTrack() {
            val currentIndex = playlist.value.indexOf(currentAudio.value)
            val newIndex = (currentIndex - 1 + playlist.value.size) % playlist.value.size
            _currentAudioId.update { playlist.value[newIndex].id }
        }
        beforeTrack()
    }

    fun toggleShuffle() {
        isShuffleEnabled = !isShuffleEnabled
    }

    fun isShuffleEnabled(): Boolean = isShuffleEnabled
    
    // ===== Private Helper Methods =====
    
    private fun handlePlay() {
        audioPlayer.play()
    }
    
    private fun handlePause() {
        audioPlayer.pause()
    }
    
    private fun handleNext() {
        nextTrack()
    }
    
    private fun handlePrevious() {
        previousTrack()
    }
    
    private fun handleStop() {
        audioPlayer.stop()
        stopSelf()
    }
    
    private fun handleSeek(position: Long) {
        audioPlayer.seekTo(position)
    }
    
    /**
     * Bắt đầu foreground service với notification
     */
    private fun startForegroundService() {
        val currentAudio = currentAudio.value ?: run {
            return
        }

        val notification = notificationManager.createNotification(
            audio = currentAudio,
            isPlaying = _isPlaying.value,
            position = _currentPosition.value,
            duration = _duration.value
        )
        
        try {
            startForeground(AudioServiceConstants.NOTIFICATION_ID, notification)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start foreground service: ${e.message}")
        }
    }

    private fun updateNotification() {
        val currentAudio = currentAudio.value ?: return
        
        val notification = notificationManager.createNotification(
            audio = currentAudio,
            isPlaying = _isPlaying.value,
            position = _currentPosition.value,
            duration = _duration.value
        )
        
        notificationManager.updateNotification(notification)
    }

    private fun startPositionUpdates() {
        stopPositionUpdates()
        
        positionUpdateJob = serviceScope.launch {
            while (isActive && audioPlayer.isCurrentlyPlaying()) {
                val position = audioPlayer.getCurrentPosition()
                val duration = audioPlayer.getDuration()
                
                _currentPosition.value = position
                _duration.value = duration
                
                delay(1000)
            }
        }
    }

    private fun stopPositionUpdates() {
        positionUpdateJob?.cancel()
        positionUpdateJob = null
    }
}
