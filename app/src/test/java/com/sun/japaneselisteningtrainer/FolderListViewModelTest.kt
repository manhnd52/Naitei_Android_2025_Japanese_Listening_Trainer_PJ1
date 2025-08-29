package com.sun.japaneselisteningtrainer

import com.sun.japaneselisteningtrainer.data.folder.FolderRepository
import com.sun.japaneselisteningtrainer.data.model.Folder
import com.sun.japaneselisteningtrainer.ui.folder.FolderListViewModel
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue


@OptIn(ExperimentalCoroutinesApi::class)
class FolderListViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: FolderRepository
    private lateinit var folderFlow: MutableSharedFlow<List<Folder>>
    private lateinit var viewModel: FolderListViewModel

    @Before
    fun setup() {
        // With StandardTestDispatcher,coroutine will not automatically advance.
        // You can use advanceUntilIdle() to manually advance the coroutine until it is idle (have no active jobs).
        Dispatchers.setMain(testDispatcher)
        // What is relaxed? →  tạo repo giả, các hàm chưa define behavior thì tự trả về default cho giá trị của biến đó.
        repository = mockk(relaxed = true)
        folderFlow = MutableSharedFlow(replay = 1)
        // Định nghĩa behavior cho hàm suspend → nếu ai gọi
        coEvery { repository.getAllFolderStream() } returns folderFlow

        viewModel = FolderListViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `collect folder list updates ui state`() = runTest {
        val folders = listOf(Folder(id = 1, name = "N3"), Folder(id = 2, name = "N2"))
        folderFlow.emit(folders)
        advanceUntilIdle()
        assertEquals(folders, viewModel.folderListUiState.folderList)
    }

    @Test
    fun `openFolderMenu should set showMenu true and select folder`() {
        val folder = Folder(id = 1, name = "N3")
        viewModel.openFolderMenu(folder)

        assertTrue(viewModel.folderListUiState.showMenu)
        assertEquals(folder, viewModel.folderListUiState.selectedFolder)
    }

    @Test
    fun `dismissMenu should set showMenu false`() {
        viewModel.folderListUiState = viewModel.folderListUiState.copy(showMenu = true)
        viewModel.dismissMenu()
        assertFalse(viewModel.folderListUiState.showMenu)
    }

    @Test
    fun `deleteSelectedFolderFromUi should remove folder from state`() {
        val folder1 = Folder(id = 1, name = "N3")
        val folder2 = Folder(id = 2, name = "N2")

        viewModel.folderListUiState = viewModel.folderListUiState.copy(
            folderList = listOf(folder1, folder2),
            selectedFolder = folder1
        )

        viewModel.deleteSelectedFolderFromUi()

        assertEquals(listOf(folder2), viewModel.folderListUiState.folderList)
    }

    @Test
    fun `restoreFolder should restore last deleted list`() {
        val folder1 = Folder(id = 1, name = "N3")
        val folder2 = Folder(id = 2, name = "N2")

        viewModel.folderListUiState = viewModel.folderListUiState.copy(
            folderList = listOf(folder1, folder2),
            selectedFolder = folder1
        )

        viewModel.deleteSelectedFolderFromUi()
        viewModel.restoreFolder()

        assertEquals(listOf(folder1, folder2), viewModel.folderListUiState.folderList)
    }

    @Test
    fun `showEditForm sets showEditForm true`() {
        viewModel.showEditForm()
        assertTrue(viewModel.folderListUiState.showEditForm)
    }

    @Test
    fun `dismissEditForm sets showEditForm false`() {
        viewModel.folderListUiState = viewModel.folderListUiState.copy(showEditForm = true)
        viewModel.dismissEditForm()
        assertFalse(viewModel.folderListUiState.showEditForm)
    }

    @Test
    fun `deleteFolder should call repository delete`() = runTest {
        val folder = Folder(id = 42, name = "JLPT")
        viewModel.folderListUiState = viewModel.folderListUiState.copy(selectedFolder = folder)

        // push một state vào DeletedCache để tránh EmptyStackException
        viewModel.deleteSelectedFolderFromUi()

        viewModel.deleteFolder()
        advanceUntilIdle()

        coVerify { repository.delete(42) }
    }
}
