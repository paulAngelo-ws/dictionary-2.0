package com.example.dictionary.data

import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.example.dictionary.data.local.DictionaryDatabase
import java.io.File

fun createAppStorage(directory: File = File(System.getProperty("user.home"), ".kmp-dictionary")): AppStorage {
    directory.mkdirs()
    val database = Room.databaseBuilder<DictionaryDatabase>(
        name = File(directory, "dictionary.db").absolutePath
    ).setDriver(BundledSQLiteDriver()).build()
    return AppStorage(database)
}
