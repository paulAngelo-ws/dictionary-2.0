package com.example.dictionary

import com.example.dictionary.data.createAppStorage
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    val storage = createAppStorage()
    ComposeViewport {
        App(storage)
    }
}