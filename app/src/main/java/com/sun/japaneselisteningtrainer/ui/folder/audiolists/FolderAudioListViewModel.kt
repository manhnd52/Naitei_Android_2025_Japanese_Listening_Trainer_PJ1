package com.sun.japaneselisteningtrainer.ui.folder.audiolists

import android.util.Log
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sun.japaneselisteningtrainer.data.folder.FolderRepository
import com.sun.japaneselisteningtrainer.data.model.Folder
import com.sun.japaneselisteningtrainer.data.repository.AudioRepository
import com.sun.japaneselisteningtrainer.service.AudioServiceManager
import com.sun.japaneselisteningtrainer.ui.folder.components.AudioItemInfo
import com.sun.japaneselisteningtrainer.ui.folder.components.toAudioItemInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class FolderAudioListViewModel(
    savedStateHandle: SavedStateHandle,
    private val folderRepository: FolderRepository,
    private val audioRepository: AudioRepository,
    private val audioServiceManager: AudioServiceManager
) : ViewModel() {
    val folderId: Int = checkNotNull(savedStateHandle[FolderAudioListDestination.folderIdArg])

    private val _uiState = MutableStateFlow(
        FolderAudioListUiState(folder = Folder())
    )

    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            folderRepository.getFolderStream(folderId)
                .filterNotNull()
                .collect {
                    _uiState.update { current ->
                        current.copy(folder = it)
                    }
                }
        }

        viewModelScope.launch {
            audioRepository.getFolderAudiosStream(folderId).map {
                it.map { audio ->
                    audio.toAudioItemInfo()
                }
            }.collect { audioItemInfoList ->
                _uiState.update {
                    it.copy(audioItemInfoList = audioItemInfoList)
                }
            }
        }

        // Fetch ui state from service
        viewModelScope.launch {
            audioServiceManager.isPlaying.collect {
                updateUiState(uiState.value.copy(isPlaying = it))
            }
        }

        viewModelScope.launch {
            audioServiceManager.currentAudio.filterNotNull().map { it.id }.collect { id ->
                updateUiState(uiState.value.copy(playingAudioId = id))
            }
        }
    }

    private fun updateUiState(newUiState: FolderAudioListUiState) {
        _uiState.update { newUiState }
    }

    fun playPause(audioId: Int) {
        viewModelScope.launch {
            audioServiceManager.playPause(audioId)
        }
    }

    fun playAll() {
        viewModelScope.launch {
            audioServiceManager.playPlaylist(uiState.value.folder.id)
        }
    }

    suspend fun favorite(audioId: Int) {
        audioServiceManager.toggleFavoriteStatus(audioId)
    }
}

data class FolderAudioListUiState(
    val folder: Folder,
    val audioItemInfoList: List<AudioItemInfo> = listOf(),
    val playingAudioId: Int = -1,
    val isPlaying: Boolean = false,
)
