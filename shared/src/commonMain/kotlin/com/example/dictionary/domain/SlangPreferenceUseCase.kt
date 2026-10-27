package com.example.dictionary.domain

import kotlinx.coroutines.flow.Flow

class SlangPreferenceUseCase(private val preferences: PreferencesRepository) {
    fun observeShowSlang(): Flow<Boolean> = preferences.observeShowSlang()

    suspend fun saveShowSlang(enabled: Boolean) {
        preferences.saveShowSlang(enabled)
    }
}
