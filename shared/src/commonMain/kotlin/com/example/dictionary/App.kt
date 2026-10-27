package com.example.dictionary

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.savedstate.read
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.dictionary.data.AppStorage
import com.example.dictionary.data.DictionaryPreferences
import com.example.dictionary.data.DictionaryRepositoryImpl
import com.example.dictionary.data.createHttpClient
import com.example.dictionary.domain.HistoryUseCase
import com.example.dictionary.domain.SearchWordUseCase
import com.example.dictionary.domain.SlangPreferenceUseCase
import com.example.dictionary.presentation.DictionaryViewModel
import com.example.dictionary.presentation.UiState
import com.example.dictionary.ui.HistoryScreen
import com.example.dictionary.ui.InputScreen
import com.example.dictionary.ui.ResultScreen
import io.ktor.http.encodeURLParameter

// Module 8: a small, fixed set of destinations, with a word for the result.
sealed class Destination {
    object Input : Destination()
    object History : Destination()
    data class Result(val word: String) : Destination()
}

@Composable
fun App(storage: AppStorage) {
    val scope = rememberCoroutineScope()
    val client = remember { createHttpClient() }
    DisposableEffect(client) {
        onDispose { client.close() }
    }
    // Module 6: simple constructor injection; no DI framework is needed.
    val viewModel = remember(storage) {
        val repository = DictionaryRepositoryImpl(
            storage.database.historyDao(), client, "2233bca2-414f-4402-b28c-69f7aef9d02e"
        )
        DictionaryViewModel(
            SearchWordUseCase(repository),
            HistoryUseCase(repository),
            SlangPreferenceUseCase(DictionaryPreferences(storage.preferences)),
            scope
        )
    }
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route ?: "input"
    var searchQuery by remember { mutableStateOf("") }
    var lastWord by remember { mutableStateOf("") }
    val state by viewModel.uiState.collectAsState()
    val history by viewModel.searchHistory.collectAsState()
    val showSlang by viewModel.showSlang.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Module 7: collect one-time messages separately from persistent screen state.
    LaunchedEffect(viewModel) {
        viewModel.events.collect { message -> snackbarHostState.showSnackbar(message) }
    }

    fun navigate(destination: Destination) {
        val route = when (destination) {
            Destination.Input -> "input"
            Destination.History -> "history"
            is Destination.Result -> {
                val word = destination.word.trim().lowercase()
                if (word.isBlank()) "result" else "result/${word.encodeURLParameter()}"
            }
        }
        navController.navigate(route) {
            launchSingleTop = true
        }
    }

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
            snackbarHost = { SnackbarHost(snackbarHostState) },
            bottomBar = {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color.White,
                    border = BorderStroke(1.dp, Color.Black)
                ) {
                    NavigationBar(containerColor = Color.White, tonalElevation = 0.dp) {
                        NavigationBarItem(
                            icon = { Text("01", fontWeight = FontWeight.Bold) },
                            label = { Text("INPUT") },
                            selected = currentRoute == "input",
                            onClick = { navigate(Destination.Input) }
                        )
                        NavigationBarItem(
                            icon = { Text("02", fontWeight = FontWeight.Bold) },
                            label = { Text("RESULT") },
                            selected = currentRoute.startsWith("result"),
                            onClick = { navigate(Destination.Result(lastWord)) }
                        )
                        NavigationBarItem(
                            icon = { Text("03", fontWeight = FontWeight.Bold) },
                            label = { Text("HISTORY") },
                            selected = currentRoute == "history",
                            onClick = { navigate(Destination.History) }
                        )
                    }
                }
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = "input",
                modifier = Modifier.fillMaxSize().padding(innerPadding)
            ) {
                composable("input") {
                    InputScreen(
                        query = searchQuery,
                        onQueryChange = { searchQuery = it },
                        onSearchClick = { navigate(Destination.Result(it)) },
                        showSlang = showSlang,
                        onShowSlangChange = { viewModel.saveShowSlang(it) }
                    )
                }
                composable("result") {
                    ResultScreen("", UiState.Idle, showSlang, {}, {})
                }
                // Module 8: pass the word through the route, including deep links.
                composable(
                    route = "result/{word}",
                    arguments = listOf(navArgument("word") { type = NavType.StringType }),
                    deepLinks = listOf(navDeepLink { uriPattern = "dictionary://word/{word}" })
                ) { entry ->
                    val word = entry.arguments?.read { getString("word") }.orEmpty()
                    LaunchedEffect(word) {
                        lastWord = word
                        searchQuery = word
                        viewModel.searchWord(word)
                    }
                    ResultScreen(
                        word = word,
                        state = state,
                        showSlangByDefault = showSlang,
                        onRefresh = { viewModel.searchWord(word, forceRefresh = true) },
                        onSearchSuggestion = { navigate(Destination.Result(it)) }
                    )
                }
                composable("history") {
                    HistoryScreen(
                        history = history,
                        onSelectWord = { navigate(Destination.Result(it)) },
                        onToggleFavorite = { viewModel.toggleFavorite(it) }
                    )
                }
            }
        }
    }
}
