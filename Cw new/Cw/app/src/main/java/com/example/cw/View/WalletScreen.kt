package com.example.cw.View

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import com.example.cw.Model.Card
import com.example.cw.Model.DatabaseHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletScreen(navController: NavController, username: String?, context: Context, userId: Int) {
    var amountInput by remember { mutableStateOf("") }
    var selectedCard by remember { mutableStateOf<String?>(null) }
    var showMessage by remember { mutableStateOf("") }
    var walletBalance by remember { mutableStateOf(0.0) }
    var showConfirmationDialog by remember { mutableStateOf(false) }
    var isTopUp by remember { mutableStateOf(true) } // To track if the action is top-up or withdraw

    val databaseHelper = DatabaseHelper(context)
    val cards = remember { mutableStateOf<List<Card>>(emptyList()) }

    LaunchedEffect(userId) {
        cards.value = databaseHelper.getCardsByUserId(userId)
        val wallet = databaseHelper.getWalletByUserId(userId)
        walletBalance = wallet?.balance ?: 0.0
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Wallet") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Username Card
            Card(
                modifier = Modifier.fillMaxWidth().padding(8.dp),
                shape = MaterialTheme.shapes.medium,
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "Username: ${username ?: "N/A"}", style = MaterialTheme.typography.titleMedium)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Wallet Balance Card with Comma Formatting
            Card(
                modifier = Modifier.fillMaxWidth().padding(8.dp),
                shape = MaterialTheme.shapes.medium,
                colors = CardDefaults.cardColors(containerColor = Color(0xFFC8E6C9))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Wallet Balance: Rs ${"%,.2f".format(walletBalance)}",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Select Card Dropdown
            var expanded by remember { mutableStateOf(false) }
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(onClick = { expanded = true }) {
                    Text(text = selectedCard ?: "Select a Card")
                }
                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    if (cards.value.isEmpty()) {
                        DropdownMenuItem(
                            text = { Text("No cards available") },
                            onClick = { expanded = false }
                        )
                    } else {
                        cards.value.forEach { card ->
                            val displayText = "${card.bank} - ${card.cardNo.takeLast(4)}"
                            DropdownMenuItem(
                                text = { Text(displayText) },
                                onClick = {
                                    selectedCard = displayText
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Amount Input Field
            OutlinedTextField(
                value = amountInput,
                onValueChange = { amountInput = it },
                label = { Text("Enter Amount") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Buttons for Top-Up & Withdraw
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Button(
                    onClick = {
                        if (selectedCard != null && amountInput.isNotEmpty()) {
                            isTopUp = true
                            showConfirmationDialog = true
                        } else {
                            showMessage = "Please select a card and enter a valid amount!"
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Top-Up Wallet")
                }

                Button(
                    onClick = {
                        if (selectedCard != null && amountInput.isNotEmpty()) {
                            isTopUp = false
                            showConfirmationDialog = true
                        } else {
                            showMessage = "Please select a card and enter a valid amount!"
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Withdraw to Card")
                }
            }

            // Confirmation Dialog
            if (showConfirmationDialog) {
                AlertDialog(
                    onDismissRequest = { showConfirmationDialog = false },
                    title = { Text(if (isTopUp) "Confirm Top-Up" else "Confirm Withdrawal") },
                    text = {
                        Text("Are you sure you want to ${if (isTopUp) "top-up" else "withdraw"} Rs ${amountInput} using $selectedCard?")
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                showConfirmationDialog = false
                                if (isTopUp) {
                                    performTransaction(true, amountInput, selectedCard, cards.value, databaseHelper, userId, { updatedBalance -> walletBalance = updatedBalance }, { message -> showMessage = message }, { amountInput = "" })
                                } else {
                                    performTransaction(false, amountInput, selectedCard, cards.value, databaseHelper, userId, { updatedBalance -> walletBalance = updatedBalance }, { message -> showMessage = message }, { amountInput = "" })
                                }
                            }
                        ) {
                            Text("Confirm")
                        }
                    },
                    dismissButton = {
                        Button(onClick = { showConfirmationDialog = false }) {
                            Text("Cancel")
                        }
                    }
                )
            }

            if (showMessage.isNotEmpty()) {
                Text(text = showMessage, color = Color.Red, modifier = Modifier.padding(top = 8.dp))
            }
        }
    }
}

fun performTransaction(
    isTopUp: Boolean,
    amountStr: String,
    selectedCard: String?,
    cards: List<Card>,
    databaseHelper: DatabaseHelper,
    userId: Int,
    updateWalletBalance: (Double) -> Unit,
    showMessage: (String) -> Unit,
    resetAmount: () -> Unit
) {
    try {
        val amount = amountStr.toDoubleOrNull()

        if (selectedCard != null && amount != null && amount > 0) {
            val selectedCardId = cards.find {
                "${it.bank} - ${it.cardNo.takeLast(4)}" == selectedCard
            }?.cardId

            if (selectedCardId != null) {
                val success = if (isTopUp) {
                    databaseHelper.topUpWalletFromCard(userId, selectedCardId, amount)
                } else {
                    databaseHelper.withdrawFromWalletToCard(userId, selectedCardId, amount)
                }

                if (success) {
                    showMessage("Transaction successful!")
                    val wallet = databaseHelper.getWalletByUserId(userId)
                    updateWalletBalance(wallet?.balance ?: 0.0)
                } else {
                    showMessage("Transaction failed! Check balance.")
                }
            } else {
                showMessage("Card not found.")
            }
        } else {
            showMessage("Enter a valid amount!")
        }
    } catch (e: Exception) {
        showMessage("Error: ${e.message}")
    }
}
