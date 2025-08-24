package com.sun.japaneselisteningtrainer.ui.home

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sun.japaneselisteningtrainer.R
import com.sun.japaneselisteningtrainer.ui.AppViewModelProvider
import com.sun.japaneselisteningtrainer.ui.navigation.NavigationDestination

object HomeDestination : NavigationDestination {
    override val route = "home"
    override val titleRes = R.string.home_title
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    homeViewModel: HomeViewModel = viewModel(factory = AppViewModelProvider.Factory),
    navigationBar: @Composable () -> Unit,
    navigateToMusicPlayer: (Int) -> Unit,
    onAudioLongClick: (Int) -> Unit
) {
    val homeUiState by homeViewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    FilterBar(
                        currentFilter = homeUiState.filterType,
                        onFilterChange = { homeViewModel.setFilter(it) }
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            navigationBar()
        }
    ) { paddingValues ->
        LazyColumn(
            contentPadding = paddingValues,
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize()
        ) {
            items(homeUiState.audioList) { audio ->
                AudioCard(
                    title = audio.title,
                    imageRes = R.drawable.logo,
                    isFavorite = audio.isFavorite,
                    onClick = {
                        navigateToMusicPlayer(audio.id)
                    },
                    onLongClick = { onAudioLongClick(audio.id) },
                    onFavoriteClick = {
                        homeViewModel.toggleFavorite(audio.id)
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AudioCard(
    modifier: Modifier = Modifier,
    title: String,
    imageRes: Int,
    isFavorite: Boolean = false,
    onClick: () -> Unit,
    onLongClick: () -> Unit = {},
    onFavoriteClick: () -> Unit = {}
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(12.dp)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(id = imageRes),
            contentDescription = "Audio Logo",
            modifier = Modifier
                .size(48.dp)
                .padding(end = 12.dp),
            contentScale = ContentScale.Crop
        )

        Text(
            text = title,
            color = Color.Black,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyLarge
        )

        IconButton(onClick = onFavoriteClick) {
            Icon(
                imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                contentDescription = "Favorite",
                tint = if (isFavorite) Color.Red else MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun FilterBar(
    currentFilter: AudioFilterType,
    onFilterChange: (AudioFilterType) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        FilterButton(
            "All",
            currentFilter == AudioFilterType.ALL
        ) { onFilterChange(AudioFilterType.ALL) }
        FilterButton("Favorites", currentFilter == AudioFilterType.FAVORITES) {
            onFilterChange(AudioFilterType.FAVORITES)
        }
        FilterButton("Recent", currentFilter == AudioFilterType.RECENT) {
            onFilterChange(AudioFilterType.RECENT)
        }
    }
}

@Composable
fun FilterButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.secondary
            else MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Text(
            text = text,
            color = if (isSelected) MaterialTheme.colorScheme.onSecondary
            else Color.Black
        )
    }
}
