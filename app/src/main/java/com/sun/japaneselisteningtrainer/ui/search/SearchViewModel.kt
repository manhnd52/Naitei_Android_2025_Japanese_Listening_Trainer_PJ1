package com.sun.japaneselisteningtrainer.ui.search

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sun.japaneselisteningtrainer.data.model.Audio
import com.sun.japaneselisteningtrainer.data.repository.AudioRepository
import com.sun.japaneselisteningtrainer.service.AudioServiceManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import androidx.core.net.toUri
import com.sun.japaneselisteningtrainer.data.folder.FolderRepository
import kotlinx.coroutines.flow.first


data class SearchUiState(
    val query: String = "",
    val audioList: List<Audio> = emptyList(),
) {
    val notFound: Boolean
        get() = audioList.isEmpty() && query.isNotBlank()
    val begin: Boolean
        get() = query.isBlank()
}

class SearchViewModel(
    private val audioRepository: AudioRepository,
    private val folderRepository: FolderRepository,
    private val audioServiceManager: AudioServiceManager
) : ViewModel() {

    private val _query = MutableStateFlow("")

    val uiState: StateFlow<SearchUiState> = combine(
        audioRepository.getAllAudioStream(),
        _query
    ) { audioList, query ->
        val filtered = if (query.isBlank()) emptyList() else audioList.filter {
            it.title.contains(query, ignoreCase = true)
        }
        SearchUiState(query = query, audioList = filtered)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
        initialValue = SearchUiState()
    )

    fun onQueryChange(newQuery: String) {
        _query.value = newQuery
    }

    fun toggleFavorite(audioId: Int) {
        viewModelScope.launch {
            audioServiceManager.toggleFavoriteStatus(audioId)
        }
    }

    companion object {
        private const val TIMEOUT_MILLIS = 5_000L
    }
}

