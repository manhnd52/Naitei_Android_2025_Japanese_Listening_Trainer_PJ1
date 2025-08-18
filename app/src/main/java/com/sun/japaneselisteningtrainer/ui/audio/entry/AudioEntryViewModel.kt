package com.sun.japaneselisteningtrainer.ui.audio.entry

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.sun.japaneselisteningtrainer.data.model.Audio
import com.sun.japaneselisteningtrainer.data.repository.AudioRepository


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
            title.isNotBlank() && script.isNotBlank() && selectedFileUri != Uri.EMPTY
        }
    }

    suspend fun addAudio() {
        if (validateInput(uiState.audioForm)) {
            audioRepository.add(audio = uiState.audioForm.toAudio(), source = uiState.audioForm.selectedFileUri)
        }
    }
}

data class AudioEntryUiState(
    val audioForm: AudioForm = AudioForm(),
    val isValid: Boolean = false
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

