package com.example.dictionary.domain

class SearchWordUseCase(private val repository: DictionaryRepository) {
    suspend operator fun invoke(word: String, forceRefresh: Boolean = false): DictionaryResult? {
        return repository.fetchDefinitions(word.trim().lowercase(), forceRefresh)
    }
}
