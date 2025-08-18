package com.sun.japaneselisteningtrainer.data.repository

import android.net.Uri
import com.sun.japaneselisteningtrainer.data.model.Audio
import kotlinx.coroutines.flow.Flow

interface AudioRepository {
    suspend fun add(audio: Audio, source: Uri) : Int
    suspend fun delete(audio: Audio)
    suspend fun update(audio: Audio)
    fun getAllAudioStream(): Flow<List<Audio>>
    fun getAudioStream(id: Int): Flow<Audio?>
}
