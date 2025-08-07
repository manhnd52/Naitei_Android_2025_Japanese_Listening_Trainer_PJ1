package com.sun.japaneselisteningtrainer.ui.audio.entry

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.sun.japaneselisteningtrainer.data.model.Audio
import com.sun.japaneselisteningtrainer.data.repository.AudioRepository


class AudioEntryViewModel (
    private val audioRepository: AudioRepository
) : ViewModel() {
    val audioTitle = mutableStateOf("")

    suspend fun addAudio() {
        audioRepository.add(Audio(1, audioTitle.value))
    }
}
