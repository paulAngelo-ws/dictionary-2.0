package com.example.dictionary.domain

import kotlinx.coroutines.flow.Flow

class HistoryUseCase(private val repository: DictionaryRepository) {
    fun observeHistory(): Flow<List<HistoryItem>> = repository.observeHistory()

    suspend fun toggleFavorite(word: String) {
        repository.toggleFavorite(word)
    }
}
