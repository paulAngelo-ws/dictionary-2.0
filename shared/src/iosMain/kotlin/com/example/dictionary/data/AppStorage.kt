package com.example.dictionary.data

import androidx.datastore.core.DataStoreFactory
import androidx.datastore.core.okio.OkioStorage
import androidx.datastore.preferences.core.PreferencesSerializer
import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.example.dictionary.data.local.DictionaryDatabase
import kotlinx.cinterop.ExperimentalForeignApi
import okio.FileSystem
import okio.Path.Companion.toPath
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

@OptIn(ExperimentalForeignApi::class)
fun createAppStorage(): AppStorage {
    val directory = NSFileManager.defaultManager.URLForDirectory(
        NSDocumentDirectory, NSUserDomainMask, null, true, null
    )
    val path = requireNotNull(directory?.path)
    val database = Room.databaseBuilder<DictionaryDatabase>(name = "$path/dictionary.db")
        .setDriver(BundledSQLiteDriver()).build()
    val preferences = DataStoreFactory.create(
        storage = OkioStorage(
            fileSystem = FileSystem.SYSTEM,
            serializer = PreferencesSerializer,
            producePath = { "$path/dictionary.preferences_pb".toPath() }
        )
    )
    return AppStorage(database, preferences)
}
