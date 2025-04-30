package com.example.freshtrack.datastore

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private const val THEME_PREF_NAME = "user_theme_pref"
private val Context.dataStore by preferencesDataStore(name = THEME_PREF_NAME)

object ThemePreferenceKeys {
    val DARK_MODE = booleanPreferencesKey("dark_mode")
}

class ThemePreferenceManager(private val context: Context) {

    val isDarkModeEnabled: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[ThemePreferenceKeys.DARK_MODE] ?: false
        }

    suspend fun setDarkMode(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[ThemePreferenceKeys.DARK_MODE] = enabled
        }
    }
}
