package com.example.cw.View

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import com.example.cw.Model.Card
import com.example.cw.Model.DatabaseHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageCardsScreen(navController: NavController, context: Context, userId: Int) {
    val databaseHelper = DatabaseHelper(context)
    var cards by remember { mutableStateOf<List<Card>>(emptyList()) }
    var showDialog by remember { mutableStateOf(false) }
    var cardToDelete by remember { mutableStateOf<Card?>(null) }

    LaunchedEffect(userId) {
        cards = databaseHelper.getCardsByUserId(userId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Manage Cards") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Spacer(modifier = Modifier.height(16.dp))

                if (cards.isEmpty()) {
                    Text("No cards registered", style = MaterialTheme.typography.bodyMedium)
                } else {
                    cards.forEach { card ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Bank: ${card.bank}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))

                                    Text(
                                        text = "Card Number: ${maskCardNumber(card.cardNo)}",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))

                                    Text(
                                        text = "Balance: Rs ${"%.2f".format(card.balance)}",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }

                                IconButton(onClick = {
                                    cardToDelete = card
                                    showDialog = true
                                }) {
                                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete Card", tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))


                Button(onClick = {
                    navController.navigate("add_card_screen/$userId")
                }) {
                    Text("Add Card")
                }
            }
        }
    }

    if (showDialog && cardToDelete != null) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Confirm Deletion") },
            text = { Text("Are you sure you want to delete this card? This action cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    databaseHelper.deleteCard(cardToDelete!!.cardId)
                    cards = databaseHelper.getCardsByUserId(userId) // Refresh list
                    showDialog = false
                }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

fun maskCardNumber(cardNo: String): String {
    return "**** **** **** ${cardNo.takeLast(4)}"
}
