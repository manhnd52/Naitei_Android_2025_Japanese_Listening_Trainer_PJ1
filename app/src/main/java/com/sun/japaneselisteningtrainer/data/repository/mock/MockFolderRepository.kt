package com.sun.japaneselisteningtrainer.data.repository.mock

import android.util.Log
import com.sun.japaneselisteningtrainer.data.folder.FolderRepository
import com.sun.japaneselisteningtrainer.data.model.Folder
import com.sun.japaneselisteningtrainer.data.repository.local.ChangeObserver
import com.sun.japaneselisteningtrainer.data.repository.local.DbChangeNotifier
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext

class MockFolderRepository : FolderRepository {
    private val folderDatabase = mutableListOf<Folder>()
    private val notifier = DbChangeNotifier()
    init {
        folderDatabase.add(Folder(1, "Folder 1", "Description for Folder 1"))
        folderDatabase.add(Folder(2, "Folder 2", "Description for Folder 2"))
        folderDatabase.add(Folder(3, "Folder 3", "Description for Folder 3"))
    }

    override suspend fun add(folder: Folder): Unit = withContext(Dispatchers.IO) {
        Log.d("MockFolderRepository", "Adding folder: $folder")
        folderDatabase.add(folder)
        notifier.notifyChanged()
    }

    override suspend fun delete(folderId: Int): Unit = withContext(Dispatchers.IO) {
        val folder = folderDatabase.find { it.id == folderId }
        if (folder != null) {
            folderDatabase.remove(folder)
            notifier.notifyChanged()
        }
    }

    override suspend fun update(folder: Folder): Unit = withContext(Dispatchers.IO) {
        TODO("Not yet implemented")
    }

    override fun getAllFolderStream(): Flow<List<Folder>> = callbackFlow {
        val observer = object : ChangeObserver {
            override fun onChanged() {
                trySend(folderDatabase.toList())
            }
        }
        notifier.register(observer)
        trySend(folderDatabase.toList())
        awaitClose {
            notifier.remove(observer)
        }
    }

    override fun getFolderStream(id: Int): Flow<Folder?> = flow {
        val folder = folderDatabase.find { it.id == id }
        emit(folder)
    }

    override fun getFolderStream(title: String): Flow<Folder?> {
        val folder = folderDatabase.find { it.name == title }
        return flow { emit(folder) }
    }
}
