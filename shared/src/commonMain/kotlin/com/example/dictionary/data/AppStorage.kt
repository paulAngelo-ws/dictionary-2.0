package com.example.dictionary.data

import com.example.dictionary.data.local.DictionaryDatabase

// Each platform creates the database and passes it to the shared app.
class AppStorage(val database: DictionaryDatabase)
