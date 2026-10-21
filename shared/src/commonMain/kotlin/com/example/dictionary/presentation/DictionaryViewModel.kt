package com.example.dictionary.presentation

import com.example.dictionary.data.DictionaryRepository
import com.example.dictionary.data.DictionaryRepositoryImpl
import com.example.dictionary.domain.DictionaryEntry
import com.example.dictionary.domain.UiState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class DictionaryViewModel(
    private val repository: DictionaryRepository = DictionaryRepositoryImpl()
) {
    private val scope = CoroutineScope(Dispatchers.Main)

    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
    val uiState: StateFlow<UiState> = _uiState

    // Simple in-memory search history to satisfy multi-screen requirement
    private val _searchHistory = MutableStateFlow<List<String>>(emptyList())
    val searchHistory: StateFlow<List<String>> = _searchHistory

    fun searchWord(word: String) {
        if (word.isBlank()) return
        val sanitized = word.trim().lowercase()

        scope.launch {
            _uiState.value = UiState.Loading
            try {
                val (formal, urban) = repository.fetchDefinitions(sanitized)
                if (formal.isEmpty() && urban.isEmpty()) {
                    _uiState.value = UiState.Error("No definitions found for '$sanitized'.")
                } else {
                    _uiState.value = UiState.Success(formalEntries = formal, urbanEntries = urban)
                    if (!_searchHistory.value.contains(sanitized)) {
                        _searchHistory.value = _searchHistory.value + sanitized
                    }
                }
            } catch (e: Exception) {
                _uiState.value = UiState.Error("Network error: ${e.message}")
            }
        }
    }
}