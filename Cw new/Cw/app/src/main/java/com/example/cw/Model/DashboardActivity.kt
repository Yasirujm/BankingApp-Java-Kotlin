package com.example.cw.Model

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.cw.ui.theme.CwTheme
import android.util.Log
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import com.google.accompanist.pager.*
import androidx.compose.foundation.Image
import com.example.cw.R
import kotlinx.coroutines.delay


class DashboardActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            CwTheme {
                val navController = rememberNavController()
                DashboardScreen(navController = navController, context = this)
            }
        }
    }
    override fun onDestroy() {
        super.onDestroy()

        // Clear stored user data on app close
        val sharedPreferences: SharedPreferences = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.clear().apply()

        Log.d("Logout", "User logged out automatically on app close")
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPagerApi::class)
@Composable
fun DashboardScreen(navController: NavController, context: Context) {
    val sharedPreferences: SharedPreferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
    val editor = sharedPreferences.edit()
    val userId = sharedPreferences.getInt("userId", -1)
    val databaseHelper = remember { DatabaseHelper(context) }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val pagerState = rememberPagerState()
    var totalBalance by remember { mutableStateOf(0.0) }

    var username by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(pagerState.currentPage) {
        while (true) {
            delay(5000)
            pagerState.animateScrollToPage((pagerState.currentPage + 1) % 3)
        }
    }

    LaunchedEffect(userId) {
        if (userId != -1) {
            username = databaseHelper.getUsernameById(userId)
        }
    }

    LaunchedEffect(userId) {
        totalBalance = databaseHelper.getTotalCardBalance(userId)
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            DrawerContent(navController, username, drawerState, context)
        }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()),
            contentAlignment = Alignment.Center
        ) {
            if (username == null) {
                CircularProgressIndicator()
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp),
                    verticalArrangement = Arrangement.Top,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    TopAppBar(
                        title = { Text("Dashboard") },
                        navigationIcon = {
                            IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                Icon(Icons.Default.Menu, contentDescription = "Menu")
                            }
                        }
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .background(MaterialTheme.colorScheme.primary, shape = MaterialTheme.shapes.medium)
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Welcome, $username",
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Total Balance Tile
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Total Balance", style = MaterialTheme.typography.titleMedium)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Rs ${String.format("%,.2f", totalBalance)}", style = MaterialTheme.typography.headlineMedium)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Slideshow
                    HorizontalPager(
                        count = 3,
                        state = pagerState,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .background(MaterialTheme.colorScheme.secondary, shape = MaterialTheme.shapes.medium)
                            .padding(16.dp)
                    ) { page ->
                        val imageRes = when (page) {
                            0 -> R.drawable.image1
                            1 -> R.drawable.image2
                            2 -> R.drawable.image3
                            else -> R.drawable.image1
                        }

                        Image(
                            painter = painterResource(id = imageRes),
                            contentDescription = "Slideshow Image $page",
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Button(
                                onClick = { username?.let { navController.navigate("wallet_screen/$userId/$it") } },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Wallet")
                            }
                            Button(
                                onClick = { navController.navigate("manage_cards_screen/$userId") },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Manage Cards")
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Button(
                                onClick = {
                                    Log.d("Navigation", "Navigating to transactions_screen with userId: $userId")
                                    navController.navigate("transactions_screen/$userId")
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Transaction History")
                            }

                            Button(
                                onClick = { navController.navigate("transfer_screen/$userId") },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Transfer")
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            try {
                                editor.clear().apply()
                                Log.d("SharedPreferences", "userId after clear: ${sharedPreferences.getInt("userId", -1)}")
                                Toast.makeText(context, "Logged out successfully", Toast.LENGTH_SHORT).show()
                                navController.navigate("login_screen") {
                                    popUpTo("dashboard_screen") { inclusive = true }
                                    launchSingleTop = true
                                }
                            } catch (e: Exception) {
                                Toast.makeText(context, "An error occurred during logout", Toast.LENGTH_LONG).show()
                                Log.e("Logout Error", "Exception message: ${e.localizedMessage}")
                            }
                        },
                        modifier = Modifier.fillMaxWidth().padding(8.dp)
                    ) {
                        Text("Logout")
                    }
                }
            }
        }
    }
}

@Composable
fun DrawerContent(navController: NavController, username: String?, drawerState: DrawerState, context: Context) {
    val scope = rememberCoroutineScope()
    val sharedPreferences: SharedPreferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)

    Column(
        modifier = Modifier
            .fillMaxHeight()
            .width(250.dp)
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp)
    ) {
        Text(
            text = "Hello, ${username ?: "User"}!",
            style = MaterialTheme.typography.bodyLarge.copy(fontSize = 18.sp),
            modifier = Modifier
                .padding(top = 32.dp, bottom = 16.dp)
                .align(Alignment.CenterHorizontally)
        )

        Divider()

        Spacer(modifier = Modifier.height(16.dp))

        DrawerItem(text = "Edit Account", onClick = {
            val userId = sharedPreferences.getInt("userId", -1)
            if (userId != -1) {
                navController.navigate("edit_account_screen/$userId")
            } else {
                Toast.makeText(context, "User not found", Toast.LENGTH_SHORT).show()
            }
            scope.launch { drawerState.close() }
        })

        DrawerItem(text = "Exchange Rates", onClick = {
            navController.navigate("exchange_rate_screen")
            scope.launch { drawerState.close() }
        })

        DrawerItem(text = "Logout", onClick = {
            scope.launch { drawerState.close() }
            navController.navigate("login_screen") {
                popUpTo("dashboard_screen") { inclusive = true }
                launchSingleTop = true
            }
        })
    }
}

@Composable
fun DrawerItem(text: String, onClick: () -> Unit) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyLarge,
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { onClick() }
    )
}

