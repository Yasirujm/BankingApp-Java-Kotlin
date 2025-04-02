package com.example.cw.View

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import com.example.cw.Model.DatabaseHelper
import com.example.cw.Model.Transactions


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionsScreen(navController: NavController, context: Context, userId: Int) {
    val dbHelper = remember { DatabaseHelper(context) }
    var transactions by remember { mutableStateOf(emptyList<Transactions>()) }

    LaunchedEffect(userId) {
        transactions = dbHelper.getTransactionsByUserId(userId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Transaction History") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (transactions.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                Text("No transactions available", style = MaterialTheme.typography.bodyLarge)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding)
            ) {
                items(transactions) { transaction ->
                    TransactionItem(transaction)
                }
            }
        }
    }
}

@Composable
fun TransactionItem(transaction: Transactions) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Type: ${transaction.type}", style = MaterialTheme.typography.bodyLarge)
            Text(text = "Amount: Rs ${transaction.amount}", style = MaterialTheme.typography.bodyMedium)
            Text(text = "Date: ${transaction.date}", style = MaterialTheme.typography.bodyMedium)
        }
    }
}
