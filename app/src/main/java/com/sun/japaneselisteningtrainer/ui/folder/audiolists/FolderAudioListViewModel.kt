package com.sun.japaneselisteningtrainer.ui.folder.audiolists

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sun.japaneselisteningtrainer.data.folder.FolderRepository
import com.sun.japaneselisteningtrainer.data.model.Folder
import com.sun.japaneselisteningtrainer.data.repository.AudioRepository
import com.sun.japaneselisteningtrainer.ui.folder.components.AudioItemInfo
import com.sun.japaneselisteningtrainer.ui.folder.components.toAudioItemInfo
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class FolderAudioListViewModel(
    savedStateHandle: SavedStateHandle,
    private val folderRepository: FolderRepository,
    private val audioRepository: AudioRepository,
) : ViewModel() {
    val folderId: Int = checkNotNull(savedStateHandle[FolderAudioListDestination.folderIdArg])

    val uiState = mutableStateOf(FolderAudioListUiState(Folder()))

    init {
        viewModelScope.launch {
            folderRepository.getFolderStream(folderId)
                .onEach {
                    Log.d("MyTag", "folder: $it")
                }
                .filterNotNull()
                .collect {
                    uiState.value = uiState.value.copy(folder = it)
                }
        }

        viewModelScope.launch {
            audioRepository.getFolderAudiosStream(folderId).map {
                it.map { audio ->
                    audio.toAudioItemInfo()
                }
            }.collect { audioItemInfoList ->
                uiState.value = uiState.value.copy(audioItemInfoList = audioItemInfoList)
            }
        }
    }

    private fun updateUiState(newUiState: FolderAudioListUiState) {
        uiState.value = newUiState
    }

    fun playPause(audioId: Int) {
        updateUiState(uiState.value.copy(playingAudioId = audioId))
    }
}

data class FolderAudioListUiState(
    val folder: Folder,
    val audioItemInfoList: List<AudioItemInfo> = listOf(),
    val playingAudioId : Int = -1,
)
