package com.example.cw.View

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import android.util.Log
import com.example.cw.Model.DatabaseHelper


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCardScreen(navController: NavController, userId: Int) {
    Log.d("AddCardScreen", "Received userId: $userId")
    var selectedBank by remember { mutableStateOf("Select Bank") }
    val banks = listOf("HNB", "Commercial Bank", "Sampath Bank", "BOC", "DFCC")
    var cardHolderName by remember { mutableStateOf("") }
    var cardNumber by remember { mutableStateOf("") }
    var cvv by remember { mutableStateOf("") }
    var expiryDate by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }

    val databaseHelper = DatabaseHelper(navController.context)

    fun validateInputs(): Boolean {
        if (!cardHolderName.matches(Regex("^[a-zA-Z ]+\$"))) {
            errorMessage = "Card Holder Name should contain only letters"
            return false
        }
        if (!cardNumber.matches(Regex("^\\d{16}\$"))) {
            errorMessage = "Card Number must be exactly 16 digits"
            return false
        }
        if (!cvv.matches(Regex("^\\d{3}\$"))) {
            errorMessage = "CVV must be exactly 3 digits"
            return false
        }
        if (!expiryDate.matches(Regex("^(0[1-9]|1[0-2])/\\d{2}\$"))) {
            errorMessage = "Expiry Date must be in MM/YY format"
            return false
        }
        errorMessage = ""
        return true
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .padding(top = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Add Card", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
            OutlinedTextField(
                value = selectedBank,
                onValueChange = {},
                readOnly = true,
                modifier = Modifier.fillMaxWidth().menuAnchor(),
                trailingIcon = {
                    IconButton(onClick = { expanded = !expanded }) {
                        Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = "Dropdown")
                    }
                }
            )
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                banks.forEach { bank ->
                    DropdownMenuItem(text = { Text(bank) }, onClick = {
                        selectedBank = bank
                        expanded = false
                    })
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Card Holder Name
        OutlinedTextField(
            value = cardHolderName,
            onValueChange = { cardHolderName = it },
            label = { Text("Card Holder Name") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Card Number
        OutlinedTextField(
            value = cardNumber,
            onValueChange = { cardNumber = it.take(16) },
            label = { Text("Card Number") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        // CVV
        OutlinedTextField(
            value = cvv,
            onValueChange = { cvv = it.take(3) },
            label = { Text("CVV") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = expiryDate,
            onValueChange = { newValue ->
                // Allow only numbers and a single '/'
                val filtered = newValue.filterIndexed { index, char ->
                    char.isDigit() || (char == '/' && index == 2)
                }
                expiryDate = filtered.take(5) // Ensure max length MM/YY
            },
            label = { Text("Expiry Date (MM/YY)") },
            keyboardOptions = KeyboardOptions.Default,
            modifier = Modifier.fillMaxWidth()
        )

        Button(onClick = {
            if (validateInputs()) {
                val isSuccess = databaseHelper.registerCard(
                    userId,
                    cardHolderName,
                    selectedBank,
                    cardNumber,
                    cvv,
                    expiryDate
                )

                if (isSuccess) {
                    navController.popBackStack()
                } else {
                    errorMessage = "Failed to add the card. Please try again."
                }
            }
        }) {
            Text("Add Card")
        }
    }
}
