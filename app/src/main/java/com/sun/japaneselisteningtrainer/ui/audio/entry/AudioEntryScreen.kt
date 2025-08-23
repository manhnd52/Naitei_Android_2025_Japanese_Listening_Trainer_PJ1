package com.sun.japaneselisteningtrainer.ui.audio.entry

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sun.japaneselisteningtrainer.TrainerTopAppBar
import com.sun.japaneselisteningtrainer.R
import com.sun.japaneselisteningtrainer.ui.AppViewModelProvider
import com.sun.japaneselisteningtrainer.ui.folder.components.FolderPicker
import com.sun.japaneselisteningtrainer.ui.navigation.NavigationDestination
import kotlinx.coroutines.launch
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.platform.LocalContext

object AudioEntryDestination : NavigationDestination {
    override val route = "audio_entry"
    override val titleRes = R.string.audio_entry_title
}

object AudioEditDestination : NavigationDestination {
    override val route = "audio_edit"
    override val titleRes = R.string.edit_audio
    const val audioIdArg = "audioId"
    val routeWithArgs = "$route/{$audioIdArg}"
    fun createRoute(audioId: Int) = "$route/$audioId"
}

private const val MAX_TITLE_LENGTH = 100
private const val MAX_SCRIPT_LENGTH = 1000
private const val MAX_TRANSLATION_LENGTH = 1000

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AudioEntryScreen(
    navigateBack: () -> Unit,
    onNavigationUp: () -> Unit,
    audioEntryViewModel: AudioEntryViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val coroutineScope = rememberCoroutineScope()
    val uiState = audioEntryViewModel.uiState
    val audioForm = uiState.audioForm

    var openDialog by remember { mutableStateOf(false) }
    var selectedFolder by remember { mutableStateOf("") }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            audioEntryViewModel.updateForm(audioForm.copy(selectedFileUri = uri))
        }
    }

    Scaffold(
        topBar = {
            TrainerTopAppBar(
                title = stringResource(R.string.add_audio),
                canNavigateBack = true,
                navigateUp = onNavigationUp
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(dimensionResource(R.dimen.dp_16)),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CustomTextField(
                label = stringResource(R.string.audio_title),
                value = audioForm.title,
                onValueChange = {
                    if (it.length <= MAX_TITLE_LENGTH) {
                        audioEntryViewModel.updateForm(audioForm.copy(title = it))
                    }
                }
            )

            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.dp_16)))

            CustomTextField(
                modifier = Modifier.weight(1f),
                label = stringResource(R.string.audio_script),
                value = audioForm.script,
                onValueChange = {
                    if (it.length <= MAX_SCRIPT_LENGTH) {
                        audioEntryViewModel.updateForm(audioForm.copy(script = it))
                    }
                }
            )

            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.dp_16)))

            OutlinedTextField(
                value = audioForm.translate,
                onValueChange = {
                    if (it.length <= MAX_TRANSLATION_LENGTH) {
                        audioEntryViewModel.updateForm(audioForm.copy(translate = it))
                    }
                },
                label = { Text(stringResource(R.string.translation)) },
                modifier = Modifier.fillMaxWidth().weight(1f),
                trailingIcon = {
                    IconButton(
                        onClick = {
                            // TODO: gọi hàm dịch tự động bằng AI
                        },
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.logoai),
                            contentDescription = stringResource(R.string.translate_auto),
                            tint = Color.Unspecified,
                        )
                    }
                }
            )

            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.dp_16)))
            UploadField(
                fileUri = audioForm.selectedFileUri,
                onClickUpload = { filePickerLauncher.launch("audio/*") }
            )

            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.dp_20)))

            Button(onClick = { openDialog = true }) {
                Text(stringResource(R.string.open_folder_picker))
            }
            Text(stringResource(R.string.selected_folder, selectedFolder))

            if (openDialog) {
                FolderPicker(
                    onFolderSelected = {
                        selectedFolder = it.name
                        audioEntryViewModel.updateForm(audioForm.copy(folderId = it.id))
                        openDialog = false
                    },
                    onDismiss = { openDialog = false }
                )
            }

            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.dp_20)))

            Button(
                onClick = {
                    coroutineScope.launch {
                        audioEntryViewModel.addAudio()
                        navigateBack()
                    }
                },
                enabled = uiState.isValid,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.save_audio))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AudioEditScreen(
    audioId: Int,
    navigateBack: () -> Unit,
    onNavigationUp: () -> Unit,
    audioEntryViewModel: AudioEntryViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val coroutineScope = rememberCoroutineScope()
    val uiState = audioEntryViewModel.uiState
    val audioForm = uiState.audioForm

    // Load audio data for editing
    LaunchedEffect(audioId) {
        audioEntryViewModel.loadAudioForEdit(audioId)
    }

    Scaffold(
        topBar = {
            TrainerTopAppBar(
                title = stringResource(R.string.edit_audio),
                canNavigateBack = true,
                navigateUp = onNavigationUp
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(dimensionResource(R.dimen.dp_16)),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CustomTextField(
                label = stringResource(R.string.audio_title),
                value = audioForm.title,
                onValueChange = {
                    if (it.length <= MAX_TITLE_LENGTH) {
                        audioEntryViewModel.updateForm(audioForm.copy(title = it))
                    }
                }
            )

            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.dp_16)))

            CustomTextField(
                label = stringResource(R.string.audio_script),
                value = audioForm.script,
                onValueChange = {
                    if (it.length <= MAX_SCRIPT_LENGTH) {
                        audioEntryViewModel.updateForm(audioForm.copy(script = it))
                    }
                }
            )

            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.dp_16)))

            OutlinedTextField(
                value = audioForm.translate,
                onValueChange = {
                    if (it.length <= MAX_TRANSLATION_LENGTH) {
                        audioEntryViewModel.updateForm(audioForm.copy(translate = it))
                    }
                },
                label = { Text(stringResource(R.string.translation)) },
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    IconButton(
                        onClick = {
                            // TODO: gọi hàm dịch tự động bằng AI
                        },
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.logoai),
                            contentDescription = stringResource(R.string.translate_auto),
                            tint = Color.Unspecified,
                        )
                    }
                }
            )

            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.dp_20)))

            Button(
                onClick = {
                    coroutineScope.launch {
                        audioEntryViewModel.updateAudio()
                        navigateBack()
                    }
                },
                enabled = uiState.isValid,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.update_audio))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomTextField(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = modifier.fillMaxWidth()
    )
}

@Composable
fun UploadField(
    fileUri: Uri,
    onClickUpload: () -> Unit
) {
    val context = LocalContext.current

    Column(modifier = Modifier.fillMaxWidth()) {
        if (fileUri != Uri.EMPTY) {
            Text(
                text = stringResource(
                    id = R.string.selected_file,
                    fileUri.lastPathSegment ?: stringResource(R.string.unknown_file)
                ),
                style = MaterialTheme.typography.bodyMedium
            )
        } else {
            Text(
                text = stringResource(R.string.no_file_selected),
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.dp_8)))

        Button(onClick = onClickUpload) {
            Text(text = stringResource(R.string.upload_audio))
        }
    }
}
