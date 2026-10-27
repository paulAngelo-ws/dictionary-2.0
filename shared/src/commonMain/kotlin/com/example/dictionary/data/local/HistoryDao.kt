package com.example.dictionary.data.local

import androidx.room3.Dao
import androidx.room3.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface HistoryDao {
    @Query("SELECT * FROM search_history ORDER BY isFavorite DESC, searchOrder DESC")
    fun observeHistory(): Flow<List<HistoryEntity>>

    @Query("SELECT * FROM search_history WHERE word = :word")
    suspend fun findWord(word: String): HistoryEntity?

    // Updating a definition keeps the user's existing favorite flag.
    @Query("""
        INSERT INTO search_history (word, snippet, resultJson, isFavorite, searchOrder)
        VALUES (:word, :snippet, :resultJson, 0,
            (SELECT COALESCE(MAX(searchOrder), 0) + 1 FROM search_history))
        ON CONFLICT(word) DO UPDATE SET
            snippet = excluded.snippet,
            resultJson = excluded.resultJson,
            searchOrder = excluded.searchOrder
    """)
    suspend fun saveWord(word: String, snippet: String, resultJson: String)

    @Query("UPDATE search_history SET isFavorite = NOT isFavorite WHERE word = :word")
    suspend fun toggleFavorite(word: String)
}
