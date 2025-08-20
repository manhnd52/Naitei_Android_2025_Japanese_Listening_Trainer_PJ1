package com.sun.japaneselisteningtrainer.ui.folder.audiolists

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sun.japaneselisteningtrainer.R
import com.sun.japaneselisteningtrainer.TrainerTopAppBar
import com.sun.japaneselisteningtrainer.ui.AppViewModelProvider
import com.sun.japaneselisteningtrainer.ui.folder.AddButton
import com.sun.japaneselisteningtrainer.ui.folder.FolderListDestination
import com.sun.japaneselisteningtrainer.ui.navigation.NavigationDestination

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
    navigateBar: @Composable () -> Unit
) {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    var createFolderRequired by remember { mutableStateOf(false) }
    val snackBarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TrainerTopAppBar(
                title = stringResource(FolderListDestination.titleRes),
                canNavigateBack = true,
                scrollBehavior = scrollBehavior,
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
        Box {
            Text(
                text = "Folder " + viewModel.folderId
            )
        }
    }
}
