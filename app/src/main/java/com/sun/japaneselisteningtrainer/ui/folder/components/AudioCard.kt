package com.sun.japaneselisteningtrainer.ui.folder.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.PauseCircle
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sun.japaneselisteningtrainer.R
import com.sun.japaneselisteningtrainer.data.model.Audio
import com.sun.japaneselisteningtrainer.ui.theme.JapaneseListeningTrainerTheme

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AudioItem(
    modifier: Modifier = Modifier,
    info: AudioItemInfo,
    canPlay: Boolean = true,
    isPlaying: Boolean = false,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onPlayPause: () -> Unit = { },
    onFavorite: () -> Unit,
) {
    Card(
        modifier = modifier
            .clip(MaterialTheme.shapes.medium)
            .requiredHeightIn(max = 72.dp)
            .combinedClickable(
                onLongClick = onLongClick,
                onClick = onClick,
            ),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent,
        ),
        shape = MaterialTheme.shapes.large
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(dimensionResource(R.dimen.dp_8)),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (canPlay) PlayPauseButton(
                isPlaying = isPlaying,
                onClick = onPlayPause,
            )
            Spacer(Modifier.width(6.dp))
            Column {
                Text(
                    text = info.title,
                    style = MaterialTheme.typography.displayMedium,
                )
                if (info.isNew) {
                    Text(
                        text = stringResource(R.string.New),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
            Spacer(Modifier.weight(1f))
            FavoriteIconButton(
                isFavorite = info.isFavorite,
                onClick = onFavorite
            )
        }
    }
}

@Composable
fun PlayPauseButton(
    modifier: Modifier = Modifier,
    isPlaying: Boolean,
    onClick: () -> Unit
) {
    IconButton(
        modifier = modifier,
        onClick = onClick
    ) {
        Icon(
            modifier = Modifier.size(37.dp),
            imageVector = if (isPlaying) Icons.Filled.PauseCircle else Icons.Filled.PlayCircle,
            contentDescription = null,
            tint = if (isPlaying) colorResource(R.color.pause_button_color) else MaterialTheme.colorScheme.primary
        )
    }
}
@Composable
fun FavoriteIconButton(
    isFavorite: Boolean,
    onClick: () -> Unit
) {
    IconButton(
        onClick = onClick
    ) {
        Icon(
            imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
            contentDescription = null,
            tint = if (isFavorite) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Preview
@Composable
fun AudioItemPreview() {
    JapaneseListeningTrainerTheme {
        AudioItem(
            modifier = Modifier.fillMaxWidth(),
            isPlaying = true,
            info = AudioItemInfo(
                id = 1,
                title = "Title",
                isFavorite = true,
                isNew = true,
                duration = "4:22"
            ),
            onClick = { },
            onLongClick = { },
            onFavorite = { },
            onPlayPause = { }
        )
    }
}

data class AudioItemInfo(
    val id: Int = 0,
    val title: String = "",
    val isFavorite: Boolean = false,
    val isNew: Boolean = true,
    val duration: String = "0:00"
)

fun Audio.toAudioItemInfo(): AudioItemInfo {
    return AudioItemInfo(
        id = id,
        title = title,
        isFavorite = isFavorite,
        isNew = (listenTimes == 0),
        duration = formatDuration,
    )
}
