package com.example.dictionary.data

import androidx.room3.Room
import com.example.dictionary.data.local.DictionaryDatabase
import com.example.dictionary.browser.createBrowserDriver

fun createAppStorage(): AppStorage {
    val database = Room.databaseBuilder<DictionaryDatabase>(name = "dictionary.db")
        .setDriver(createBrowserDriver()).build()
    return AppStorage(database)
}
