package com.sun.japaneselisteningtrainer.ui.audio.player

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sun.japaneselisteningtrainer.R

/* ------------------------------ Top app bar ------------------------------- */

@Composable
fun PlayerTopBar(
    titleChip: String,
    onBack: () -> Unit,
    onOpenPicker: () -> Unit,
    onEditAudio: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
        }
        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.Center
        ) {
            AssistChip(
                onClick = onOpenPicker,
                label = { Text(titleChip) },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = Color(0x1428A8FF),
                    labelColor = Color.White
                ),
            )
        }
        EditAudioButton(onClick = onEditAudio)
    }
}
@Composable
fun RotatingDisc(
    imageRes: Int,
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
    size: Dp = 200.dp,
    borderColor: Color = Color.White
) {
    val infinite = rememberInfiniteTransition(label = "disc")
    val angle by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 6000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "angle"
    )

    Image(
        painter = painterResource(imageRes),
        contentDescription = "Album art",
        contentScale = ContentScale.Crop,
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .border(2.dp, borderColor, CircleShape)
            .rotate(if (isPlaying) angle else 0f)
    )
}
@Composable
fun AudioProgressBar(
    progress: Float,                 // 0f..1f
    currentLabel: String,            // "mm:ss"
    totalLabel: String,              // "mm:ss"
    onSeek: (Float) -> Unit,
    modifier: Modifier = Modifier,
    onSeekFinished: () -> Unit = {}
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Slider(
            value = progress.coerceIn(0f, 1f),
            onValueChange = onSeek,
            onValueChangeFinished = onSeekFinished,
            modifier = Modifier
                .fillMaxWidth()
                .height(24.dp),
            colors = SliderDefaults.colors(
                thumbColor = Color.White,
                activeTrackColor = Color.White,
                inactiveTrackColor = Color.White.copy(alpha = 0.22f)
            )
        )
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(currentLabel, color = Color.White, fontSize = 12.sp)
            Text(totalLabel, color = Color.White, fontSize = 12.sp)
        }
    }
}
@Composable
fun AudioProgressBarMs(
    positionMs: Long,
    durationMs: Long,
    onSeekChange: (Long) -> Unit,
    onSeekFinished: () -> Unit,
    modifier: Modifier = Modifier,
    showTimeLabels: Boolean = true
) {
    val progress = if (durationMs > 0) positionMs.toFloat() / durationMs else 0f
    Column(modifier = modifier.fillMaxWidth()) {
        Slider(
            value = progress.coerceIn(0f, 1f),
            onValueChange = { frac -> onSeekChange((frac * durationMs).toLong()) },
            onValueChangeFinished = onSeekFinished,
            modifier = Modifier
                .fillMaxWidth()
                .height(24.dp),
            colors = SliderDefaults.colors(
                thumbColor = Color.White,
                activeTrackColor = Color.White,
                inactiveTrackColor = Color.White.copy(alpha = 0.22f)
            )
        )
        if (showTimeLabels) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(formatMs(positionMs), color = Color.White, fontSize = 12.sp)
                Text(formatMs(durationMs), color = Color.White, fontSize = 12.sp)
            }
        }
    }
}

private fun formatMs(ms: Long): String {
    val sec = (ms / 1000).toInt()
    val m = sec / 60
    val s = sec % 60
    return "%02d:%02d".format(m, s)
}

@Composable
fun RandomToggleButton(
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    IconButton(onClick = onClick, modifier = modifier) {
        Icon(
            Icons.Default.Shuffle,
            contentDescription = "Shuffle",
            tint = if (enabled) Color(0xFF7DD3FC) else Color.White
        )
    }
}

@Composable
fun FavoriteToggleButton(
    isFavorite: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    IconButton(onClick = onClick, modifier = modifier) {
        Icon(
            imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
            contentDescription = "Favorite",
            tint = if (isFavorite) Color(0xFFFF6B6B) else Color.White
        )
    }
}

@Composable
fun EditAudioButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    IconButton(onClick = onClick, modifier = modifier) {
        Icon(Icons.Default.Edit, contentDescription = "Edit audio", tint = Color.White)
    }
}

/* --------------------------- Playback controls core ----------------------- */

@Composable
fun PlaybackControls(
    isPlaying: Boolean,
    onPrevious: () -> Unit,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        IconButton(onClick = onPrevious) {
            Icon(Icons.Default.SkipPrevious, contentDescription = "Previous", tint = Color.White)
        }
        IconButton(onClick = onPlayPause) {
            Icon(
                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = if (isPlaying) "Pause" else "Play",
                tint = Color.White,
                modifier = Modifier.size(44.dp)
            )
        }
        IconButton(onClick = onNext) {
            Icon(Icons.Default.SkipNext, contentDescription = "Next", tint = Color.White)
        }
    }
}
@Composable
fun TransportBar(
    isPlaying: Boolean,
    isShuffleOn: Boolean,
    isFavorite: Boolean,
    onToggleShuffle: () -> Unit,
    onPrevious: () -> Unit,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onToggleFavorite: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        RandomToggleButton(enabled = isShuffleOn, onClick = onToggleShuffle)
        PlaybackControls(
            isPlaying = isPlaying,
            onPrevious = onPrevious,
            onPlayPause = onPlayPause,
            onNext = onNext
        )
        FavoriteToggleButton(isFavorite = isFavorite, onClick = onToggleFavorite)
    }
}
@Deprecated("Use TransportBar instead")
@Composable
fun TransportControls(
    isPlaying: Boolean,
    isShuffleOn: Boolean,
    isFavorite: Boolean,
    onToggleShuffle: () -> Unit,
    onPrevious: () -> Unit,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onToggleFavorite: () -> Unit,
    modifier: Modifier = Modifier
) {
    TransportBar(
        isPlaying = isPlaying,
        isShuffleOn = isShuffleOn,
        isFavorite = isFavorite,
        onToggleShuffle = onToggleShuffle,
        onPrevious = onPrevious,
        onPlayPause = onPlayPause,
        onNext = onNext,
        onToggleFavorite = onToggleFavorite,
        modifier = modifier
    )
}

@Composable
fun MusicPlayerScreen(
    // state
    title: String,
    isPlaying: Boolean,
    progress: Float,                 // vẫn giữ để không vỡ preview cũ
    currentTimeLabel: String,
    totalTimeLabel: String,
    isShuffleOn: Boolean,
    isFavorite: Boolean,
    // callbacks
    onBack: () -> Unit,
    onOpenPicker: () -> Unit,
    onEditAudio: () -> Unit,
    onSeek: (Float) -> Unit,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onToggleShuffle: () -> Unit,
    onToggleFavorite: () -> Unit,
    onGoToLyrics: (() -> Unit)? = null,
    // style
    modifier: Modifier = Modifier,
    albumRes: Int = R.drawable.son_tung
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0D1B2A))
            .systemBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        PlayerTopBar(
            titleChip = "New",
            onBack = onBack,
            onOpenPicker = onOpenPicker,
            onEditAudio = onEditAudio
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(70.dp))
            RotatingDisc(albumRes, isPlaying, size = 265.dp)
            Spacer(Modifier.height(24.dp))

            Text(
                text = title,
                color = Color.White,
                fontSize = 22.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(Modifier.height(24.dp))
            AudioProgressBar(
                progress = progress,
                currentLabel = currentTimeLabel,
                totalLabel = totalTimeLabel,
                onSeek = onSeek,
                onSeekFinished = { /* no-op: VM có thể commit seek tại đây */ }
            )

            Spacer(Modifier.height(12.dp))
            TransportBar(
                isPlaying = isPlaying,
                isShuffleOn = isShuffleOn,
                isFavorite = isFavorite,
                onToggleShuffle = onToggleShuffle,
                onPrevious = onPrevious,
                onPlayPause = onPlayPause,
                onNext = onNext,
                onToggleFavorite = onToggleFavorite
            )

            Spacer(Modifier.height(8.dp))
            if (onGoToLyrics != null) {
                TextButton(onClick = onGoToLyrics) {
                    Icon(Icons.Default.MenuBook, contentDescription = "Go to lyrics", tint = Color.White)
                    Spacer(Modifier.width(8.dp))
                    Text("Đi đến bài nghe tiếng Nhật", color = Color.White)
                }
            }
        }

        Spacer(Modifier.height(8.dp))
    }
}

/* -------------------------------- Preview --------------------------------- */

@Preview(
    showBackground = true,
    backgroundColor = 0xFF0D1B2A,
    widthDp = 360,
    heightDp = 780
)
@Composable
private fun MusicPlayerPreview() {
    MusicPlayerScreen(
        title = "耳から覚える日本語",
        isPlaying = true,
        progress = 0.08f,
        currentTimeLabel = "00:10",
        totalTimeLabel = "03:44",
        isShuffleOn = true,
        isFavorite = false,
        onBack = {},
        onOpenPicker = {},
        onEditAudio = {},
        onSeek = {},
        onPlayPause = {},
        onNext = {},
        onPrevious = {},
        onToggleShuffle = {},
        onToggleFavorite = {},
        onGoToLyrics = {}
    )
}
