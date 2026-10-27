package com.example.dictionary.presentation

import com.example.dictionary.domain.DictionaryEntry

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