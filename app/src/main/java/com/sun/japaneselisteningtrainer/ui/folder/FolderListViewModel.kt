package com.sun.japaneselisteningtrainer.ui.folder

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sun.japaneselisteningtrainer.data.folder.FolderRepository
import com.sun.japaneselisteningtrainer.data.model.Folder
import com.sun.japaneselisteningtrainer.ui.folder.create.CreateFolderUiState
import com.sun.japaneselisteningtrainer.ui.home.HomeUiState
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Stack

class FolderListViewModel(private val folderRepository: FolderRepository) : ViewModel() {


    var folderListUiState by mutableStateOf(FolderListUiState())

    init {
        viewModelScope.launch {
            folderRepository.getAllFolderStream().collect { it ->
                folderListUiState = folderListUiState.copy(
                    folderList = it
                )
            }
        }
    }

    fun updateUiState(newUiState: FolderListUiState) {
        folderListUiState = newUiState
    }

    suspend fun deleteFolder(folderId: Int = folderListUiState.selectedFolder.id) {
        folderRepository.delete(folderId)
        Cache.reset()
    }

    fun openFolderMenu(folder: Folder) {
        folderListUiState = folderListUiState.copy(showMenu = true, selectedFolder = folder)
    }

    fun dismissMenu() {
        folderListUiState = folderListUiState.copy(showMenu = false)
    }

    fun deleteSelectedFolderFromUi() {
        Cache.folderListStack.push(folderListUiState.folderList)
        folderListUiState = folderListUiState.copy(
            folderList = folderListUiState.folderList.filter { it.id != folderListUiState.selectedFolder.id }
        )
    }

    fun restoreFolder() {
        folderListUiState = folderListUiState.copy(
            folderList = Cache.folderListStack.pop()
        )
        Cache.reset()
    }

    companion object {
        private const val TIMEOUT_MILLIS = 5_000L
    }
}

data class FolderListUiState(
    val folderList: List<Folder> = listOf(),
    val showMenu: Boolean = false,
    val selectedFolder: Folder = Folder()
)

object Cache {
    var folderListStack: Stack<List<Folder>> = Stack()
    fun reset() {
        folderListStack.empty()
    }
}
