package com.example.dictionary.domain

import kotlinx.serialization.Serializable

@Serializable
data class DictionaryEntry(
    val word: String,
    val source: String, // "Merriam-Webster" or "Urban Dictionary"
    val partOfSpeech: String = "",
    val definition: String,
    val example: String = ""
)

sealed class UiState {
    object Idle : UiState()
    object Loading : UiState()
    data class Success(
        val formalEntries: List<DictionaryEntry>,
        val urbanEntries: List<DictionaryEntry>
    ) : UiState()
    data class Error(val message: String) : UiState()
}