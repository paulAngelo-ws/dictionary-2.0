package com.example.dictionary.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import com.example.dictionary.domain.PreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Module 9: DataStore is for a small setting; definitions belong in Room.
class DictionaryPreferences(private val dataStore: DataStore<Preferences>) : PreferencesRepository {
    private val showSlangKey = booleanPreferencesKey("show_slang")

    override fun observeShowSlang(): Flow<Boolean> {
        return dataStore.data.map { preferences -> preferences[showSlangKey] ?: false }
    }

    override suspend fun saveShowSlang(enabled: Boolean) {
        dataStore.edit { preferences -> preferences[showSlangKey] = enabled }
    }
}
