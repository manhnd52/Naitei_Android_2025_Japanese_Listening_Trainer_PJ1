package com.sun.japaneselisteningtrainer.data.repository.mock

import com.sun.japaneselisteningtrainer.data.folder.FolderRepository
import com.sun.japaneselisteningtrainer.data.model.Folder
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class MockFolderRepository : FolderRepository {
    val folderDatabase = mutableListOf<Folder>()

    init {
        folderDatabase.add(Folder(1, "Folder 1", "Description for Folder 1"))
        folderDatabase.add(Folder(2, "Folder 2", "Description for Folder 2"))
        folderDatabase.add(Folder(3, "Folder 3", "Description for Folder 3"))
    }

    override suspend fun add(folder: Folder) {
        TODO("Not yet implemented")
    }

    override suspend fun delete(folder: Folder) {
        TODO("Not yet implemented")
    }

    override suspend fun update(folder: Folder) {
        TODO("Not yet implemented")
    }

    override fun getAllFolderStream(): Flow<List<Folder>> = flow {
        emit(folderDatabase)
    }

    override fun getFolderStream(id: Int): Flow<Folder?> = flow {
        val folder = folderDatabase.find { it.id == id }
        emit(folder)
    }

}
