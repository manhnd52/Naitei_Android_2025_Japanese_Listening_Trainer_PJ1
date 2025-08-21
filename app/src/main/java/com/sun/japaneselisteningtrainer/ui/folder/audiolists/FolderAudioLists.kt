package com.sun.japaneselisteningtrainer.ui.folder.audiolists

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sun.japaneselisteningtrainer.R
import com.sun.japaneselisteningtrainer.TrainerTopAppBar
import com.sun.japaneselisteningtrainer.data.model.Folder
import com.sun.japaneselisteningtrainer.ui.AppViewModelProvider
import com.sun.japaneselisteningtrainer.ui.folder.AddButton
import com.sun.japaneselisteningtrainer.ui.folder.components.AudioItem
import com.sun.japaneselisteningtrainer.ui.folder.components.AudioItemInfo
import com.sun.japaneselisteningtrainer.ui.navigation.NavigationDestination
import com.sun.japaneselisteningtrainer.ui.theme.JapaneseListeningTrainerTheme

object FolderAudioListDestination : NavigationDestination {
    override val route: String = "folders"
    override val titleRes: Int = R.string.null_string
    const val folderIdArg = "folderId"
    val routeWithArgs = "$route/{$folderIdArg}"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FolderAudioListScreen(
    modifier: Modifier = Modifier,
    viewModel: FolderAudioListViewModel = viewModel(factory = AppViewModelProvider.Factory),
    navigateBar: @Composable () -> Unit,
    onNavigateUp: () -> Unit,
) {
    val uiState by viewModel.uiState
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    var createFolderRequired by remember { mutableStateOf(false) }
    val snackBarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TrainerTopAppBar(
                title = stringResource(FolderAudioListDestination.titleRes),
                canNavigateBack = true,
                navigateUp = { onNavigateUp() },
                actions = {
                    AddButton(
                        onClick = { createFolderRequired = true }
                    )
                },
            )
        },
        bottomBar = {
            navigateBar()
        },
        snackbarHost = {
            SnackbarHost(hostState = snackBarHostState)
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            FolderHeader(
                modifier = Modifier.background(MaterialTheme.colorScheme.surface),
                folder = uiState.folder
            )
            HorizontalDivider()
            AudioList(
                playingAudioId = uiState.playingAudioId,
                audioItemInfoList = uiState.audioItemInfoList,
                onPlayPause = { viewModel.playPause(it) }
                onFavorite = { viewModel.favorite(it) }
            )
        }
    }
}

@Composable
fun FolderHeader(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.surfaceContainerHighest,
    folder: Folder,
    onStudyClick: () -> Unit = {},
) {
    Surface(
        modifier = modifier.height(IntrinsicSize.Max),
        color = color
    ) {
        Row(modifier = Modifier.padding(16.dp)) {
            Column(modifier = Modifier) {
                Row(
                    verticalAlignment = Alignment.Bottom,
                ) {
                    Thumbnail()
                    Spacer(Modifier.width(10.dp))
                }
                Spacer(Modifier.height(20.dp))
                FolderTitle(
                    modifier = Modifier,
                    title = folder.name
                )
                FolderDescription(
                    description = folder.description
                )
            }
            Spacer(Modifier.weight(1f))
            Column(
                modifier = Modifier.fillMaxHeight(),
                verticalArrangement = Arrangement.Bottom) {
                StudyButton(
                    onClick = onStudyClick
                )
            }
        }

    }

}

@Composable
fun Thumbnail(
    modifier: Modifier = Modifier,
    painter: Painter = painterResource(R.drawable.mint_green_folder_with_blossom)
) {
    Box(
        modifier = modifier
    ) {
        Image(
            modifier = Modifier.size(120.dp).offset(y = 14.dp),
            painter = painter,
            contentDescription = "",
        )
    }

}

@Composable
fun StudyButton(modifier: Modifier = Modifier, onClick: () -> Unit) {
    Button(
        modifier = modifier,
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = colorResource(R.color.study_button_color)
        )
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Study", style = MaterialTheme.typography.bodyLarge)
        }
    }
}

@Composable
fun FolderTitle(
    modifier: Modifier = Modifier,
    title: String,
) {
    Box(modifier = Modifier) {
        Text(
            text = title,
            style = MaterialTheme.typography.displayLarge
        )
    }
}

@Composable
fun FolderDescription(
    modifier: Modifier = Modifier,
    description: String,
) {
    Box(modifier = Modifier) {
        Text(
            text = description,
            style = MaterialTheme.typography.displaySmall
        )
    }
}

@Preview(showBackground = true)
@Composable
fun FolderHeaderReview() {
    JapaneseListeningTrainerTheme {
        FolderHeader(
            folder = Folder(1, "Folder", "Description")
        )
    }
}

@Composable
fun AudioList(
    modifier: Modifier = Modifier,
    playingAudioId: Int,
    audioItemInfoList: List<AudioItemInfo>,
    onPlayPause: (audioId: Int) -> Unit,
    onFavorite: (audioId: Int) -> Unit,
) {
    LazyColumn(
        modifier = modifier
    ) {
        items(audioItemInfoList, key = { item -> item.id }) { item ->
            AudioItem(
                modifier = Modifier.padding(dimensionResource(R.dimen.dp_8)),
                isPlaying = item.id == playingAudioId,
                info = item,
                onClick = { /*TODO*/ },
                onLongClick = { /*TODO*/ },
                onFavorite = { /*TODO*/ },
                onPlayPause = { onPlayPause(item.id) }
            )
        }
    }
}
