package com.sun.japaneselisteningtrainer.ui.folder.components

import android.app.ProgressDialog.show
import android.view.Gravity
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeightIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sun.japaneselisteningtrainer.R
import com.sun.japaneselisteningtrainer.data.folder.FolderRepository
import com.sun.japaneselisteningtrainer.data.model.Folder
import com.sun.japaneselisteningtrainer.ui.AppViewModelProvider
import com.sun.japaneselisteningtrainer.ui.folder.create.CreateFolderDialog
import com.sun.japaneselisteningtrainer.ui.theme.JapaneseListeningTrainerTheme
import kotlinx.coroutines.launch

@Composable
fun FolderPicker(
    modifier: Modifier = Modifier,
    title: String = stringResource(R.string.select_folder),
    viewModel: FolderPickerViewModel = viewModel(factory = AppViewModelProvider.Factory),
    onFolderSelected: (Folder) -> Unit,
    onDismiss: () -> Unit,
) {
    val uiState = viewModel.uiState
    val context = LocalContext.current
    when {
        uiState.showAddDialog -> CreateFolderDialog(
            onCreateConfirm = {
                viewModel.dismissCreateDialog()
                val toast = Toast.makeText(
                    context,
                    context.getString(R.string.folder_created),
                    Toast.LENGTH_SHORT
                ).show()
            },
            onCancel = { viewModel.dismissCreateDialog() },
        )

        else -> FolderPickerDialog(
            modifier = modifier,
            title = title,
            folderList = uiState.folderList,
            onFolderSelected = onFolderSelected,
            onDismiss = onDismiss,
            onCreateFolder = { viewModel.openCreateDialog() }
        )
    }
}

//TODO: Optimize folder search folder instead of using getAllFolderStream()
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FolderPickerDialog(
    modifier: Modifier = Modifier,
    title: String,
    folderList: List<Folder>,
    onFolderSelected: (Folder) -> Unit,
    onDismiss: () -> Unit,
    onCreateFolder: () -> Unit
) {
    var query by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = { onDismiss() },
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(title)
                Spacer(modifier = Modifier.weight(1f))
                AddFolderButton(
                    onClick = onCreateFolder
                )
            }
        },
        text = {
            Column {
                FolderSearchField(query = query, onQueryChange = { query = it })
                Spacer(modifier = Modifier.height(8.dp))
                FolderList(
                    modifier = Modifier.fillMaxWidth(),
                    folderList = folderList,
                    query = query,
                    onFolderSelected = {
                        onDismiss()
                        onFolderSelected(it)
                    }
                )
            }
        },
        modifier = modifier,
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        },
        confirmButton = { }
    )
}

@Composable
fun FolderSearchField(
    modifier: Modifier = Modifier,
    query: String,
    onQueryChange: (String) -> Unit,
) {
    TextField(
        value = query,
        onValueChange = onQueryChange,
        label = { Text(stringResource(R.string.search)) },
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = TextFieldDefaults.colors(
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,
            errorIndicatorColor = Color.Transparent
        )
    )
}

@Composable
fun FolderList(
    modifier: Modifier = Modifier,
    folderList: List<Folder>,
    query: String,
    onFolderSelected: (Folder) -> Unit,
) {
    val state = rememberLazyListState()

    LaunchedEffect(query) {
        state.scrollToItem(0)
    }

    LazyColumn(
        modifier = modifier
            .requiredHeightIn(max = 175.dp)
            .padding(start = 4.dp),
        state = state
    ) {
        items(
            items = folderList.filter { it.name.contains(query, ignoreCase = true) },
            key = { it.id }
        ) { it ->
            FolderTextButton(
                folder = it,
                onClick = {
                    onFolderSelected(it)
                }
            )
        }
    }
}

@Composable
fun FolderTextButton(
    folder: Folder,
    onClick: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(vertical = 5.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Folder,
            tint = MaterialTheme.colorScheme.primary,
            contentDescription = null
        )
        Spacer(Modifier.width(5.dp))
        Text(
            text = folder.name,
            style = MaterialTheme.typography.displaySmall,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
fun AddFolderButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    IconButton(
        onClick = onClick,
        modifier = modifier
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            tint = MaterialTheme.colorScheme.primary,
            contentDescription = stringResource(R.string.add_folder)
        )
    }
}

@Preview
@Composable
fun FolderTextButtonPreview() {
    JapaneseListeningTrainerTheme {
        FolderTextButton(
            folder = Folder(1, "Test Folder", "Test Description"),
            onClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun FolderPickerPreview() {
    JapaneseListeningTrainerTheme {
        FolderPickerDialog(
            title = "Select Folder",
            folderList = listOf(
                Folder(1, "Test Folder", "Test Description"),
                Folder(2, "Test Folder 2", "Test Description 2"),
                Folder(3, "Test Folder 3", "Test Description 3"),
                Folder(4, "Test Folder 4", "Test Description 4"),
                Folder(5, "Test Folder 5", "Test Description 5"),
                Folder(6, "Test Folder 6", "Test Description 6"),
                Folder(7, "Test Folder 7", "Test Description 7"),
                Folder(8, "Test Folder 8", "Test Description 8"),
                Folder(9, "Test Folder 9", "Test Description 9"),
                Folder(10, "Test Folder 10", "Test Description 10"),
            ),
            onFolderSelected = {},
            onDismiss = {},
            onCreateFolder = {}
        )
    }
}

class FolderPickerViewModel(private val folderRepository: FolderRepository) : ViewModel() {
    var uiState by mutableStateOf(FolderPickerUiState())

    init {
        viewModelScope.launch {
            folderRepository.getAllFolderStream().collect { it ->
                uiState = uiState.copy(
                    folderList = it
                )
            }
        }
    }

    fun dismissCreateDialog() {
        uiState = uiState.copy(showAddDialog = false)
    }

    fun openCreateDialog() {
        uiState = uiState.copy(showAddDialog = true)
    }
}

data class FolderPickerUiState(
    val folderList: List<Folder> = listOf(),
    val showAddDialog: Boolean = false
)
