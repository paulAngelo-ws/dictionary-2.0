package com.example.dictionary

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.dictionary.presentation.DictionaryViewModel
import com.example.dictionary.ui.HistoryScreen
import com.example.dictionary.ui.InputScreen
import com.example.dictionary.ui.ResultScreen

enum class AppTab(val title: String) {
    INPUT("SEARCH"),
    RESULT("RESULT"),
    HISTORY("HISTORY")
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

    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = Color.Black,
            onPrimary = Color.White,
            surface = Color.White,
            onSurface = Color.Black,
            surfaceVariant = Color(0xFFF4F4F6)
        )
    ) {
        Scaffold(
            bottomBar = {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color.White,
                    border = BorderStroke(1.dp, Color.Black)
                ) {
                    NavigationBar(
                        containerColor = Color.White,
                        tonalElevation = 0.dp
                    ) {
                        NavigationBarItem(
                            icon = { Text("01", fontWeight = FontWeight.Bold) },
                            label = { Text("INPUT") },
                            selected = currentTab == AppTab.INPUT,
                            onClick = { currentTab = AppTab.INPUT },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color.White,
                                selectedTextColor = Color.Black,
                                indicatorColor = Color.Black,
                                unselectedIconColor = Color.Gray,
                                unselectedTextColor = Color.Gray
                            )
                        )
                        NavigationBarItem(
                            icon = { Text("02", fontWeight = FontWeight.Bold) },
                            label = { Text("RESULT") },
                            selected = currentTab == AppTab.RESULT,
                            onClick = { currentTab = AppTab.RESULT },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color.White,
                                selectedTextColor = Color.Black,
                                indicatorColor = Color.Black,
                                unselectedIconColor = Color.Gray,
                                unselectedTextColor = Color.Gray
                            )
                        )
                        NavigationBarItem(
                            icon = { Text("03", fontWeight = FontWeight.Bold) },
                            label = { Text("HISTORY") },
                            selected = currentTab == AppTab.HISTORY,
                            onClick = { currentTab = AppTab.HISTORY },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color.White,
                                selectedTextColor = Color.Black,
                                indicatorColor = Color.Black,
                                unselectedIconColor = Color.Gray,
                                unselectedTextColor = Color.Gray
                            )
                        )
                    }
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