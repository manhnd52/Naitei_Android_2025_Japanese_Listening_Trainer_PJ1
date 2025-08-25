package com.sun.japaneselisteningtrainer.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sun.japaneselisteningtrainer.data.model.Audio
import com.sun.japaneselisteningtrainer.service.AudioServiceManager
import com.sun.japaneselisteningtrainer.ui.AppViewModelProvider
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@Composable
fun MiniAudioPlayer(
    modifier: Modifier = Modifier,
    viewModel: MiniAudioPlayerViewModel = viewModel(factory = AppViewModelProvider.Factory),
    onClickPlayer: (audioId: Int) -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()
    uiState.currentAudio?.let { audio ->
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.secondaryContainer)
                .clickable { onClickPlayer(audio.id) }
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = audio.title,
                color = Color.Black,
                modifier = Modifier.weight(1f)
            )

            IconButton(onClick = viewModel::playPrevious ) {
                Icon(
                    imageVector = Icons.Default.SkipPrevious,
                    contentDescription = "Previous",
                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }

            IconButton(onClick = viewModel::pauseAudio) {
                Icon(
                    imageVector = if (uiState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (uiState.isPlaying) "Pause" else "Play",
                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }

            IconButton(onClick = viewModel::playNext) {
                Icon(
                    imageVector = Icons.Default.SkipNext,
                    contentDescription = "Next",
                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }

            IconButton(onClick = {viewModel.toggleFavorite()}) {
                Icon(
                    imageVector = if (audio.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Favorite",
                    tint = if (audio.isFavorite) Color.Red else MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
    }

}

data class MiniAudioPlayerUiState(
    val currentAudio: Audio? = null,
    val isPlaying: Boolean = false,
    val isShuffleOn: Boolean = false
)

class MiniAudioPlayerViewModel(
    private val audioServiceManager: AudioServiceManager
) : ViewModel() {

    val uiState: StateFlow<MiniAudioPlayerUiState> = combine(
        audioServiceManager.currentAudio,
        audioServiceManager.isPlaying
    ) { currentAudio, isPlaying ->
        MiniAudioPlayerUiState(
            currentAudio = currentAudio,
            isPlaying = isPlaying,
            isShuffleOn = audioServiceManager.isShuffleEnabled()
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = MiniAudioPlayerUiState()
    )

    fun playAudio(audio: Audio) {
        viewModelScope.launch {
            audioServiceManager.loadAndPlayAudio(audio.id)
        }
    }

    fun pauseAudio() {
        audioServiceManager.togglePlayPause()
    }

    fun playPrevious() {
        audioServiceManager.previousTrack()
    }

    fun playNext() {
        audioServiceManager.nextTrack()
    }

    fun toggleFavorite() {
        viewModelScope.launch {
            val audioId = uiState.value.currentAudio?.id ?: return@launch
            audioServiceManager.toggleFavoriteStatus(audioId)
        }
    }

    fun toggleShuffle() {
        audioServiceManager.toggleShuffle()
    }
}

