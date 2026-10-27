package com.example.dictionary.data.local

import androidx.room3.Entity
import androidx.room3.PrimaryKey

// Module 9: one saved search per word. Room stores this in SQLite.
@Entity(tableName = "search_history")
data class HistoryEntity(
    @PrimaryKey val word: String,
    val snippet: String,
    val resultJson: String,
    val isFavorite: Boolean = false,
    val searchOrder: Long = 0
)
