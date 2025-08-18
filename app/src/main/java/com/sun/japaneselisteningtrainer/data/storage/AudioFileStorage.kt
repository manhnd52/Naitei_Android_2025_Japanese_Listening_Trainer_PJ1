package com.sun.japaneselisteningtrainer.data.storage

import android.net.Uri
import java.io.File

interface AudioFileStorage {
    /**
     * Save audio file from from [inputUri] into storage.
     * Returns the URI to access the saved file.
     * **Should save the URI to the database for later retrieval.**
     */
    suspend fun save(inputUri: Uri): Uri

    /**
     * Return [File] for an [uri] pointing to a stored file if it exists.
     */
    suspend fun get(uri: Uri): File?

    /**
     * Replace content of an existing file has [uri] with data from [inputUri].
     * Return true if successful.
     * Return false if file does not exist.
     */
    suspend fun update(uri: Uri, inputUri: Uri) : Boolean

    /**
     * Delete stored file has [uri].
     */
    suspend fun delete(uri: Uri): Boolean
}
