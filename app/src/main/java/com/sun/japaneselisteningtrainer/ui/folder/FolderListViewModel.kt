package com.sun.japaneselisteningtrainer.ui.folder

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sun.japaneselisteningtrainer.data.folder.FolderRepository
import com.sun.japaneselisteningtrainer.data.model.Folder
import com.sun.japaneselisteningtrainer.ui.home.HomeUiState
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class FolderListViewModel (private val folderRepository: FolderRepository) : ViewModel() {
    val homeUiState: StateFlow<FolderListUiState> =
        folderRepository.getAllFolderStream().map { FolderListUiState(it) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
                initialValue = FolderListUiState()
            )

    var createFolderUiState by mutableStateOf(CreateFolderUiState())
        private set

    fun updateUiState(title: String, description: String) {
        createFolderUiState =
            CreateFolderUiState(title = title, description = description, isEntryValid = validateTitleInput(title))
    }

    private fun validateTitleInput(title: String = createFolderUiState.title): Boolean {
        if (title.isBlank()) return false
        var isTitleExists = false
        viewModelScope.launch {
            isTitleExists = isFolderTitleExists(title)
        }
        return !isTitleExists
    }

    private suspend fun isFolderTitleExists(title: String): Boolean {
        val folder = folderRepository.getFolderStream(title).first()
        return folder != null
    }

    fun createFolder() {
        viewModelScope.launch {
            if (validateTitleInput()) {
                val folder = Folder(name = createFolderUiState.title, description = createFolderUiState.description)
                folderRepository.add(folder)
            }
        }
    }

    companion object {
        private const val TIMEOUT_MILLIS = 5_000L
    }
}

data class FolderListUiState(
    val folderList: List<Folder> = listOf(),
)

data class CreateFolderUiState(
    val title: String = "",
    val description: String = "",
    val isEntryValid: Boolean = false
)

