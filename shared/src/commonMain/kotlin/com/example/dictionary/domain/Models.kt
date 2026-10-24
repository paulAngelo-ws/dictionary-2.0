package com.example.dictionary.domain

import kotlinx.serialization.Serializable

@Serializable
data class DictionaryEntry(
    val word: String,
    val source: String,
    val partOfSpeech: String = "",
    val definition: String,
    val example: String = ""
)

data class HistoryItem(
    val word: String,
    val snippet: String,
    val formalEntries: List<DictionaryEntry>,
    val urbanEntries: List<DictionaryEntry>,
    val isFavorite: Boolean = false
)

sealed class UiState {
    object Idle : UiState()
    object Loading : UiState()
    data class Success(
        val formalEntries: List<DictionaryEntry>,
        val urbanEntries: List<DictionaryEntry>,
        val didYouMeanSuggestion: String? = null
    ) : UiState()
    data class Error(val message: String) : UiState()
}