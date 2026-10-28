package com.example.dictionary.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dictionary.domain.DictionaryEntry
import com.example.dictionary.presentation.UiState

@Composable
fun ResultScreen(
    word: String,
    state: UiState,
    onRefresh: () -> Unit,
    onSearchSuggestion: (String) -> Unit
) {

    Surface(modifier = Modifier.fillMaxSize(), color = Color(0xFFF9F9FB)) {
        Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
            if (word.isNotBlank()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        text = word.uppercase(),
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        ),
                        color = Color.Black
                    )
                    Text(
                        text = "QUERY_RESOLVED",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(color = Color.Black, thickness = 2.dp)
            }

            if (word.isNotBlank()) {
                TextButton(onClick = onRefresh, enabled = state !is UiState.Loading) {
                    Text("Refresh definitions")
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            Box(modifier = Modifier.fillMaxSize()) {
                when (state) {
                    is UiState.Loading -> CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = Color.Black
                    )
                    is UiState.Error -> Text(
                        text = "ERROR: ${state.message}",
                        color = Color.Red,
                        modifier = Modifier.align(Alignment.Center)
                    )
                    is UiState.Success -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {

                            if (!state.didYouMeanSuggestion.isNullOrBlank()) {
                                item {
                                    Surface(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { onSearchSuggestion(state.didYouMeanSuggestion) },
                                        color = Color(0xFFE3F2FD),
                                        border = BorderStroke(1.dp, Color(0xFF90CAF9)),
                                        shape = Shapes().extraSmall
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(14.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text("💡", fontSize = 16.sp)
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Column {
                                                Text(
                                                    "Did you mean: ${state.didYouMeanSuggestion.uppercase()}?",
                                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                                    color = Color(0xFF0D47A1)
                                                )
                                                Text(
                                                    "Click here to search the corrected spelling.",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = Color(0xFF1565C0)
                                                )
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                }
                            }

                            if (state.formalEntries.isNotEmpty()) {
                                item {
                                    Text(
                                        "// FORMAL DEFINITIONS",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                        color = Color.DarkGray
                                    )
                                }
                                items(state.formalEntries) { entry ->
                                    DefinitionCard(entry)
                                }
                            }


                        }
                    }
                    is UiState.Idle -> Text(
                        "No active query. Input a target term.",
                        modifier = Modifier.align(Alignment.Center),
                        color = Color.Gray
                    )
                }
            }
        }
    }
}

@Composable
fun DefinitionCard(entry: DictionaryEntry) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White,
        border = BorderStroke(1.dp, Color.Black),
        shape = Shapes().extraSmall
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "[ ${entry.source.uppercase()} ]",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = Color.Black
                )
                if (entry.partOfSpeech.isNotEmpty()) {
                    Text(
                        text = entry.partOfSpeech,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = entry.definition,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Normal),
                color = Color.Black
            )
            if (entry.example.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF4F4F6))
                        .padding(10.dp)
                ) {
                    Text(
                        text = "EX: \"${entry.example}\"",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.DarkGray
                    )
                }
            }
        }
    }
}
