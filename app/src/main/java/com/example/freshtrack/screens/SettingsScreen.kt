package com.example.freshtrack.screens

import android.app.Activity
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

@Composable
fun SettingsScreen(
    isDarkMode: Boolean,
    onThemeChange: (Boolean) -> Unit,
    isNotificationsEnabled: Boolean,
    onNotificationsToggle: (Boolean) -> Unit,
    onLogout: () -> Unit
) {
    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)) {

        Text("Settings", style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(16.dp))

        // Notifications toggle
        SettingSwitchItem(
            title = "Enable Notifications",
            checked = isNotificationsEnabled,
            onCheckedChange = onNotificationsToggle
        )

        // Theme toggle
        SettingSwitchItem(
            title = "Dark Mode",
            checked = isDarkMode,
            onCheckedChange = onThemeChange
        )

        // Account Section
        Divider(Modifier.padding(vertical = 12.dp))
        Text("Account", style = MaterialTheme.typography.titleMedium)


        SettingTextItem(title = "Change Password") {
            // Navigate to change password screen
        }

        SettingTextItem(title = "Delete Account") {
            // Show confirmation dialog
        }

        // About Section
        Divider(Modifier.padding(vertical = 12.dp))
        Text("About", style = MaterialTheme.typography.titleMedium)

        SettingTextItem(title = "App Version: 1.15") {}
        SettingTextItem(title = "Send Feedback") {
            // Launch email intent
        }

        Spacer(modifier = Modifier.weight(1f))

        // Log out button
        Button(
            onClick = onLogout,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Log Out")
        }
    }
}

@Composable
fun SettingSwitchItem(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(title)
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
fun SettingTextItem(title: String, onClick: () -> Unit) {
    TextButton(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Text(title, modifier = Modifier.padding(vertical = 4.dp))
    }
}
