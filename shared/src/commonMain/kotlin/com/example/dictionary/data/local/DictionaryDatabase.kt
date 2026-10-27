package com.example.dictionary.data.local

import androidx.room3.ConstructedBy
import androidx.room3.Database
import androidx.room3.RoomDatabase
import androidx.room3.RoomDatabaseConstructor

@Database(entities = [HistoryEntity::class], version = 1)
@ConstructedBy(DictionaryDatabaseConstructor::class)
abstract class DictionaryDatabase : RoomDatabase() {
    abstract fun historyDao(): HistoryDao
}

// Room generates the actual implementation for each target.
@Suppress("KotlinNoActualForExpect")
expect object DictionaryDatabaseConstructor : RoomDatabaseConstructor<DictionaryDatabase> {
    override fun initialize(): DictionaryDatabase
}
