package com.example.cw.View

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExchangeRateScreen() {
    val rates = mapOf(
        "USD" to 325.0,
        "GBP" to 415.0,
        "AUD" to 215.0,
        "EUR" to 355.0,
        "CAD" to 240.0,
        "JPY" to 2.15
    )

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Exchange Rates") })
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                items(rates.entries.toList()) { (currency, rate) ->
                    ExchangeRateItem(currency, rate)
                }
            }
        }
    }
}

@Composable
fun ExchangeRateItem(currency: String, rate: Double) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "1 $currency = ${"%.2f".format(rate)} LKR", style = MaterialTheme.typography.titleLarge)
        }
    }
}
