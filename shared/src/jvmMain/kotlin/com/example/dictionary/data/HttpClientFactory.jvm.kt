package com.example.dictionary.data

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO

actual fun createHttpClient(): HttpClient {
    return HttpClient(CIO) { configureDictionaryClient() }
}
