package com.sun.japaneselisteningtrainer.ui.audio.player

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sun.japaneselisteningtrainer.R
import com.sun.japaneselisteningtrainer.TrainerTopAppBar
import com.sun.japaneselisteningtrainer.data.model.Audio
import com.sun.japaneselisteningtrainer.ui.AppViewModelProvider
import com.sun.japaneselisteningtrainer.ui.audio.player.components.AudioProgressBar
import com.sun.japaneselisteningtrainer.ui.audio.player.components.EditAudioButton
import com.sun.japaneselisteningtrainer.ui.audio.player.components.LyricsBox
import com.sun.japaneselisteningtrainer.ui.audio.player.components.RotatingDisc
import com.sun.japaneselisteningtrainer.ui.audio.player.components.TranscriptContainer
import com.sun.japaneselisteningtrainer.ui.audio.player.components.TransportBar
import com.sun.japaneselisteningtrainer.ui.navigation.NavigationDestination
import com.sun.japaneselisteningtrainer.ui.theme.JapaneseListeningTrainerTheme

object MusicPlayerDestination : NavigationDestination {
    override val route = "audio_player"
    override val titleRes = R.string.audio_player_title
    const val audioIdArg = "audioId"
    val routeWithArgs = "$route/{$audioIdArg}"

    fun createRoute(id: Int) = "$route/$id"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MusicPlayerScreen(
    modifier: Modifier = Modifier,
    audioId: Int = 1, // ID của audio cần phát
    onNavigationBack: () -> Unit,
    onEditAudio: () -> Unit,
    musicPlayerViewModel: MusicPlayerViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val scope = rememberCoroutineScope()

    // Observe ViewModel states
    val isServiceConnected by musicPlayerViewModel.isServiceConnected.collectAsState()
    val isPlaying by musicPlayerViewModel.isPlaying.collectAsState()
    val currentPosition by musicPlayerViewModel.currentPosition.collectAsState()
    val duration by musicPlayerViewModel.duration.collectAsState()
    val currentAudio by musicPlayerViewModel.currentAudioWithOverride.collectAsState()
    val isLoadingAudio by musicPlayerViewModel.isLoadingAudio.collectAsState()
    val audioLoadError by musicPlayerViewModel.audioLoadError.collectAsState()

    // Service đã được bind trong MainActivity.onCreate()
    // Không cần bind lại ở đây

    // Load audio from database only khi cần thiết
    LaunchedEffect(audioId, isServiceConnected) {
        if (isServiceConnected) {
            val currentAudio = musicPlayerViewModel.currentAudio.value

            // Chỉ load audio mới khi:
            // 1. Không có audio nào đang được load
            // 2. AudioId khác với audio hiện tại (thực sự là audio mới)
            if (currentAudio == null || currentAudio.id != audioId) {
                musicPlayerViewModel.loadAndPlayAudio(audioId)
            }
            // Nếu cùng audio đang phát, không làm gì (để nhạc tiếp tục phát)
        }
    }


    // NOTE: Không unbind service khi back để nhạc tiếp tục phát cho mini player
    // Service sẽ được unbind trong ViewModel.onCleared() hoặc khi app tắt hoàn toàn
    Scaffold(
        topBar = {
            TrainerTopAppBar(
                title = stringResource(MusicPlayerDestination.titleRes),
                canNavigateBack = true,
                navigateUp = onNavigationBack,
                actions = {
                    EditAudioButton(onClick = onEditAudio)
                }
            )
        }
    ) { inner ->
        // Container nhận swipe để toggle transcript
        if (isLoadingAudio) {
            // Loading state
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CircularProgressIndicator()
                    Text("Đang tải audio...")
                }
            }
        } else if (audioLoadError != null) {
            // Error state
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = audioLoadError!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Button(
                        onClick = {
                            musicPlayerViewModel.loadAndPlayAudio(audioId)
                        }
                    ) {
                        Text("Thử lại")
                    }
                }
            }
        } else {
            // Success state với real data - sử dụng currentAudio nếu có
            val audio = currentAudio
            if (audio != null) {
                PlayerContainer(
                    musicPlayerViewModel = musicPlayerViewModel,
                    isPlaying = isPlaying,
                    currentPosition = currentPosition,
                    duration = duration,
                    currentAudio = audio,
                    audioId = audioId,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(inner)
                )
            } else {
                // No audio found state
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Không tìm thấy audio với ID: $audioId",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Composable
fun PlayerContainer(
    musicPlayerViewModel: MusicPlayerViewModel,
    isPlaying: Boolean,
    currentPosition: Long,
    duration: Long,
    currentAudio: Audio,
    audioId: Int,
    modifier: Modifier = Modifier
) {
    var isTranscriptVisible by remember { mutableStateOf(false) }
    TranscriptContainer(
        visible = true,
        onToggle = { isTranscriptVisible = !isTranscriptVisible },
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(dimensionResource(R.dimen.dp_16))
                .animateContentSize(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioNoBouncy,
                        stiffness = Spring.StiffnessMedium
                    )
                ),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            if (!isTranscriptVisible) {
                RotatingDiscBox(
                    modifier = Modifier
                        .padding(30.dp, 0.dp, 30.dp, 30.dp)
                        .fillMaxWidth(),
                    isPlaying = isPlaying,
                    onDoubleClick = {
                        // Double-tap để restart audio từ đầu (force reload)
                        musicPlayerViewModel.forceReloadAudio(audioId)
                    }
                )
            } else {
                LyricView(
                    audio = currentAudio,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                )
            }
            AudioTitle(
                modifier = Modifier.padding(vertical = dimensionResource(R.dimen.dp_8)),
                title = currentAudio.title
            )
            AudioController(
                musicPlayerViewModel = musicPlayerViewModel,
                isPlaying = isPlaying,
                currentPosition = currentPosition,
                duration = duration,
                currentAudio = currentAudio,
                modifier = Modifier
            )
        }
    }
}

@Composable
fun RotatingDiscBox(
    modifier: Modifier,
    isPlaying: Boolean,
    onDoubleClick: () -> Unit = {}
) {
    RotatingDisc(
        imageRes = R.drawable.cd,
        isPlaying = isPlaying,
        size = 280.dp
    )
}

@Composable
fun AudioTitle(
    modifier: Modifier = Modifier,
    title: String,
) {
    Text(
        modifier = modifier,
        text = title,
        color = MaterialTheme.colorScheme.onBackground,
        style = MaterialTheme.typography.displayMedium,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}

@Composable
fun AudioController(
    musicPlayerViewModel: MusicPlayerViewModel,
    isPlaying: Boolean,
    currentPosition: Long,
    duration: Long,
    currentAudio: Audio,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
    ) {
        AudioProgressBar(
            progress = musicPlayerViewModel.getProgress(),
            currentPosition = currentPosition,
            duration = duration,
            onSeek = { progress ->
                val newPosition = (duration * progress).toLong()
                musicPlayerViewModel.seekTo(newPosition)
            },
            onSeekFinished = { },
            modifier = Modifier.padding(horizontal = 8.dp)
        )
        Spacer(Modifier.height(8.dp))
        TransportBar(
            isPlaying = isPlaying,
            isShuffleOn = musicPlayerViewModel.isShuffleEnabled(),
            isFavorite = currentAudio.isFavorite,
            onToggleShuffle = { musicPlayerViewModel.toggleShuffle() },
            onPrevious = { musicPlayerViewModel.previousTrack() },
            onPlayPause = { musicPlayerViewModel.togglePlayPause() },
            onNext = { musicPlayerViewModel.nextTrack() },
            onToggleFavorite = {
                // Toggle favorite trong database
                musicPlayerViewModel.toggleFavoriteStatus(currentAudio)
            }
        )
    }
}

@Composable
fun LyricView(
    audio: Audio,
    modifier: Modifier = Modifier
) {
    // Split script thành lines
    val scriptLines = remember(audio.script) {
        audio.script.split("\n").filter { it.isNotBlank() }
    }

    LyricsBox(
        lines = audio.script,
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
private fun AudioPlayerPreview() {
    JapaneseListeningTrainerTheme {
        MusicPlayerScreen(
            audioId = 1,
            onEditAudio = {},
            modifier = Modifier,
            onNavigationBack = { },
        )
    }
}
