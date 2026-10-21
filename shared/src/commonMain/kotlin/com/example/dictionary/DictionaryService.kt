package com.example.dictionary

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.HttpRedirect
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

class DictionaryService {
    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
        install(HttpRedirect)
        install(HttpTimeout) {
            requestTimeoutMillis = 10000 // Increased to 10 seconds
            connectTimeoutMillis = 10000
        }
        defaultRequest {
            header(HttpHeaders.UserAgent, "Mozilla/5.0")
        }
    }

    suspend fun getDefinition(word: String): List<WordResponse>? {
        return try {
            val url = "https://api.dictionaryapi.dev/api/v2/entries/en/$word"
            val response = client.get(url)

            if (response.status.isSuccess()) {
                val wordArray: Array<WordResponse> = response.body()
                wordArray.toList()
            } else {
                null
            }
        } catch (e: Exception) {
            println("API EXCEPTION: ${e.message}")
            null
        }
    }
}