package com.example.cw.View

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import com.example.cw.Model.DatabaseHelper
import com.example.cw.Controller.GmailSender
import java.util.concurrent.Executors

@Composable
fun RegisterScreen(onBackClick: () -> Unit, modifier: Modifier = Modifier, context: Context) {
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var mobileNo by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var reEnterPassword by remember { mutableStateOf("") }

    var usernameError by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf("") }
    var mobileNoError by remember { mutableStateOf("") }
    var passwordError by remember { mutableStateOf("") }
    var reEnterPasswordError by remember { mutableStateOf("") }

    fun validateRegister() {
        usernameError = if (username.isBlank() || username.any { it.isDigit() }) {
            "Username cannot be empty or contain numbers."
        } else {
            ""
        }

        emailError = if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            "Please enter a valid email address."
        } else {
            ""
        }

        mobileNoError = if (mobileNo.length != 10 || mobileNo.any { !it.isDigit() }) {
            "Mobile number must be 10 digits."
        } else {
            ""
        }

        passwordError = if (password.length < 6) {
            "Password must be at least 6 characters."
        } else {
            ""
        }

        reEnterPasswordError = if (reEnterPassword != password) {
            "Passwords do not match."
        } else {
            ""
        }
    }

    val dbHelper = DatabaseHelper(context)

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            "Register",
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
            Text(usernameError, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            isError = emailError.isNotEmpty(),
            modifier = Modifier.fillMaxWidth().padding(8.dp)
        )
        if (emailError.isNotEmpty()) {
            Text(emailError, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }

        OutlinedTextField(
            value = mobileNo,
            onValueChange = { mobileNo = it },
            label = { Text("Mobile No") },
            isError = mobileNoError.isNotEmpty(),
            modifier = Modifier.fillMaxWidth().padding(8.dp)
        )
        if (mobileNoError.isNotEmpty()) {
            Text(mobileNoError, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            isError = passwordError.isNotEmpty(),
            modifier = Modifier.fillMaxWidth().padding(8.dp)
        )
        if (passwordError.isNotEmpty()) {
            Text(passwordError, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }

        OutlinedTextField(
            value = reEnterPassword,
            onValueChange = { reEnterPassword = it },
            label = { Text("Re-enter Password") },
            visualTransformation = PasswordVisualTransformation(),
            isError = reEnterPasswordError.isNotEmpty(),
            modifier = Modifier.fillMaxWidth().padding(8.dp)
        )
        if (reEnterPasswordError.isNotEmpty()) {
            Text(reEnterPasswordError, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                validateRegister()
                if (usernameError.isEmpty() && emailError.isEmpty() && mobileNoError.isEmpty() &&
                    passwordError.isEmpty() && reEnterPasswordError.isEmpty()
                ) {
                    val isRegistered = dbHelper.registerUser(username, email, mobileNo, password)
                    if (isRegistered) {
                        Toast.makeText(context, "Registration successful! Wallet created.", Toast.LENGTH_SHORT).show()

                        // Send Email in Background Thread
                        Executors.newSingleThreadExecutor().execute {
                            val sender = GmailSender("Email", "App Password")//removed my own for privacy reasons
                            val subject = "Welcome to Our App!"
                            val message = """
                                Hello $username,

                                Thank you for registering. Your account and wallet have been created successfully.

                                Best Regards,
                                EBank App Team
                            """.trimIndent()

                            val emailSent = sender.sendEmail(email, subject, message)
                            if (emailSent) {
                                println("✅ Email sent successfully!")
                            } else {
                                println("❌ Failed to send email.")
                            }
                        }

                        onBackClick()
                    } else {
                        Toast.makeText(context, "Error during registration.", Toast.LENGTH_SHORT).show()
                    }
                }
            },
            modifier = Modifier.fillMaxWidth().padding(8.dp)
        ) {
            Text("Register")
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = { onBackClick() }) {
            Text("Back to Login")
        }
    }
}

