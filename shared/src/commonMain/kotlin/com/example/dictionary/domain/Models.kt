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
    val isFavorite: Boolean = false
)

// A named result is easier to understand than Triple.first/second/third.
@Serializable
data class DictionaryResult(
    val formalEntries: List<DictionaryEntry>,
    val didYouMeanSuggestion: String? = null,
    val isCached: Boolean = false
)
