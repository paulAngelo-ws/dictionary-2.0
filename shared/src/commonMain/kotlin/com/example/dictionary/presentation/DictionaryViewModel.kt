package com.example.dictionary.presentation

import com.example.dictionary.data.DictionaryRepository
import com.example.dictionary.data.DictionaryRepositoryImpl
import com.example.dictionary.domain.DictionaryEntry
import com.example.dictionary.domain.HistoryItem
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

    // History list sorted with favorites on top
    private val _searchHistory = MutableStateFlow<List<HistoryItem>>(emptyList())
    val searchHistory: StateFlow<List<HistoryItem>> = _searchHistory

    fun searchWord(word: String, bypassApiIfCached: Boolean = true) {
        if (word.isBlank()) return
        val sanitized = word.trim().lowercase()

        // Check if already in history to load cached results instantly without API calls
        if (bypassApiIfCached) {
            val existing = _searchHistory.value.find { it.word.lowercase() == sanitized }
            if (existing != null) {
                _uiState.value = UiState.Success(existing.formalEntries, existing.urbanEntries)
                return
            }
        }

        scope.launch {
            _uiState.value = UiState.Loading
            try {
                val (formal, urban) = repository.fetchDefinitions(sanitized)
                if (formal.isEmpty() && urban.isEmpty()) {
                    _uiState.value = UiState.Error("No definitions found for '$sanitized'.")
                } else {
                    _uiState.value = UiState.Success(formalEntries = formal, urbanEntries = urban)

                    val snippet = formal.firstOrNull()?.definition ?: urban.firstOrNull()?.definition ?: "No definition snippet."

                    val updatedList = _searchHistory.value.toMutableList()
                    val index = updatedList.indexOfFirst { it.word.lowercase() == sanitized }

                    if (index >= 0) {
                        // Update existing entry data while preserving favorite status
                        val old = updatedList[index]
                        updatedList[index] = old.copy(formalEntries = formal, urbanEntries = urban, snippet = snippet)
                    } else {
                        // Add new entry
                        updatedList.add(0, HistoryItem(word = sanitized, snippet = snippet, formalEntries = formal, urbanEntries = urban))
                    }

                    _searchHistory.value = sortHistory(updatedList)
                }
            } catch (e: Exception) {
                _uiState.value = UiState.Error("Network error: ${e.message}")
            }
        }
    }

    fun toggleFavorite(word: String) {
        val updatedList = _searchHistory.value.map { item ->
            if (item.word.lowercase() == word.lowercase()) {
                item.copy(isFavorite = !item.isFavorite)
            } else {
                item
            }
        }
        _searchHistory.value = sortHistory(updatedList)
    }

    private fun sortHistory(list: List<HistoryItem>): List<HistoryItem> {
        // Favorites first, then ordinary history
        return list.sortedWith(compareByDescending<HistoryItem> { it.isFavorite })
    }
}