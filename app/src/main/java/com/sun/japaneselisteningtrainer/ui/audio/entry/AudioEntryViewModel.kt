package com.sun.japaneselisteningtrainer.ui.audio.entry

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.sun.japaneselisteningtrainer.data.model.Audio
import com.sun.japaneselisteningtrainer.data.repository.AudioRepository
import kotlinx.coroutines.flow.first


class AudioEntryViewModel(
    private val audioRepository: AudioRepository
) : ViewModel() {
    var uiState by mutableStateOf(AudioEntryUiState())

    fun updateForm(audioForm: AudioForm) {
        uiState = uiState.copy(
            audioForm = audioForm,
            isValid = validateInput(audioForm)
        )
    }

    private fun validateInput(audioForm: AudioForm): Boolean {
        return with(audioForm) {
            title.isNotBlank() && script.isNotBlank() && translate.isNotBlank()
        }
    }

    suspend fun addAudio() {
        if (validateInput(uiState.audioForm)) {
            audioRepository.add(audio = uiState.audioForm.toAudio(), source = uiState.audioForm.selectedFileUri)
        }
    }
    
    suspend fun loadAudioForEdit(audioId: Int) {
        try {
            val audio = audioRepository.getAudioStream(audioId).first()
            audio?.let {
                uiState = uiState.copy(
                    audioForm = it.toAudioForm(),
                    isValid = true,
                    isEditMode = true,
                    editingAudioId = audioId
                )
            }
        } catch (e: Exception) {
            // Handle error loading audio
        }
    }
    
    suspend fun updateAudio() {
        if (validateInput(uiState.audioForm) && uiState.isEditMode) {
            // Lấy audio hiện tại từ database
            val currentAudio = audioRepository.getAudioStream(uiState.editingAudioId).first()
            if (currentAudio != null) {
                // Chỉ update những field được edit, preserve các field khác
                val updatedAudio = currentAudio.copy(
                    title = uiState.audioForm.title,
                    script = uiState.audioForm.script,
                    translate = uiState.audioForm.translate
                    // filePath, source, listenTimes, isFavorite, etc. được preserve
                )
                audioRepository.update(updatedAudio)
            }
        }
    }
}

data class AudioEntryUiState(
    val audioForm: AudioForm = AudioForm(),
    val isValid: Boolean = false,
    val isEditMode: Boolean = false,
    val editingAudioId: Int = 0
)

data class AudioForm(
    val title: String = "",
    val folderId: Int = 0,
    val script: String = "",
    val translate: String = "",
    val selectedFileUri: Uri = Uri.EMPTY
)

fun AudioForm.toAudio(): Audio = Audio(
    title = title,
    folderId = folderId,
    script = script,
    translate = translate
)

fun Audio.toAudioForm(): AudioForm = AudioForm(
    title = title,
    folderId = folderId,
    script = script,
    translate = translate,
    selectedFileUri = Uri.EMPTY // File URI không cần thiết cho edit mode
)

