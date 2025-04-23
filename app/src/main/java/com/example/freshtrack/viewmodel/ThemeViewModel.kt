package com.example.freshtrack.viewmodel

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel

class ThemeViewModel : ViewModel() {
    private val _isDarkMode = mutableStateOf(false)
    val isDarkMode: State<Boolean> get() = _isDarkMode

    fun toggleTheme(enabled: Boolean) {
        _isDarkMode.value = enabled
    }
}
