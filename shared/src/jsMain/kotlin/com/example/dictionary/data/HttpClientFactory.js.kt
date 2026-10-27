package com.example.dictionary.data

import io.ktor.client.HttpClient
import io.ktor.client.engine.js.Js

actual fun createHttpClient(): HttpClient {
    return HttpClient(Js) { configureDictionaryClient() }
}
