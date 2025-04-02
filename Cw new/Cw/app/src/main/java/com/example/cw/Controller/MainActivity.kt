package com.example.cw.Controller

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.cw.Model.DashboardScreen
import com.example.cw.View.AddCardScreen
import com.example.cw.View.EditAccountScreen
import com.example.cw.View.ExchangeRateScreen
import com.example.cw.View.LoginScreen
import com.example.cw.View.ManageCardsScreen
import com.example.cw.View.RegisterScreen
import com.example.cw.View.TransactionsScreen
import com.example.cw.View.TransferScreen
import com.example.cw.View.WalletScreen
import com.example.cw.ui.theme.CwTheme


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CwTheme {
                // Initialize
                val navController = rememberNavController()

                NavHost(
                    navController = navController,
                    startDestination = "login_screen"
                ) {
                    composable("login_screen") {
                        LoginScreen(
                            onRegisterClick = { navController.navigate("register_screen") },
                            onLoginSuccess = { navController.navigate("dashboard_screen") },
                            context = this@MainActivity
                        )
                    }
                    composable("register_screen") {
                        RegisterScreen(
                            onBackClick = { navController.popBackStack() },
                            context = this@MainActivity
                        )
                    }
                    composable("dashboard_screen") {
                        DashboardScreen(navController = navController, context = this@MainActivity)
                    }
                    composable("edit_account_screen/{userId}") { backStackEntry ->
                        val userIdString = backStackEntry.arguments?.getString("userId")
                        val userId = userIdString?.toIntOrNull() ?: -1
                        if (userId == -1) {
                            Log.e("Navigation", "Invalid or missing userId: $userIdString")
                            navController.popBackStack()
                        } else {
                            EditAccountScreen(
                                navController = navController,
                                context = this@MainActivity,
                                userId = userId
                            )
                        }
                    }
                    composable("wallet_screen/{userId}/{username}") { backStackEntry ->
                        val userId = backStackEntry.arguments?.getString("userId")?.toInt() ?: 0
                        val username = backStackEntry.arguments?.getString("username") ?: ""
                        WalletScreen(
                            navController = navController,
                            username = username,
                            context = this@MainActivity,
                            userId = userId
                        )
                    }
                    composable("manage_cards_screen/{userId}") { backStackEntry ->
                        val userId = backStackEntry.arguments?.getString("userId")?.toInt() ?: 0
                        ManageCardsScreen(
                            navController = navController,
                            context = this@MainActivity,
                            userId = userId
                        )
                    }
                    composable("add_card_screen/{userId}") { backStackEntry ->
                        val userId = backStackEntry.arguments?.getString("userId")?.toInt() ?: 0
                        AddCardScreen(navController = navController, userId = userId)
                    }
                    composable("transactions_screen/{userId}") { backStackEntry ->
                        val userId = backStackEntry.arguments?.getString("userId")?.toInt() ?: 0
                        TransactionsScreen(
                            navController = navController,
                            context = this@MainActivity,
                            userId = userId
                        )
                    }
                    composable("transfer_screen/{userId}") { backStackEntry ->
                        val userId = backStackEntry.arguments?.getString("userId")?.toInt() ?: 0
                        TransferScreen(
                            navController = navController,
                            context = this@MainActivity,
                            userId = userId
                        )
                    }
                    composable("exchange_rate_screen") {
                        ExchangeRateScreen()
                    }
                }
            }
        }
    }
}
