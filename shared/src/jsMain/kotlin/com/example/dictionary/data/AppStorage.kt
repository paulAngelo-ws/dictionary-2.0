package com.example.dictionary.data

import androidx.datastore.core.DataStoreFactory
import androidx.datastore.core.okio.WebLocalStorage
import androidx.datastore.preferences.core.PreferencesSerializer
import androidx.room3.Room
import com.example.dictionary.data.local.DictionaryDatabase
import com.example.dictionary.browser.createBrowserDriver

fun createAppStorage(): AppStorage {
    val database = Room.databaseBuilder<DictionaryDatabase>(name = "dictionary.db")
        .setDriver(createBrowserDriver()).build()
    val preferences = DataStoreFactory.create(
        storage = WebLocalStorage(
            serializer = PreferencesSerializer,
            name = "dictionary.preferences"
        )
    )
    return AppStorage(database, preferences)
}
