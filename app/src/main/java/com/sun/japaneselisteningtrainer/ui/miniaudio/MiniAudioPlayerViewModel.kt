package com.sun.japaneselisteningtrainer.ui.miniaudio

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sun.japaneselisteningtrainer.data.model.Audio
import com.sun.japaneselisteningtrainer.service.AudioServiceManager
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class MiniAudioPlayerUiState(
    val currentAudio: Audio? = null,
    val isPlaying: Boolean = false,
    val isShuffleOn: Boolean = false
)

class MiniAudioPlayerViewModel(
    private val audioServiceManager: AudioServiceManager
) : ViewModel() {

    val uiState: StateFlow<MiniAudioPlayerUiState> = combine(
        audioServiceManager.currentAudio,
        audioServiceManager.isPlaying
    ) { currentAudio, isPlaying ->
        MiniAudioPlayerUiState(
            currentAudio = currentAudio,
            isPlaying = isPlaying,
            isShuffleOn = audioServiceManager.isShuffleEnabled()
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = MiniAudioPlayerUiState()
    )

    fun playAudio(audio: Audio) {
        viewModelScope.launch {
            audioServiceManager.loadAndPlayAudio(audio.id)
        }
    }

    fun pauseAudio() {
        audioServiceManager.togglePlayPause()
    }

    fun playPrevious() {
        audioServiceManager.previousTrack()
    }

    fun playNext() {
        audioServiceManager.nextTrack()
    }

    fun toggleFavorite(audioId: Int) {
        viewModelScope.launch {
            audioServiceManager.toggleFavoriteStatus(audioId)
        }
    }

    fun toggleShuffle() {
        audioServiceManager.toggleShuffle()
    }
}