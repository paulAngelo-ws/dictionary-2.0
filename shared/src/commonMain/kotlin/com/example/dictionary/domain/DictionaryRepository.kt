package com.example.dictionary.domain

import kotlinx.coroutines.flow.Flow

// Module 6: presentation depends on an interface, not Ktor or Room.
interface DictionaryRepository {
    suspend fun fetchDefinitions(word: String, forceRefresh: Boolean = false): DictionaryResult?
    fun observeHistory(): Flow<List<HistoryItem>>
    suspend fun toggleFavorite(word: String)
}
