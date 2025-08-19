package com.sun.japaneselisteningtrainer.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sun.japaneselisteningtrainer.data.model.Audio
import com.sun.japaneselisteningtrainer.data.repository.AudioRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

enum class AudioFilterType {
    ALL, FAVORITES, RECENT
}

data class HomeUiState(
    val audioList: List<Audio> = emptyList(),
    val currentAudio: Audio? = null,
    val isPlaying: Boolean = false,
    val filterType: AudioFilterType = AudioFilterType.ALL
)

class HomeViewModel(
    private val audioRepository: AudioRepository
) : ViewModel() {

    private val _filterType = MutableStateFlow(AudioFilterType.ALL)
    private val _currentAudio = MutableStateFlow<Audio?>(null)
    private val _isPlaying = MutableStateFlow(false)

    val uiState: StateFlow<HomeUiState> = combine(
        audioRepository.getAllAudioStream(),
        _filterType,
        _currentAudio,
        _isPlaying
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
        _currentAudio.value = audio
        _isPlaying.value = true
    }

    fun pauseAudio() {
        _isPlaying.value = false
    }

    fun stopAudio() {
        _currentAudio.value = null
        _isPlaying.value = false
    }

    fun playPrevious() {
        val list = uiState.value.audioList
        val current = _currentAudio.value ?: return
        val currentIndex = list.indexOf(current)
        if (currentIndex > 0) {
            playAudio(list[currentIndex - 1])
        }
    }

    fun playNext() {
        val list = uiState.value.audioList
        val current = _currentAudio.value ?: return
        val currentIndex = list.indexOf(current)
        if (currentIndex >= 0 && currentIndex < list.lastIndex) {
            playAudio(list[currentIndex + 1])
        }
    }

    fun toggleFavorite(audio: Audio) {
        // TODO: sau này cập nhật DB
        println("Toggle favorite for: ${audio.title}")
    }

    companion object {
        private const val TIMEOUT_MILLIS = 5_000L
    }
}