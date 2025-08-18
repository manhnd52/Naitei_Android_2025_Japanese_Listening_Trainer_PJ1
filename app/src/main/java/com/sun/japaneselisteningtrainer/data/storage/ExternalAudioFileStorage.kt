package com.sun.japaneselisteningtrainer.data.storage

import android.content.Context
import android.net.Uri
import android.os.Environment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID


class ExternalAudioFileStorage(private val context: Context) : AudioFileStorage {

    private val audioDir: File by lazy {
        context.getExternalFilesDir(Environment.DIRECTORY_MUSIC)!!.apply {
            if (!exists()) {
                mkdirs()
            }
        }
    }

    override suspend fun save(inputUri: Uri): Uri = withContext(Dispatchers.IO) {
        val filename = UUID.randomUUID().toString()
        val file = File(audioDir, filename)
        val inputStream = context.contentResolver.openInputStream(inputUri)
        inputStream.use { input -> file.outputStream().use { output -> input?.copyTo(output) } }
        return@withContext Uri.fromFile(file)
    }

    override suspend fun get(uri: Uri): File? = withContext(Dispatchers.IO) {
        val filename = uri.lastPathSegment ?: return@withContext null
        val file = File(audioDir, filename)
        return@withContext if (file.exists()) file else null
    }

    override suspend fun update(uri: Uri, inputUri: Uri): Boolean =
        withContext(Dispatchers.IO) {
            val fileName = uri.lastPathSegment ?: return@withContext false
            val file = File(audioDir, fileName)
            if (!file.exists()) return@withContext false
            val inputStream = context.contentResolver.openInputStream(inputUri)
            inputStream.use { input -> file.outputStream().use { output -> input?.copyTo(output) } }
            return@withContext true
        }

    override suspend fun delete(uri: Uri): Boolean = withContext(Dispatchers.IO) {
        val fileName = uri.lastPathSegment ?: return@withContext false
        val file = File(audioDir, fileName)
        if (!file.exists()) return@withContext false
        return@withContext file.delete()
    }
}
