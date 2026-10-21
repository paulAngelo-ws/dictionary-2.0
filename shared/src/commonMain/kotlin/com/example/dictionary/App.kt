package com.example.dictionary

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.dictionary.domain.DictionaryEntry
import com.example.dictionary.domain.UiState
import com.example.dictionary.presentation.DictionaryViewModel

// Simple screen enumeration to handle multi-screen navigation cleanly in KMP Desktop
enum class Screen {
    HOME,
    RESULTS,
    HISTORY
}

@Composable
fun App() {
    val viewModel = remember { DictionaryViewModel() }

    var currentScreen by remember { mutableStateOf(Screen.HOME) }
    var selectedWord by remember { mutableStateOf("") }

    val state by viewModel.uiState.collectAsState()
    val history by viewModel.searchHistory.collectAsState()

    MaterialTheme {
        Box(modifier = Modifier.fillMaxSize()) {
            when (currentScreen) {
                Screen.HOME -> HomeScreen(
                    onSearch = { word ->
                        selectedWord = word
                        viewModel.searchWord(word)
                        currentScreen = Screen.RESULTS
                    },
                    onOpenHistory = {
                        currentScreen = Screen.HISTORY
                    }
                )
                Screen.RESULTS -> ResultsScreen(
                    word = selectedWord,
                    state = state,
                    onBack = { currentScreen = Screen.HOME }
                )
                Screen.HISTORY -> HistoryScreen(
                    history = history,
                    onSelectWord = { word ->
                        selectedWord = word
                        viewModel.searchWord(word)
                        currentScreen = Screen.RESULTS
                    },
                    onBack = { currentScreen = Screen.HOME }
                )
            }
        }
    }
}

// --- SCREEN 1: HOME ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(onSearch: (String) -> Unit, onOpenHistory: () -> Unit) {
    var text by remember { mutableStateOf("") }

    Scaffold(
        topBar = { TopAppBar(title = { Text("KMP Multi-Dictionary (Exam Edition)") }) }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                label = { Text("Enter a word to lookup") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = { if (text.isNotBlank()) onSearch(text) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Search Dual Dictionaries")
            }
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(
                onClick = onOpenHistory,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("View Search History")
            }
        }
    }
}

// --- SCREEN 2: RESULTS ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultsScreen(word: String, state: UiState, onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Results for: $word") },
                navigationIcon = {
                    TextButton(onClick = onBack) { Text("< Back") }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            when (state) {
                is UiState.Loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                is UiState.Error -> Text(text = state.message, color = MaterialTheme.colorScheme.error, modifier = Modifier.align(Alignment.Center))
                is UiState.Success -> {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        if (state.formalEntries.isNotEmpty()) {
                            item { Text("Merriam-Webster Formal Definitions", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary) }
                            items(state.formalEntries) { EntryCard(it) }
                        }
                        if (state.urbanEntries.isNotEmpty()) {
                            item {
                                Spacer(modifier = Modifier.height(16.dp))
                                Text("Urban Dictionary Slang Definitions", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.secondary)
                            }
                            items(state.urbanEntries) { EntryCard(it) }
                        }
                    }
                }
                is UiState.Idle -> Text("Type a word to begin.", modifier = Modifier.align(Alignment.Center))
            }
        }
    }
}

@Composable
fun EntryCard(entry: DictionaryEntry) {
    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Column(modifier = Modifier.padding(12.dp)) {
            if (entry.partOfSpeech.isNotEmpty()) {
                Text("(${entry.partOfSpeech})", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.tertiary)
            }
            Text(entry.definition, style = MaterialTheme.typography.bodyMedium)
            if (entry.example.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text("Ex: \"${entry.example}\"", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

// --- SCREEN 3: HISTORY ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(history: List<String>, onSelectWord: (String) -> Unit, onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Search History") },
                navigationIcon = { TextButton(onClick = onBack) { Text("< Back") } }
            )
        }
    ) { padding ->
        if (history.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("No search history recorded yet.")
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
                items(history) { word ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable { onSelectWord(word) }
                    ) {
                        Text(word, modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
        }
    }
}