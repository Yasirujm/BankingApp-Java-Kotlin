package com.example.cw.View

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.cw.Model.DatabaseHelper


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditAccountScreen(navController: NavController, context: Context, userId: Int) {
    val databaseHelper = remember { DatabaseHelper(context) }

    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }

    var existingPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }


    LaunchedEffect(userId) {
        username = databaseHelper.getUsernameById(userId) ?: ""
        email = databaseHelper.getEmailById(userId) ?: ""
        phoneNumber = databaseHelper.getPhoneNumberById(userId) ?: ""
    }


    fun isEmailValid(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    fun isPhoneNumberValid(phoneNumber: String): Boolean {
        return phoneNumber.startsWith("07") && phoneNumber.length == 10 && phoneNumber.all { it.isDigit() }
    }

    fun isPasswordValid(password: String): Boolean {
        val uppercaseRegex = ".*[A-Z].*".toRegex()
        val specialCharRegex = ".*[!@#$%^&*(),.?\":{}|<>].*".toRegex()
        return uppercaseRegex.matches(password) && specialCharRegex.matches(password)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .background(MaterialTheme.colorScheme.surface)
    ) {
        TopAppBar(
            title = { Text("Edit Account") },
            navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Username") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
        )
        if (email.isNotEmpty() && !isEmailValid(email)) {
            Text("Please enter a valid email", color = MaterialTheme.colorScheme.error)
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = phoneNumber,
            onValueChange = { phoneNumber = it },
            label = { Text("Phone Number") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
        )
        if (phoneNumber.isNotEmpty() && !isPhoneNumberValid(phoneNumber)) {
            Text("Phone number must start with 07 and contain 10 digits", color = MaterialTheme.colorScheme.error)
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = existingPassword,
            onValueChange = { existingPassword = it },
            label = { Text("Existing Password") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = newPassword,
            onValueChange = { newPassword = it },
            label = { Text("New Password") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
        )
        if (newPassword.isNotEmpty() && !isPasswordValid(newPassword)) {
            Text("Password must contain at least one uppercase letter and one special character", color = MaterialTheme.colorScheme.error)
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = { Text("Confirm New Password") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
        )
        if (confirmPassword.isNotEmpty() && confirmPassword != newPassword) {
            Text("Passwords do not match", color = MaterialTheme.colorScheme.error)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {

                val storedPassword = databaseHelper.getPasswordById(userId) // Assuming you have a method to fetch the password
                if (storedPassword != existingPassword) {
                    Toast.makeText(context, "Incorrect existing password", Toast.LENGTH_SHORT).show()
                    return@Button
                }

                if (!isEmailValid(email)) {
                    Toast.makeText(context, "Invalid email", Toast.LENGTH_SHORT).show()
                    return@Button
                }
                if (!isPhoneNumberValid(phoneNumber)) {
                    Toast.makeText(context, "Invalid phone number", Toast.LENGTH_SHORT).show()
                    return@Button
                }
                if (!isPasswordValid(newPassword)) {
                    Toast.makeText(context, "Password must contain at least one uppercase letter and one special character", Toast.LENGTH_SHORT).show()
                    return@Button
                }
                if (newPassword != confirmPassword) {
                    Toast.makeText(context, "New passwords do not match", Toast.LENGTH_SHORT).show()
                    return@Button
                }

                databaseHelper.updateUserDetails(userId, username, email, phoneNumber)
                databaseHelper.updatePassword(userId, newPassword)

                Toast.makeText(context, "Account updated successfully", Toast.LENGTH_SHORT).show()
                navController.popBackStack()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save Changes")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = { navController.popBackStack() },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
        ) {
            Text("Cancel")
        }
    }
}