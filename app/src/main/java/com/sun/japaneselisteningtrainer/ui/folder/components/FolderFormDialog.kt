package com.sun.japaneselisteningtrainer.ui.folder.components

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
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.sun.japaneselisteningtrainer.R
import com.sun.japaneselisteningtrainer.data.model.Folder

const val DESCRIPTION_MAX_LENGTH = 100
const val TITLE_MAX_LENGTH = 30

/**
 * Selected used to populate the form when editing a folder.
 */
@Composable
fun FolderFormDialog(
    modifier: Modifier = Modifier,
    title: String = "",
    uiState: FolderFormUiState,
    onValueChange: (FolderFormUiState) -> Unit,
    onCancel: () -> Unit,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = { onCancel() },
        containerColor = MaterialTheme.colorScheme.surface,
        title = { Text(title) },
        text = {
            FolderInputForm(
                uiState = uiState,
                onValueChange = onValueChange
            )
        },
        modifier = modifier,
        dismissButton = {
            TextButton(onClick = {
                onCancel()
            }) {
                Text(stringResource(R.string.cancel))
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm()
                },
                enabled = uiState.isEntryValid
            ) {
                Text(stringResource(R.string.ok))
            }
        }
    )
}

@Composable
fun FolderInputForm(
    uiState: FolderFormUiState,
    onValueChange: (FolderFormUiState) -> Unit,
) {
    val isDescriptionOverLimit = uiState.description.length > DESCRIPTION_MAX_LENGTH
    val isTitleOverLimit = uiState.title.length > TITLE_MAX_LENGTH
    val isTitleFieldError = uiState.doesTitleExist || isTitleOverLimit
    Column {
        OutlinedTextField(
            value = uiState.title,
            onValueChange = { onValueChange(uiState.copy(title = it)) },
            label = {
                when {
                    isTitleOverLimit -> {
                        Text(stringResource(R.string.max_chars, TITLE_MAX_LENGTH))
                    }
                    uiState.doesTitleExist -> {
                        Text(stringResource(R.string.folder_title_exists))
                    }
                    else -> Text(stringResource(R.string.title))

                }
            },
            singleLine = true,
            isError = isTitleFieldError,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = uiState.description,
            onValueChange = { onValueChange(uiState.copy(description = it)) },
            label = {
                if (isDescriptionOverLimit) {
                    Text(stringResource(R.string.max_chars, DESCRIPTION_MAX_LENGTH))
                } else Text (stringResource(R.string.description))
            },
            placeholder = { Text(stringResource(R.string.description_placeholder)) },
            modifier = Modifier.fillMaxWidth(),
            isError = isDescriptionOverLimit,
        )
    }
}

data class FolderFormUiState(
    val title: String = "",
    val description: String = "",
    val isEntryValid: Boolean = false,
    val doesTitleExist: Boolean = false
)

fun FolderFormUiState.toFolder(): Folder = Folder(
    name = title,
    description = description
)

fun Folder.toFolderFormUiState(): FolderFormUiState = FolderFormUiState(
    title = name,
    description = description,
    isEntryValid = false,
    doesTitleExist = false
)
