package com.sun.japaneselisteningtrainer.data

import android.content.Context
import com.sun.japaneselisteningtrainer.data.folder.FolderRepository
import com.sun.japaneselisteningtrainer.data.repository.mock.MockFolderRepository
import com.sun.japaneselisteningtrainer.data.repository.AudioRepository
import com.sun.japaneselisteningtrainer.data.repository.local.JLTDbHelper
import com.sun.japaneselisteningtrainer.data.repository.local.LocalAudioRepository
import com.sun.japaneselisteningtrainer.data.repository.local.LocalFolderRepository
import com.sun.japaneselisteningtrainer.data.repository.mock.MockAudioRepository
import com.sun.japaneselisteningtrainer.data.storage.AudioFileStorage
import com.sun.japaneselisteningtrainer.data.storage.ExternalAudioFileStorage


/**
 * App container for Dependency injection.
 */
interface AppContainer {
    val audioRepository: AudioRepository
    val folderRepository: FolderRepository
    val audioFileStorage: AudioFileStorage
}

/**
 * [AppContainer] implementation that provides instance of mock repository
 */
class MockAppDataContainer(private val context: Context) : AppContainer {
    /**
     * Implementation for [AudioRepository]
     */
    override val audioRepository: AudioRepository = MockAudioRepository()
    /**
     * Implementation for [FolderRepository]
     */
    override val folderRepository: FolderRepository = MockFolderRepository()
    override val audioFileStorage: AudioFileStorage = ExternalAudioFileStorage(context)
}

class AppDataContainer(private val context: Context) : AppContainer {
    val dbHelper = JLTDbHelper(context)
    /**
     * Implementation for [AudioRepository]
     */
    override val audioRepository: AudioRepository by lazy {
        LocalAudioRepository(JLTDbHelper(context))
    }
    /**
     * Implementation for [FolderRepository]
     */
    override val folderRepository: FolderRepository by lazy {
        LocalFolderRepository(JLTDbHelper(context))
    }
    override val audioFileStorage: AudioFileStorage by lazy {
        ExternalAudioFileStorage(context)
    }
}
