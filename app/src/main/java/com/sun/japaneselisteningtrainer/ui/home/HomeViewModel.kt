package com.sun.japaneselisteningtrainer.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sun.japaneselisteningtrainer.data.model.Audio
import com.sun.japaneselisteningtrainer.data.repository.AudioRepository
import com.sun.japaneselisteningtrainer.service.AudioServiceManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class AudioFilterType {
    ALL, FAVORITES, RECENT
}

data class HomeUiState(
    val audioList: List<Audio> = emptyList(),
    val currentAudio: Audio? = null,
    val isPlaying: Boolean = false,
    val isShuffleOn: Boolean = false,
    val filterType: AudioFilterType = AudioFilterType.ALL
)

class HomeViewModel(
    private val audioRepository: AudioRepository,
    private val audioServiceManager: AudioServiceManager
) : ViewModel() {

    private val _filterType = MutableStateFlow(AudioFilterType.ALL)

    val uiState: StateFlow<HomeUiState> = combine(
        audioRepository.getAllAudioStream(),
        _filterType,
        audioServiceManager.currentAudio,
        audioServiceManager.isPlaying
    ) { audioList, filterType, currentAudio, isPlaying ->
        val filteredList = when (filterType) {
            AudioFilterType.ALL -> audioList
            AudioFilterType.FAVORITES -> audioList.filter { it.isFavorite }
            AudioFilterType.RECENT -> audioList.sortedByDescending { it.createdAt }
        }

        HomeUiState(
            audioList = filteredList,
            currentAudio = currentAudio,
            isPlaying = isPlaying,
            isShuffleOn = audioServiceManager.isShuffleEnabled(),
            filterType = filterType
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
        initialValue = HomeUiState()
    )

    fun setFilter(type: AudioFilterType) {
        _filterType.value = type
    }

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

    companion object {
        private const val TIMEOUT_MILLIS = 5_000L
    }
}
