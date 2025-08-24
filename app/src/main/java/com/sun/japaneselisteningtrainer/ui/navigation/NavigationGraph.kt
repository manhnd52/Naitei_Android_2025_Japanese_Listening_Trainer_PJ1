/*
 * Copyright (C) 2023 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.sun.japaneselisteningtrainer.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.dialog
import androidx.navigation.navArgument
import com.sun.japaneselisteningtrainer.ui.AppViewModelProvider
import com.sun.japaneselisteningtrainer.ui.audio.entry.AudioEditDestination
import com.sun.japaneselisteningtrainer.ui.audio.entry.AudioEditScreen
import com.sun.japaneselisteningtrainer.ui.audio.entry.AudioEntryDestination
import com.sun.japaneselisteningtrainer.ui.audio.entry.AudioEntryScreen
import com.sun.japaneselisteningtrainer.ui.audio.player.MusicPlayerDestination
import com.sun.japaneselisteningtrainer.ui.audio.player.MusicPlayerScreen
import com.sun.japaneselisteningtrainer.ui.components.AudioMenuDestination
import com.sun.japaneselisteningtrainer.ui.components.AudioMenuDialog
import com.sun.japaneselisteningtrainer.ui.folder.FolderListDestination
import com.sun.japaneselisteningtrainer.ui.folder.FolderListScreen
import com.sun.japaneselisteningtrainer.ui.folder.audiolists.FolderAudioListDestination
import com.sun.japaneselisteningtrainer.ui.folder.audiolists.FolderAudioListScreen
import com.sun.japaneselisteningtrainer.ui.home.HomeDestination
import com.sun.japaneselisteningtrainer.ui.home.HomeScreen
import com.sun.japaneselisteningtrainer.ui.miniaudio.MiniAudioPlayer
import com.sun.japaneselisteningtrainer.ui.miniaudio.MiniAudioPlayerViewModel
import com.sun.japaneselisteningtrainer.ui.navigation.NavItem.Add
import com.sun.japaneselisteningtrainer.ui.navigation.NavItem.Companion.destinationRoutes
import com.sun.japaneselisteningtrainer.ui.navigation.NavItem.Companion.items

sealed class NavItem(
    val icon: ImageVector,
    val des: NavigationDestination?
) {
    object Home : NavItem(Icons.Default.Home, HomeDestination)
    object Folder : NavItem(Icons.Default.Menu, FolderListDestination)
    object Add : NavItem(Icons.Default.Add, AudioEntryDestination)
    object Search : NavItem(Icons.Default.Search, null)
    object Profile : NavItem(Icons.Default.Person, null)

    companion object {
        val items by lazy { listOf(Home, Folder, Add, Search, Profile) }
        val destinationRoutes by lazy { items.filter { it.des != null }.map { it.des?.route } }
    }
}


/**
 * Provides Navigation graph for the application.
 */
@Composable
fun TrainerNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    val miniAudioPlayerViewModel: MiniAudioPlayerViewModel =
        androidx.lifecycle.viewmodel.compose.viewModel(factory = AppViewModelProvider.Factory)

    NavHost(
        navController = navController,
        startDestination = HomeDestination.route,
        modifier = modifier
    ) {
        composable(route = HomeDestination.route) {
            HomeScreen(
                navigateToMusicPlayer = { audioId ->
                    navController.navigate(MusicPlayerDestination.createRoute(audioId))
                },
                navigationBar = {
                    TrainerNavigationBar(
                        navController = navController,
                        miniAudioPlayerViewModel = miniAudioPlayerViewModel,
                        navigateToMusicPlayer = { audioId ->
                            navController.navigate(MusicPlayerDestination.createRoute(audioId))
                        }
                    )
                },
                onAudioLongClick = { audioId ->
                    navController.navigate(AudioMenuDestination.createRoute(audioId))
                }
            )
        }
        composable(route = AudioEntryDestination.route) {
            AudioEntryScreen(
                navigateBack = { navController.popBackStack() },
                onNavigationUp = { navController.navigateUp() }
            )
        }
        composable(route = FolderListDestination.route) {
            FolderListScreen(
                navigateBar = {
                    TrainerNavigationBar(
                        navController = navController,
                        miniAudioPlayerViewModel = miniAudioPlayerViewModel,
                        navigateToMusicPlayer = { audioId ->
                            navController.navigate(MusicPlayerDestination.createRoute(audioId))
                        }
                    )
                },
                navigateToFolderAudioList = { folderId ->
                    navController.navigate("${FolderAudioListDestination.route}/$folderId")
                }
            )
        }
        composable(
            route = FolderAudioListDestination.routeWithArgs,
            arguments = listOf(navArgument(FolderAudioListDestination.folderIdArg) {
                type = NavType.IntType
            })
        ) {
            FolderAudioListScreen(
                navigateBar = {
                    TrainerNavigationBar(
                        navController = navController,
                        miniAudioPlayerViewModel = miniAudioPlayerViewModel,
                        navigateToMusicPlayer = { audioId ->
                            navController.navigate(MusicPlayerDestination.createRoute(audioId))
                        }
                    )
                },
                onNavigateUp = navController::navigateUp,
                onAudioLongClick = { audioId ->
                    navController.navigate(AudioMenuDestination.createRoute(audioId))
                }
            )
        }
        composable(
            route = MusicPlayerDestination.routeWithArgs,
            arguments = listOf(navArgument(MusicPlayerDestination.audioIdArg) {
                type = NavType.IntType
            })
        ) { backStackEntry ->
            val audioId = backStackEntry.arguments?.getInt(MusicPlayerDestination.audioIdArg) ?: 1
            MusicPlayerScreen(
                audioId = audioId,
                modifier = Modifier,
                onNavigationBack = { navController.navigateUp() },
                onEditAudio = { navController.navigate(AudioEditDestination.createRoute(audioId)) }
            )
        }
        composable(
            route = AudioEditDestination.routeWithArgs,
            arguments = listOf(navArgument(AudioEditDestination.audioIdArg) {
                type = NavType.IntType
            })
        ) { backStackEntry ->
            val audioId = backStackEntry.arguments?.getInt(AudioEditDestination.audioIdArg) ?: 1
            AudioEditScreen(
                audioId = audioId,
                navigateBack = { navController.popBackStack() },
                onNavigationUp = { navController.navigateUp() }
            )
        }
        dialog(
            route = AudioMenuDestination.routeWithArgs,
            arguments = listOf(navArgument(AudioMenuDestination.audioIdArg) {
                type = NavType.IntType
            })
        ) {
            AudioMenuDialog(
                onEdit = { audioId ->
                    navController.navigate(AudioEditDestination.createRoute(audioId))
                },
                onDismiss = { navController.navigateUp() }
            )
        }
    }
}


@Composable
fun TrainerNavigationBar(
    navController: NavHostController,
    miniAudioPlayerViewModel: MiniAudioPlayerViewModel,
    navigateToMusicPlayer: (Int) -> Unit
) {
    val navBackStackEntry = navController.currentBackStackEntry
    var currentDes by remember { mutableStateOf(navBackStackEntry?.destination?.route ?: "") }

    LaunchedEffect(navBackStackEntry) {
        val des = navBackStackEntry?.destination?.route
        if (des in destinationRoutes) currentDes = des.toString()
    }

    val uiState by miniAudioPlayerViewModel.uiState.collectAsState()

    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 3.dp,
        modifier = Modifier.navigationBarsPadding()
    ) {
        Column {
            uiState.currentAudio?.let { audio ->
                MiniAudioPlayer(
                    audioTitle = audio.title,
                    isPlaying = uiState.isPlaying,
                    isFavorite = audio.isFavorite,
                    onPlayPause = { miniAudioPlayerViewModel.pauseAudio() },
                    onPrevious = { miniAudioPlayerViewModel.playPrevious() },
                    onNext = { miniAudioPlayerViewModel.playNext() },
                    onFavorite = { miniAudioPlayerViewModel.toggleFavorite(audio.id) },
                    onClickPlayer = { navigateToMusicPlayer(audio.id) }
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (item in items) {
                    val selected = currentDes == item.des?.route
                    TrainerNavigationBarItem(
                        icon = item.icon,
                        onClick = { item.des?.route?.let { navController.navigate(it) } },
                        isSelected = item == Add || selected
                    )
                }
            }
        }
    }
}

@Composable
fun TrainerNavigationBarItem(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    onClick: () -> Unit,
    isSelected: Boolean = false
) {
    Box(
        modifier = modifier
            .size(48.dp)
            .background(
                color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else Color.Transparent,
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        IconButton(onClick = onClick) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}