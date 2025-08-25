package com.sun.japaneselisteningtrainer.ui.search

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.isTraversalGroup
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sun.japaneselisteningtrainer.R
import com.sun.japaneselisteningtrainer.TrainerTopAppBar
import com.sun.japaneselisteningtrainer.data.model.Audio
import com.sun.japaneselisteningtrainer.ui.AppViewModelProvider
import com.sun.japaneselisteningtrainer.ui.folder.components.AudioItem
import com.sun.japaneselisteningtrainer.ui.folder.components.toAudioItemInfo
import com.sun.japaneselisteningtrainer.ui.home.AudioCard
import com.sun.japaneselisteningtrainer.ui.navigation.NavigationDestination

object SearchDestination : NavigationDestination {
    override val route: String = "search"
    override val titleRes: Int = R.string.search_audio
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    modifier: Modifier = Modifier,
    viewModel: SearchViewModel = viewModel(factory = AppViewModelProvider.Factory),
    navigationBar: @Composable () -> Unit,
    navigateToMusicPlayer: (Int) -> Unit,
    onAudioLongClick: (Int) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TrainerTopAppBar(
                title = stringResource(SearchDestination.titleRes),
                canNavigateBack = false,
            )
        },
        bottomBar = { navigationBar() },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        val focusManager = LocalFocusManager.current

        Column(
            Modifier
                .padding(paddingValues)
                .padding(horizontal = 12.dp)
                .fillMaxSize()
                .semantics { isTraversalGroup = true }
        ) {
            TextField(
                value = uiState.query,
                onValueChange = viewModel::onQueryChange,
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large,
                placeholder = { Text(text = stringResource(R.string.audio_search_input)) },
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    disabledIndicatorColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    errorIndicatorColor = Color.Transparent,
                ),
                keyboardOptions = KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Search
                ),
                keyboardActions = KeyboardActions(
                    onSearch = { focusManager.clearFocus() }
                )
            )
            when {
                uiState.begin -> SearchBegin()
                uiState.notFound -> SearchNotFound()
                else -> Column(Modifier.verticalScroll(rememberScrollState())) {
                    uiState.audioList.forEach { audio ->
                        AudioSearchItem(
                            audio = audio,
                            navigateToMusicPlayer = { navigateToMusicPlayer(audio.id) },
                            onAudioLongClick = { onAudioLongClick(audio.id) },
                            onFavorite = { viewModel.toggleFavorite(audio.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AudioSearchItem(
    modifier: Modifier = Modifier,
    audio: Audio,
    navigateToMusicPlayer: () -> Unit,
    onAudioLongClick: () -> Unit,
    onFavorite: () -> Unit,
    enableHorizontalDivider: Boolean = true
) {
    AudioItem(
        info = audio.toAudioItemInfo(),
        canPlay = false,
        onClick = { navigateToMusicPlayer() },
        onLongClick = { onAudioLongClick() },
        onFavorite = { onFavorite() },
    )
    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
}

@Composable
fun SearchBegin(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(R.drawable.happy_search),
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth(0.3f)
                .widthIn(max = 500.dp)
        )
        Spacer(modifier = Modifier.padding(16.dp))
        Text(
            text = stringResource(R.string.begin_search_notice),
            style = MaterialTheme.typography.displaySmall,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

@Composable
fun SearchNotFound(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(R.drawable.sad_search),
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth(0.3f)
                .widthIn(max = 500.dp)
        )
        Spacer(modifier = Modifier.padding(16.dp))
        Text(
            text = stringResource(R.string.audio_not_found),
            style = MaterialTheme.typography.displaySmall,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}


