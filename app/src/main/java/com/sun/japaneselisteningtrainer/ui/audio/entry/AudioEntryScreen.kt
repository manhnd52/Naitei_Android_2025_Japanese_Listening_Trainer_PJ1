package com.sun.japaneselisteningtrainer.ui.audio.entry

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sun.japaneselisteningtrainer.R
import com.sun.japaneselisteningtrainer.TrainerTopAppBar
import com.sun.japaneselisteningtrainer.ui.AppViewModelProvider
import com.sun.japaneselisteningtrainer.ui.AudioFilePicker
import com.sun.japaneselisteningtrainer.ui.folder.components.FolderPicker
import com.sun.japaneselisteningtrainer.ui.navigation.NavigationDestination
import kotlinx.coroutines.launch

object AudioEntryDestination : NavigationDestination {
    override val route = "audio_entry"
    override val titleRes = R.string.audio_entry_title
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AudioEntryScreen(
    navigateBack: () -> Unit,
    onNavigationUp: () -> Unit,
    viewModel: AudioEntryViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val audioForm = viewModel.uiState.audioForm
    var openDialog by remember { mutableStateOf(false) }
    var selectedFolder by remember { mutableStateOf("") }

    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TrainerTopAppBar(
                title = stringResource(AudioEntryDestination.titleRes),
                canNavigateBack = true,
                navigateUp = onNavigationUp
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier.padding(innerPadding)
        ) {
            TextField(
                value = audioForm.title,
                onValueChange = { viewModel.updateForm(audioForm.copy(title = it)) },
                label = { Text(stringResource(R.string.title)) }
            )
            TextField(
                value = audioForm.script,
                onValueChange = { viewModel.updateForm(audioForm.copy(script = it)) },
                label = { Text(stringResource(R.string.script)) }
            )

            AudioFilePicker(
                onFileSelected = { viewModel.updateForm(audioForm.copy(selectedFileUri = it)) },
            )

            Button(
                onClick = { openDialog = true }
            ) {
                Text("Open Folder Picker")
            }
            Text("Selected Folder: $selectedFolder")

            if (openDialog) {
                FolderPicker(
                    onFolderSelected = {
                        selectedFolder = it.name
                        viewModel.updateForm(audioForm.copy(folderId = it.id))
                        openDialog = false
                    },
                    onDismiss = { openDialog = false }
                )
            }

            Button(
                onClick = {
                    coroutineScope.launch {
                        viewModel.addAudio()
                        navigateBack()
                    }
                },
                enabled = viewModel.uiState.isValid
            ) {
                Text(stringResource(R.string.add_audio))
            }
        }
    }
}

