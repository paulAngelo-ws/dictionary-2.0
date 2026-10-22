package com.example.dictionary

import androidx.compose.animation.AnimatedVisibility
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
import com.example.dictionary.domain.HistoryItem
import com.example.dictionary.domain.UiState
import com.example.dictionary.presentation.DictionaryViewModel

enum class AppTab(val title: String) {
    INPUT("Search"),
    RESULT("Result"),
    HISTORY("History")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App() {
    val viewModel = remember { DictionaryViewModel() }

    var currentTab by remember { mutableStateOf(AppTab.INPUT) }
    var searchQuery by remember { mutableStateOf("") }
    var activeWord by remember { mutableStateOf("") }

    val state by viewModel.uiState.collectAsState()
    val history by viewModel.searchHistory.collectAsState()

    MaterialTheme {
        Scaffold(
            bottomBar = {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ) {
                    NavigationBarItem(
                        icon = { Text("🏠", style = MaterialTheme.typography.titleMedium) },
                        label = { Text("Input Box") },
                        selected = currentTab == AppTab.INPUT,
                        onClick = { currentTab = AppTab.INPUT }
                    )
                    NavigationBarItem(
                        icon = { Text("📖", style = MaterialTheme.typography.titleMedium) },
                        label = { Text("Result") },
                        selected = currentTab == AppTab.RESULT,
                        onClick = { currentTab = AppTab.RESULT }
                    )
                    NavigationBarItem(
                        icon = { Text("🕒", style = MaterialTheme.typography.titleMedium) },
                        label = { Text("History") },
                        selected = currentTab == AppTab.HISTORY,
                        onClick = { currentTab = AppTab.HISTORY }
                    )
                }
            }
        ) { innerPadding ->
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                when (currentTab) {
                    AppTab.INPUT -> InputScreen(
                        query = searchQuery,
                        onQueryChange = { searchQuery = it },
                        onSearchClick = { word ->
                            activeWord = word
                            viewModel.searchWord(word)
                            currentTab = AppTab.RESULT
                        }
                    )
                    AppTab.RESULT -> ResultScreen(
                        word = activeWord,
                        state = state
                    )
                    AppTab.HISTORY -> HistoryScreen(
                        history = history,
                        onSelectWord = { word ->
                            activeWord = word
                            viewModel.searchWord(word, bypassApiIfCached = true)
                            currentTab = AppTab.RESULT
                        },
                        onToggleFavorite = { word ->
                            viewModel.toggleFavorite(word)
                        }
                    )
                }
            }
        }
    }
}

// --- PAGE 1: INPUT BOX SCREEN ---
@Composable
fun InputScreen(query: String, onQueryChange: (String) -> Unit, onSearchClick: (String) -> Unit) {
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier.fillMaxSize().padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("📚 KMP Dictionary", style = MaterialTheme.typography.headlineLarge, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Merriam-Webster & Urban Dictionary", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.outline)

            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = query,
                onValueChange = onQueryChange,
                label = { Text("Type any word...") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = MaterialTheme.shapes.medium
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { if (query.isNotBlank()) onSearchClick(query) },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = MaterialTheme.shapes.medium
            ) {
                Text("Search Definition", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}

// --- PAGE 2: RESULT SCREEN ---
@Composable
fun ResultScreen(word: String, state: UiState) {
    var showUrbanSlang by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        if (word.isNotBlank()) {
            Text("Results for: '$word'", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(4.dp))
            Divider()
        }

        Spacer(modifier = Modifier.height(12.dp))

        Box(modifier = Modifier.fillMaxSize()) {
            when (state) {
                is UiState.Loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                is UiState.Error -> Text(text = state.message, color = MaterialTheme.colorScheme.error, modifier = Modifier.align(Alignment.Center))
                is UiState.Success -> {
                    LazyColumn(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(8.dp)) {

                        if (state.formalEntries.isNotEmpty()) {
                            item {
                                Text("📖 Formal Definitions", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.secondary)
                            }
                            items(state.formalEntries) { entry ->
                                EntryCard(entry)
                            }
                        }

                        if (state.urbanEntries.isNotEmpty()) {
                            item {
                                Spacer(modifier = Modifier.height(12.dp))
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f)),
                                    modifier = Modifier.fillMaxWidth().clickable { showUrbanSlang = !showUrbanSlang },
                                    shape = MaterialTheme.shapes.medium
                                ) {
                                    Row(
                                        modifier = Modifier.padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("⚠️", style = MaterialTheme.typography.titleLarge)
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text("Urban Slang Available", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onErrorContainer)
                                            Text(
                                                if (showUrbanSlang) "Click to hide internet definitions" else "Viewer Discretion: May contain explicit terms.",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.outline
                                            )
                                        }
                                        Text(if (showUrbanSlang) "▲ Hide" else "▼ Show", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                                    }
                                }
                            }

                            item {
                                Column {
                                    AnimatedVisibility(visible = showUrbanSlang) {
                                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text("🔥 Internet & Slang Definitions", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.tertiary)
                                            state.urbanEntries.forEach { entry ->
                                                EntryCard(entry)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                is UiState.Idle -> Text("No active search. Head to the Input Box tab to look up a word.", modifier = Modifier.align(Alignment.Center))
            }
        }
    }
}

@Composable
fun EntryCard(entry: DictionaryEntry) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = MaterialTheme.shapes.small
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(entry.source, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                if (entry.partOfSpeech.isNotEmpty()) {
                    Text(" • (${entry.partOfSpeech})", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(entry.definition, style = MaterialTheme.typography.bodyMedium)
            if (entry.example.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text("Ex: \"${entry.example}\"", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
            }
        }
    }
}

// --- PAGE 3: HISTORY SCREEN WITH SNIPPETS & FAVORITES ---
@Composable
fun HistoryScreen(
    history: List<HistoryItem>,
    onSelectWord: (String) -> Unit,
    onToggleFavorite: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("🕒 Search History & Favorites", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(4.dp))
        Divider()
        Spacer(modifier = Modifier.height(12.dp))

        if (history.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No past lookups recorded yet.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.outline)
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(history) { item ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectWord(item.word) },
                        colors = CardDefaults.cardColors(
                            containerColor = if (item.isFavorite)
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                            else
                                MaterialTheme.colorScheme.surfaceVariant
                        ),
                        shape = MaterialTheme.shapes.small
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp).fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(item.word, style = MaterialTheme.typography.titleMedium)
                                    if (item.isFavorite) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("⭐", style = MaterialTheme.typography.bodySmall)
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = item.snippet,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.outline,
                                    maxLines = 2
                                )
                            }

                            IconButton(onClick = { onToggleFavorite(item.word) }) {
                                Text(if (item.isFavorite) "⭐" else "☆", style = MaterialTheme.typography.titleLarge)
                            }
                        }
                    }
                }
            }
        }
    }
}