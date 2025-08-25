package com.sun.japaneselisteningtrainer.data.repository.local

import android.content.ContentValues
import android.provider.BaseColumns
import com.sun.japaneselisteningtrainer.data.folder.FolderRepository
import com.sun.japaneselisteningtrainer.data.model.Folder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LocalFolderRepository(dbHelper: JLTDbHelper, private val notifier: DbChangeNotifier) :
    FolderRepository {
    private val db = dbHelper.writableDatabase

    override suspend fun add(folder: Folder) = withContext(Dispatchers.IO) {
        val values = ContentValues().apply {
            put(JLTContract.Folder.COLUMN_NAME, folder.name)
            put(JLTContract.Folder.COLUMN_DESCRIPTION, folder.description)
            put(JLTContract.Folder.COLUMN_CREATED_AT, System.currentTimeMillis())
        }
        db.insert(JLTContract.Folder.TABLE_NAME, null, values)
        notifier.notifyChanged()
    }

    override suspend fun delete(folderId: Int) = withContext(Dispatchers.IO) {
        val selection = "${BaseColumns._ID} = ?"
        val selectionArgs = arrayOf(folderId.toString())
        db.delete(JLTContract.Folder.TABLE_NAME, selection, selectionArgs)
        notifier.notifyChanged()
    }

    override suspend fun update(folder: Folder) = withContext(Dispatchers.IO) {
        val values = ContentValues().apply {
            put(JLTContract.Folder.COLUMN_NAME, folder.name)
            put(JLTContract.Folder.COLUMN_DESCRIPTION, folder.description)
            put(JLTContract.Folder.COLUMN_CREATED_AT, folder.createdAt)
        }
        val selection = "${BaseColumns._ID} = ?"
        val selectionArgs = arrayOf(folder.id.toString())
        db.update(JLTContract.Folder.TABLE_NAME, values, selection, selectionArgs)
        notifier.notifyChanged()
    }

    override fun getAllFolderStream(): Flow<List<Folder>> = callbackFlow {
        suspend fun query(): List<Folder> = withContext(Dispatchers.IO) {
            val folderList = mutableListOf<Folder>()
            val query =
                "SELECT f.*, COUNT(a.${BaseColumns._ID}) AS audioCount FROM ${JLTContract.Folder.TABLE_NAME} f " +
                        "LEFT JOIN ${JLTContract.Audio.TABLE_NAME} a " +
                        "ON f.${BaseColumns._ID} = a.${JLTContract.Audio.COLUMN_FOLDER_ID} " +
                        "GROUP BY f.${BaseColumns._ID}"
            val cursor = db.rawQuery(query, null)
            if (cursor.moveToFirst()) {
                do {
                    val id = cursor.getInt(cursor.getColumnIndexOrThrow(BaseColumns._ID))
                    val name =
                        cursor.getString(cursor.getColumnIndexOrThrow(JLTContract.Folder.COLUMN_NAME))
                    val description =
                        cursor.getString(cursor.getColumnIndexOrThrow(JLTContract.Folder.COLUMN_DESCRIPTION))
                    val audioCount = cursor.getInt(cursor.getColumnIndexOrThrow("audioCount"))
                    val createdAt =
                        cursor.getLong(cursor.getColumnIndexOrThrow(JLTContract.Folder.COLUMN_CREATED_AT))
                    val folder = Folder(
                        id = id,
                        name = name,
                        description = description,
                        audioCount = audioCount,
                        createdAt = createdAt
                    )
                    folderList.add(folder)
                } while (cursor.moveToNext())
            }
            cursor.close()
            return@withContext folderList
        }

        val observer = object : ChangeObserver {
            override fun onChanged() {
                launch {
                    trySend(query()).isSuccess
                }
            }
        }

        notifier.register(observer)
        launch {
            trySend(query()).isSuccess
        }

        awaitClose {
            notifier.remove(observer)
        }
    }

    override fun getFolderStream(id: Int): Flow<Folder?> = callbackFlow {
        suspend fun query(): Folder? = withContext(Dispatchers.IO) {
            val query =
                "SELECT f.*, COUNT(a.${BaseColumns._ID}) audioCount FROM ${JLTContract.Folder.TABLE_NAME} f " +
                        "LEFT JOIN ${JLTContract.Audio.TABLE_NAME} a ON f.${BaseColumns._ID} = a.${JLTContract.Audio.COLUMN_FOLDER_ID} " +
                        "WHERE f.${BaseColumns._ID} = ? " +
                        "GROUP BY f.${BaseColumns._ID}"

            val cursor = db.rawQuery(query, arrayOf(id.toString()))
            if (cursor.moveToFirst()) {
                val name =
                    cursor.getString(cursor.getColumnIndexOrThrow(JLTContract.Folder.COLUMN_NAME))
                val description =
                    cursor.getString(cursor.getColumnIndexOrThrow(JLTContract.Folder.COLUMN_DESCRIPTION))
                val audioCount = cursor.getInt(cursor.getColumnIndexOrThrow("audioCount"))
                val createdAt =
                    cursor.getLong(cursor.getColumnIndexOrThrow(JLTContract.Folder.COLUMN_CREATED_AT))
                return@withContext Folder(id, name, description, audioCount, createdAt)
            }
            cursor.close()
            return@withContext null
        }

        val observer = object : ChangeObserver {
            override fun onChanged() {
                launch {
                    trySend(query()).isSuccess
                }
            }
        }

        notifier.register(observer)

        launch {
            trySend(query()).isSuccess
        }
        awaitClose {
            notifier.remove(observer)
        }
    }

    override fun getFolderStream(title: String): Flow<Folder?> = callbackFlow {
        suspend fun query(): Folder? = withContext(Dispatchers.IO) {
            val query = "SELECT f.*, COUNT(a.${BaseColumns._ID}) audioCount FROM ${JLTContract.Folder.TABLE_NAME} f " +
                        "LEFT JOIN ${JLTContract.Audio.TABLE_NAME} a ON f.${BaseColumns._ID} = a.${JLTContract.Audio.COLUMN_FOLDER_ID} " +
                        "WHERE ${JLTContract.Folder.COLUMN_NAME} = ? " +
                        "GROUP BY f.${BaseColumns._ID}"

            val cursor = db.rawQuery(query, arrayOf(title))
            if (cursor.moveToFirst()) {
                val id = cursor.getInt(cursor.getColumnIndexOrThrow(BaseColumns._ID))
                val name =
                    cursor.getString(cursor.getColumnIndexOrThrow(JLTContract.Folder.COLUMN_NAME))
                val description =
                    cursor.getString(cursor.getColumnIndexOrThrow(JLTContract.Folder.COLUMN_DESCRIPTION))
                val audioCount = cursor.getInt(cursor.getColumnIndexOrThrow("audioCount"))
                val createdAt =
                    cursor.getLong(cursor.getColumnIndexOrThrow(JLTContract.Folder.COLUMN_CREATED_AT))
                return@withContext Folder(id, name, description, audioCount, createdAt)
            }
            cursor.close()
            return@withContext null
        }

        val observer = object : ChangeObserver {
            override fun onChanged() {
                launch {
                    trySend(query()).isSuccess
                }
            }
        }

        notifier.register(observer)

        launch {
            trySend(query()).isSuccess
        }
        awaitClose {
            notifier.remove(observer)
        }
    }
}
