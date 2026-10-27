package com.example.dictionary.presentation

import com.example.dictionary.domain.DictionaryResult
import com.example.dictionary.domain.HistoryItem
import com.example.dictionary.domain.HistoryUseCase
import com.example.dictionary.domain.SearchWordUseCase
import com.example.dictionary.domain.SlangPreferenceUseCase
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
    private val searchWordUseCase: SearchWordUseCase,
    private val historyUseCase: HistoryUseCase,
    private val slangPreferenceUseCase: SlangPreferenceUseCase,
    private val scope: CoroutineScope
) {
    private var searchJob: Job? = null
    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
    val uiState: StateFlow<UiState> = _uiState

    private val _searchHistory = MutableStateFlow<List<HistoryItem>>(emptyList())
    val searchHistory: StateFlow<List<HistoryItem>> = _searchHistory

    private val _showSlang = MutableStateFlow(false)
    val showSlang: StateFlow<Boolean> = _showSlang

    // Module 7: messages happen once; screen values belong in StateFlow.
    private val _events = MutableSharedFlow<String>()
    val events: SharedFlow<String> = _events

    init {
        scope.launch {
            try {
                historyUseCase.observeHistory().collect { _searchHistory.value = it }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _events.emit("Could not read saved history.")
            }
        }
        scope.launch {
            try {
                slangPreferenceUseCase.observeShowSlang().collect { _showSlang.value = it }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _events.emit("Could not read your saved preference.")
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
                val result = searchWordUseCase(word, forceRefresh)
                if (result == null) {
                    if (forceRefresh && previousState is UiState.Success) {
                        _uiState.value = previousState
                        _events.emit("Could not refresh. Showing the saved definition.")
                    } else {
                        _uiState.value = UiState.Error("Could not search. Check your connection and try again.")
                    }
                } else {
                    showResult(word, result)
                    // Module 9: display cached data first, then refresh in the background.
                    if (result.isCached) {
                        try {
                            val fresh = searchWordUseCase(word, forceRefresh = true)
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
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.value = UiState.Error("Could not load or save this word. Please try again.")
            }
        }
    }

    private fun hasDefinitions(result: DictionaryResult): Boolean {
        return result.formalEntries.isNotEmpty() || result.urbanEntries.isNotEmpty()
    }

    private fun showResult(word: String, result: DictionaryResult) {
        _uiState.value = if (!hasDefinitions(result) && result.didYouMeanSuggestion == null) {
            UiState.Error("No definitions found for '${word.trim()}'.")
        } else {
            UiState.Success(result.formalEntries, result.urbanEntries, result.didYouMeanSuggestion)
        }
    }

    fun toggleFavorite(word: String) {
        scope.launch {
            try {
                historyUseCase.toggleFavorite(word)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _events.emit("Could not save this favorite.")
            }
        }
    }

    fun saveShowSlang(enabled: Boolean) {
        scope.launch {
            try {
                slangPreferenceUseCase.saveShowSlang(enabled)
                _events.emit("Preference saved.")
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _events.emit("Could not save your preference.")
            }
        }
    }
}
