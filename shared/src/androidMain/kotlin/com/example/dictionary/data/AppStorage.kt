package com.example.dictionary.data

import android.content.Context
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.core.FileStorage
import androidx.datastore.preferences.core.PreferencesFileSerializer
import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.example.dictionary.data.local.DictionaryDatabase

private var appStorage: AppStorage? = null

fun createAppStorage(context: Context): AppStorage {
    appStorage?.let { return it }
    val appContext = context.applicationContext
    val database = Room.databaseBuilder<DictionaryDatabase>(
        context = appContext,
        name = appContext.getDatabasePath("dictionary.db").absolutePath
    ).setDriver(BundledSQLiteDriver()).build()
    val preferences = DataStoreFactory.create(
        storage = FileStorage(
            serializer = PreferencesFileSerializer,
            produceFile = { appContext.filesDir.resolve("dictionary.preferences_pb") }
        )
    )
    val storage = AppStorage(database, preferences)
    appStorage = storage
    return storage
}
