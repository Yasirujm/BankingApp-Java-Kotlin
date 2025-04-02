package com.example.cw.Model

import androidx.lifecycle.ViewModel
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

class UserViewModel : ViewModel() {
    var username by mutableStateOf<String?>(null)
}