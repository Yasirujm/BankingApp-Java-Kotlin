package com.example.cw.View

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.input.PasswordVisualTransformation
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.remember
import androidx.compose.runtime.LaunchedEffect
import com.example.cw.Model.DashboardActivity
import com.example.cw.Model.DatabaseHelper
import com.example.cw.R


@Composable
fun LoginScreen(
    onRegisterClick: () -> Unit,
    onLoginSuccess: () -> Unit,
    modifier: Modifier = Modifier,
    context: Context
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var usernameError by remember { mutableStateOf("") }
    var passwordError by remember { mutableStateOf("") }

    fun validateLogin() {
        usernameError = if (username.isBlank()) "Username cannot be empty." else ""
        passwordError = if (password.isBlank()) "Password cannot be empty." else ""
    }

    val dbHelper = DatabaseHelper(context)
    val sharedPreferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
    val editor = sharedPreferences.edit()

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                showLoginNotification(context)
            } else {
                Toast.makeText(context, "Notification permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    )

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFFFFFFF),
                        Color(0xFF90CAF9)
                    ),
                    startY = 600f,
                    endY = 2000f
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.login_image),
                contentDescription = "Login Image",
                modifier = Modifier
                    .size(250.dp)
                    .padding(bottom = 8.dp)
            )

            Text(
                "Login",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Username") },
                isError = usernameError.isNotEmpty(),
                modifier = Modifier.fillMaxWidth().padding(8.dp)
            )
            if (usernameError.isNotEmpty()) {
                Text(usernameError, color = MaterialTheme.colorScheme.error)
            }
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                isError = passwordError.isNotEmpty(),
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth().padding(8.dp)
            )
            if (passwordError.isNotEmpty()) {
                Text(passwordError, color = MaterialTheme.colorScheme.error)
            }
            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    validateLogin()
                    if (usernameError.isEmpty() && passwordError.isEmpty()) {
                        val user = dbHelper.getUserByUsernameAndPassword(username, password)
                        if (user != null) {
                            editor.putInt("userId", user.id)
                            editor.putString("username", username)
                            editor.apply()

                            Toast.makeText(context, "Login successful!", Toast.LENGTH_SHORT).show()
                            showLoginNotification(context)
                            onLoginSuccess()
                        } else {
                            Toast.makeText(context, "Invalid credentials", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().padding(8.dp)
            ) {
                Text("Login")
            }
            Spacer(modifier = Modifier.height(16.dp))

            TextButton(onClick = { onRegisterClick() }) {
                Text("Don't have an account? Register")
            }
        }
    }
}

fun showLoginNotification(context: Context) {
    val sharedPreferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
    val username = sharedPreferences.getString("username", null) // Change "User" to null

    // Don't show notification if no user is logged in
    if (username.isNullOrEmpty()) {
        return
    }

    val channelId = "login_success_channel"
    val notificationId = 1

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val name = "Login Notifications"
        val descriptionText = "Shows notifications when login is successful"
        val importance = NotificationManager.IMPORTANCE_HIGH
        val channel = NotificationChannel(channelId, name, importance).apply {
            description = descriptionText
        }
        val notificationManager: NotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    val intent = Intent(context, DashboardActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    }
    val pendingIntent = PendingIntent.getActivity(
        context, 0, intent, PendingIntent.FLAG_IMMUTABLE
    )

    val builder = NotificationCompat.Builder(context, channelId)
        .setSmallIcon(R.drawable.login_image)
        .setContentTitle("Successful Login!")
        .setContentText("Welcome back, $username!")
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setAutoCancel(true)
        .setContentIntent(pendingIntent)

    with(NotificationManagerCompat.from(context)) {
        notify(notificationId, builder.build())
    }
}

