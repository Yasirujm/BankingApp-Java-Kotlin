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
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransferScreen(navController: NavController, context: Context, userId: Int) {
    var selectedCard by remember { mutableStateOf<String?>(null) }
    var selectedBank by remember { mutableStateOf("Select a Bank") }
    var beneficiaryName by remember { mutableStateOf("") }
    var beneficiaryAccountNumber by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf("Miscellaneous") }
    var showMessage by remember { mutableStateOf("") }

    val databaseHelper = DatabaseHelper(context)
    val cards = remember { mutableStateOf<List<Card>>(emptyList()) }

    LaunchedEffect(userId) {
        cards.value = databaseHelper.getCardsByUserId(userId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Transfer Money") },
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
            Spacer(modifier = Modifier.height(16.dp))

            var cardExpanded by remember { mutableStateOf(false) }
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(onClick = { cardExpanded = true }) {
                    Text(text = selectedCard ?: "Select a Card")
                }
                DropdownMenu(expanded = cardExpanded, onDismissRequest = { cardExpanded = false }) {
                    if (cards.value.isEmpty()) {
                        DropdownMenuItem(
                            text = { Text("No cards available") },
                            onClick = { cardExpanded = false }
                        )
                    } else {
                        cards.value.forEach { card ->
                            val displayText = "${card.bank} - ${card.cardNo.takeLast(4)}"
                            DropdownMenuItem(
                                text = { Text(displayText) },
                                onClick = {
                                    selectedCard = displayText
                                    cardExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            var bankExpanded by remember { mutableStateOf(false) }
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(onClick = { bankExpanded = true }) {
                    Text(text = selectedBank)
                }
                DropdownMenu(expanded = bankExpanded, onDismissRequest = { bankExpanded = false }) {
                    listOf("HNB", "Commercial Bank", "Sampath Bank", "BOC", "DFCC").forEach { bank ->
                        DropdownMenuItem(
                            text = { Text(bank) },
                            onClick = {
                                selectedBank = bank
                                bankExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = beneficiaryName,
                onValueChange = { beneficiaryName = it },
                label = { Text("Beneficiary Name") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = beneficiaryAccountNumber,
                onValueChange = { beneficiaryAccountNumber = it },
                label = { Text("Beneficiary Account Number") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it },
                label = { Text("Amount") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            var typeExpanded by remember { mutableStateOf(false) }
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(onClick = { typeExpanded = true }) {
                    Text(text = selectedType)
                }
                DropdownMenu(expanded = typeExpanded, onDismissRequest = { typeExpanded = false }) {
                    listOf("Miscellaneous", "Financial", "Personal", "Other").forEach { type ->
                        DropdownMenuItem(
                            text = { Text(type) },
                            onClick = {
                                selectedType = type
                                typeExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    try {
                        if (selectedCard != null && amount.isNotEmpty()) {
                            val transferAmount = amount.toDoubleOrNull()
                            if (transferAmount != null && transferAmount > 0) {
                                val selectedCardEntity = cards.value.find {
                                    "${it.bank} - ${it.cardNo.takeLast(4)}" == selectedCard
                                }

                                if (selectedCardEntity != null && selectedCardEntity.balance >= transferAmount) {
                                    // Deduct amount from card
                                    val success = databaseHelper.deductFromCard(selectedCardEntity.cardId, transferAmount)

                                    if (success) {
                                        val date = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
                                        databaseHelper.insertTransaction(
                                            userId = userId,
                                            cardId = selectedCardEntity.cardId,
                                            type = selectedType,
                                            amount = transferAmount,
                                            date = date
                                        )

                                        showMessage = "Transfer successful!"
                                    } else {
                                        showMessage = "Transfer failed! Check card balance."
                                    }
                                } else {
                                    showMessage = "Insufficient balance or invalid card."
                                }
                            } else {
                                showMessage = "Enter a valid amount!"
                            }
                        } else {
                            showMessage = "Please select a card and enter an amount!"
                        }
                    } catch (e: Exception) {
                        showMessage = "An error occurred: ${e.message}"
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Transfer")
            }

            if (showMessage.isNotEmpty()) {
                Text(text = showMessage, color = Color.Red, modifier = Modifier.padding(top = 8.dp))
            }
        }
    }
}
