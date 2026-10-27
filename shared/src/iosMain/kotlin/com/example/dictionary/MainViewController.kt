package com.example.dictionary

import androidx.compose.ui.window.ComposeUIViewController
import com.example.dictionary.data.createAppStorage

private val storage by lazy { createAppStorage() }

fun MainViewController() = ComposeUIViewController { App(storage) }
