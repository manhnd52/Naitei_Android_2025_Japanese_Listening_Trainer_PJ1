package com.sun.japaneselisteningtrainer.ui.folder.create

import androidx.annotation.RestrictTo
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sun.japaneselisteningtrainer.R
import com.sun.japaneselisteningtrainer.ui.AppViewModelProvider

@Composable
fun CreateFolderDialog(
    onCreateConfirm: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CreateFolderViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState = viewModel.createFolderUiState
    val isError = !uiState.isEntryValid && uiState.title.isNotBlank()
    AlertDialog(
        onDismissRequest = { onCancel() },
        containerColor = MaterialTheme.colorScheme.surface,
        title = { Text(stringResource(R.string.create_folder_title)) },
        text = {
            Column {
                OutlinedTextField(
                    value = uiState.title,
                    onValueChange = {
                        viewModel.updateUiState(title = it)
                    },
                    label = {
                        if (isError) {
                            Text(
                                text = stringResource(R.string.folder_title_exists),
                                color = MaterialTheme.colorScheme.error
                            )
                        } else {
                            Text(stringResource(R.string.title))
                        }
                    },
                    singleLine = true,
                    isError = isError,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = uiState.description,
                    onValueChange = { viewModel.updateUiState(description = it) },
                    label = { Text(stringResource(R.string.description)) },
                    placeholder = { Text(stringResource(R.string.description_placeholder)) },
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
                onClick = {
                    viewModel.createFolder()
                    onCreateConfirm()
                },
                enabled = uiState.isEntryValid
            ) {
                Text(stringResource(R.string.ok))
            }
        }
    )
}

