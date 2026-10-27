package com.example.dictionary

import com.example.dictionary.data.createAppStorage
import androidx.compose.runtime.remember
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() = application {
    val storage = remember { createAppStorage() }
    Window(
        onCloseRequest = ::exitApplication,
        title = "dictionary",
    ) {
        App(storage)
    }
}