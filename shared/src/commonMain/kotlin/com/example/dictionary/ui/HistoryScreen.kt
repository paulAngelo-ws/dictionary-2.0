package com.example.dictionary.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.dictionary.domain.HistoryItem

@Composable
fun HistoryScreen(
    history: List<HistoryItem>,
    onSelectWord: (String) -> Unit,
    onToggleFavorite: (String) -> Unit
) {
    Surface(modifier = Modifier.fillMaxSize(), color = Color(0xFFF9F9FB)) {
        Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = "SEARCH_LEDGER",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Black),
                    color = Color.Black
                )
                Text(
                    text = "${history.size} ENTRIES",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = Color.Black, thickness = 2.dp)
            Spacer(modifier = Modifier.height(16.dp))

            if (history.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No records found in local memory.", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(history) { item ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelectWord(item.word) },
                            color = if (item.isFavorite) Color(0xFFFFF9C4) else Color.White,
                            border = BorderStroke(1.dp, Color.Black),
                            shape = Shapes().extraSmall
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = item.word.uppercase(),
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                            color = Color.Black
                                        )
                                        if (item.isFavorite) {
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("★", color = Color(0xFFF57F17))
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = item.snippet,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.DarkGray,
                                        maxLines = 1
                                    )
                                }

                                IconButton(onClick = { onToggleFavorite(item.word) }) {
                                    Text(if (item.isFavorite) "★" else "☆", style = MaterialTheme.typography.titleLarge)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}