package com.example.dictionary.domain

import kotlinx.coroutines.flow.Flow

interface PreferencesRepository {
    fun observeShowSlang(): Flow<Boolean>
    suspend fun saveShowSlang(enabled: Boolean)
}
