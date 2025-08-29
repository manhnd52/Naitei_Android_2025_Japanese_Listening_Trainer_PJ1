package com.sun.japaneselisteningtrainer

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.sun.japaneselisteningtrainer.data.folder.FolderRepository
import com.sun.japaneselisteningtrainer.data.model.Audio
import com.sun.japaneselisteningtrainer.data.model.Folder
import com.sun.japaneselisteningtrainer.data.repository.AudioRepository
import com.sun.japaneselisteningtrainer.service.AudioServiceManager
import com.sun.japaneselisteningtrainer.ui.folder.audiolists.FolderAudioListDestination
import com.sun.japaneselisteningtrainer.ui.folder.audiolists.FolderAudioListViewModel
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class FolderAudioListViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var folderRepository: FolderRepository
    private lateinit var audioRepository: AudioRepository
    private lateinit var audioServiceManager: AudioServiceManager

    private lateinit var viewModel: FolderAudioListViewModel

    private val folderFlow = MutableSharedFlow<Folder?>()
    private val audioFlow = MutableSharedFlow<List<Audio>>()
    private val isPlayingFlow = MutableStateFlow<Boolean>(false)
    private val currentAudioFlow = MutableStateFlow<Audio?>(null)

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        folderRepository = mockk(relaxed = true)
        audioRepository = mockk(relaxed = true)
        audioServiceManager = mockk(relaxed = true)

        every { folderRepository.getFolderStream(any(Int::class)) } returns folderFlow
        every { audioRepository.getFolderAudiosStream(any()) } returns audioFlow
        every { audioServiceManager.isPlaying } returns isPlayingFlow
        every { audioServiceManager.currentAudio } returns currentAudioFlow

        val savedStateHandle = SavedStateHandle(mapOf(FolderAudioListDestination.folderIdArg to 1))
        viewModel = FolderAudioListViewModel(
            savedStateHandle,
            folderRepository,
            audioRepository,
            audioServiceManager
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `when folder emits, uiState should update folder`() = runTest {
        viewModel.uiState.test {
            assertEquals(Folder(), awaitItem().folder)

            folderFlow.emit(Folder(id = 10, name = "JLPT N3"))
            advanceUntilIdle()

            val updated = awaitItem()
            assertEquals(10, updated.folder.id)
            assertEquals("JLPT N3", updated.folder.name)
        }
    }

    @Test
    fun `when audioRepository emits, uiState should update audio list`() = runTest {
        viewModel.uiState.test {
            skipItems(1)

            audioFlow.emit(listOf(Audio(1, "Track A"), Audio(2, "Track B")))
            advanceUntilIdle()

            val updated = awaitItem()
            assertEquals(2, updated.audioItemInfoList.size)
            assertEquals("Track A", updated.audioItemInfoList[0].title)
        }
    }

    @Test
    fun `when audioServiceManager emits isPlaying, uiState should update isPlaying`() = runTest {
        viewModel.uiState.test {
            skipItems(1)

            isPlayingFlow.emit(true)
            advanceUntilIdle()

            val updated = awaitItem()
            assertEquals(true, updated.isPlaying)
        }
    }

    @Test
    fun `when currentAudio emits, uiState should update playingAudioId`() = runTest {
        viewModel.uiState.test {
            skipItems(1)

            currentAudioFlow.emit(Audio(99, "Now Playing"))
            advanceUntilIdle()

            val updated = awaitItem()
            assertEquals(99, updated.playingAudioId)
        }
    }

    @Test
    fun `playPause should call audioServiceManager`() = runTest {
        viewModel.playPause(42)
        advanceUntilIdle()

        coVerify { audioServiceManager.playPause(42) }
    }

    @Test
    fun `playAll should call audioServiceManager`() = runTest {
        // default folder id is 1 (from SavedStateHandle)
        viewModel.playAll()
        advanceUntilIdle()

        coVerify { audioServiceManager.playPlaylist(1) }
    }

    @Test
    fun `favorite should call audioServiceManager`() = runTest {
        viewModel.favorite(77)
        coVerify { audioServiceManager.toggleFavoriteStatus(77) }
    }

}



