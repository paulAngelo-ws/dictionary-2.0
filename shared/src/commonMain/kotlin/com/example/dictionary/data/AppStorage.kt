package com.example.dictionary.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.example.dictionary.data.local.DictionaryDatabase

// Platform entry points create these once and inject them into App.
class AppStorage(
    val database: DictionaryDatabase,
    val preferences: DataStore<Preferences>
)
