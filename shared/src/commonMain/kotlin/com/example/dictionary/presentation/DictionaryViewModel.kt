package com.example.dictionary.presentation

import com.example.dictionary.domain.DictionaryResult
import com.example.dictionary.domain.HistoryItem
import com.example.dictionary.domain.DictionaryRepository
import com.example.dictionary.domain.DictionaryEntry
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// Module 7: this state holder uses plain Kotlin and Flow on every platform.
class DictionaryViewModel(
    private val repository: DictionaryRepository,
    private val scope: CoroutineScope
) {
    private var searchJob: Job? = null
    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
    val uiState: StateFlow<UiState> = _uiState

    private val _searchHistory = MutableStateFlow<List<HistoryItem>>(emptyList())
    val searchHistory: StateFlow<List<HistoryItem>> = _searchHistory

    // Module 7: messages happen once; screen values belong in StateFlow.
    private val _events = MutableSharedFlow<String>()
    val events: SharedFlow<String> = _events

    init {
        scope.launch {
            try {
                repository.observeHistory().collect { _searchHistory.value = it }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _events.emit("Could not read saved history.")
            }
        }
    }

    fun searchWord(word: String, forceRefresh: Boolean = false) {
        if (word.isBlank()) return
        searchJob?.cancel()
        searchJob = scope.launch {
            val previousState = _uiState.value
            _uiState.value = UiState.Loading
            try {
                val result = repository.fetchDefinitions(word.trim().lowercase(), forceRefresh)
                if (result == null) {
                    if (forceRefresh && previousState is UiState.Success) {
                        _uiState.value = previousState
                        _events.emit("Could not refresh. Showing the saved definition.")
                    } else {
                        _uiState.value = UiState.Error("Could not search. Check your connection and try again.")
                    }
                } else {
                    showResult(word, result)
                    if (result.isCached) refreshCachedWord(word)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.value = UiState.Error("Could not load or save this word. Please try again.")
            }
        }
    }

    // Keep saved definitions visible while checking for an update.
    private suspend fun refreshCachedWord(word: String) {
        try {
            val fresh = repository.fetchDefinitions(word.trim().lowercase(), forceRefresh = true)
            if (fresh != null && hasDefinitions(fresh)) {
                showResult(word, fresh)
            } else {
                _events.emit("Showing saved definitions. A refresh is unavailable.")
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            _events.emit("Showing saved definitions. A refresh is unavailable.")
        }
    }

    private fun hasDefinitions(result: DictionaryResult): Boolean {
        return result.formalEntries.isNotEmpty()
    }

    private fun showResult(word: String, result: DictionaryResult) {
        _uiState.value = if (!hasDefinitions(result) && result.didYouMeanSuggestion == null) {
            UiState.Error("No definitions found for '${word.trim()}'.")
        } else {
            UiState.Success(result.formalEntries, result.didYouMeanSuggestion)
        }
    }

    fun toggleFavorite(word: String) {
        scope.launch {
            try {
                repository.toggleFavorite(word)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _events.emit("Could not save this favorite.")
            }
        }
    }

}

// The states that ResultScreen can display.
sealed class UiState {
    object Idle : UiState()
    object Loading : UiState()
    data class Success(
        val formalEntries: List<DictionaryEntry>,
        val didYouMeanSuggestion: String? = null
    ) : UiState()
    data class Error(val message: String) : UiState()
}
