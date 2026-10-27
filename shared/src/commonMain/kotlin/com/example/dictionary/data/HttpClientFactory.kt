package com.example.dictionary.data

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

// Module 6: each platform chooses its own networking engine.
expect fun createHttpClient(): HttpClient

// Module 9: the plugins are configured once in shared code.
fun HttpClientConfig<*>.configureDictionaryClient() {
    install(ContentNegotiation) {
        json(Json { ignoreUnknownKeys = true })
    }
    install(Logging) {
        level = LogLevel.INFO
        // The other API's URL contains a key, so log only key-free requests.
        filter { request -> request.url.host == "api.urbandictionary.com" }
    }
    install(HttpTimeout) {
        requestTimeoutMillis = 10000
        connectTimeoutMillis = 10000
    }
}
