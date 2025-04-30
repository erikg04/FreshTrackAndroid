package com.example.freshtrack.viewmodel

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.freshtrack.datastore.ThemePreferenceManager
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ThemeViewModel(private val themePreferenceManager: ThemePreferenceManager) : ViewModel() {
    private val _isDarkMode = mutableStateOf(false)
    val isDarkMode: State<Boolean> get() = _isDarkMode

    init {
        viewModelScope.launch {
            themePreferenceManager.isDarkModeEnabled.collectLatest { savedValue ->
                _isDarkMode.value = savedValue
            }
        }
    }

    fun toggleTheme(enabled: Boolean) {
        _isDarkMode.value = enabled
        viewModelScope.launch {
            themePreferenceManager.setDarkMode(enabled)
        }
    }
}
