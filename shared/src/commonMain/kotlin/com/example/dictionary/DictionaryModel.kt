package com.example.dictionary

import kotlinx.serialization.Serializable

@Serializable
data class WordResponse(
    val word: String,
    val meanings: List<Meaning>
)

@Serializable
data class Meaning(
    val partOfSpeech: String,
    val definitions: List<Definition>
)

@Serializable
data class Definition(
    val definition: String
)