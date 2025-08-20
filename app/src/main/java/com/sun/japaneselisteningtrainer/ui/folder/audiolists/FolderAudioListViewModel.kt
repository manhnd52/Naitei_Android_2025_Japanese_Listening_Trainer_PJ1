package com.sun.japaneselisteningtrainer.ui.folder.audiolists

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sun.japaneselisteningtrainer.data.folder.FolderRepository
import com.sun.japaneselisteningtrainer.data.model.Audio
import com.sun.japaneselisteningtrainer.data.model.Folder
import com.sun.japaneselisteningtrainer.data.repository.AudioRepository
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class FolderAudioListViewModel(
    savedStateHandle: SavedStateHandle,
    private val folderRepository: FolderRepository,
    private val audioRepository: AudioRepository,
) : ViewModel() {
    val folderId: Int = checkNotNull(savedStateHandle[FolderAudioListDestination.folderIdArg])

    val uiState = mutableStateOf(FolderAudioListUiState())

    init {
        viewModelScope.launch {
            folderRepository.getFolderStream(folderId)
                .onEach {
                    Log.d("MyTag", "folder: $it")
                }
                .collect {
                    uiState.value = uiState.value.copy(folder = it)
                }
        }

        viewModelScope.launch {
            audioRepository.getFolderAudiosStream(folderId).onEach {
                Log.d("MyTag", "audioList: $it")
            }.collect {
                uiState.value = uiState.value.copy(audioList = it)
            }
        }
    }

}

data class FolderAudioListUiState(
    val folder: Folder? = null,
    val audioList: List<Audio> = listOf()
)
