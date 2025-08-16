package com.sun.japaneselisteningtrainer.ui.folder

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sun.japaneselisteningtrainer.R
import com.sun.japaneselisteningtrainer.TrainerTopAppBar
import com.sun.japaneselisteningtrainer.data.model.Folder
import com.sun.japaneselisteningtrainer.ui.AppViewModelProvider
import com.sun.japaneselisteningtrainer.ui.navigation.NavigationDestination
import com.sun.japaneselisteningtrainer.ui.theme.JapaneseListeningTrainerTheme


object FolderListDestination : NavigationDestination {
    override val route: String = "folder_list"
    override val titleRes: Int = R.string.folder_list_title
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FolderListScreen(
    modifier: Modifier = Modifier,
    navigateBar: @Composable () -> Unit = {},
    viewModel: FolderListViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState by viewModel.homeUiState.collectAsState()
    val createFolderUiState = viewModel.createFolderUiState
    var createFolderRequired by remember { mutableStateOf(false) }
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TrainerTopAppBar(
                title = stringResource(FolderListDestination.titleRes),
                canNavigateBack = false,
                navigateUp = { },
                actions = {
                    AddButton(
                        onClick = { createFolderRequired = true }
                    )
                },
            )
        },
        bottomBar = {
            navigateBar()
        }
    ) { it ->
        LazyColumn(contentPadding = it) {
            items(uiState.folderList) {
                FolderItem(
                    folder = it,
                    modifier = Modifier.padding(dimensionResource(R.dimen.dp_8))
                )
            }
        }
        if (createFolderRequired) {
            CreateFolderDialog(
                title = createFolderUiState.title,
                description = createFolderUiState.description,
                onTitleChange = { title ->
                    viewModel.updateUiState(
                        title,
                        createFolderUiState.description
                    )
                },
                onDescriptionChange = { description ->
                    viewModel.updateUiState(
                        createFolderUiState.title,
                        description
                    )
                },
                onCreateConfirm = {
                    viewModel.createFolder()
                    createFolderRequired = false
                },
                onCancel = { createFolderRequired = false },
                isEntryValid = createFolderUiState.isEntryValid
            )
        }
    }
}

@Composable
fun AddButton(
    onClick: () -> Unit
) {
    IconButton(onClick = { onClick() }) {
        Icon(
            imageVector = Icons.Filled.Add,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
fun CreateFolderDialog(
    title: String,
    description: String,
    onTitleChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onCreateConfirm: () -> Unit,
    isEntryValid: Boolean,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {

    AlertDialog(
        onDismissRequest = { onCancel() },
        containerColor = MaterialTheme.colorScheme.surface,
        title = { Text(stringResource(R.string.create_folder_title)) },
        text = {
            Column {
                OutlinedTextField(
                    value = title,
                    onValueChange = { onTitleChange(it) },
                    label = { Text(stringResource(R.string.title)) },
                    singleLine = true,
                    isError = isEntryValid,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = { onDescriptionChange(it) },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        modifier = modifier,
        dismissButton = {
            TextButton(onClick = onCancel) {
                Text(stringResource(R.string.cancel))
            }
        },
        confirmButton = {
            TextButton(
                onClick = onCreateConfirm,
                enabled = isEntryValid
            ) {
                Text(stringResource(R.string.ok))
            }
        }
    )
}


@Composable
fun FolderItem(folder: Folder, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .sizeIn(minHeight = 72.dp, maxHeight = 72.dp)
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = folder.name,
                    style = MaterialTheme.typography.displaySmall,
                )
                Text(
                    text = folder.description,
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
            Spacer(Modifier.width(16.dp))
            Box(
                modifier = Modifier.size(72.dp)
            ) {
                Text(
                    text = stringResource(R.string.percent_string, 50),
                    style = MaterialTheme.typography.displayLarge,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }
}

@Preview
@Composable
fun FolderItemPreview() {
    val folder = Folder(1, "Folder 1", "Super long description")
    JapaneseListeningTrainerTheme {
        FolderItem(folder = folder)
    }
}
