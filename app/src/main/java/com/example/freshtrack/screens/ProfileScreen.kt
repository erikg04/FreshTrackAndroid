package com.example.freshtrack.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Warning

@Composable
fun ProfileScreen() {
    var name by remember { mutableStateOf("John Doe") }
    var email by remember { mutableStateOf("john@example.com") }
    var allergies by remember { mutableStateOf("Peanuts, Dairy") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Profile Header
        Text("Your Profile", style = MaterialTheme.typography.headlineMedium)

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Name") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = allergies,
            onValueChange = { allergies = it },
            label = { Text("Allergies (comma separated)") },
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = { /* Save profile data to Firebase or local */ },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save Profile")
        }

        Divider(modifier = Modifier.padding(vertical = 8.dp))

        // Action Items
        ProfileOption(text = "Saved Recipes", icon = Icons.Default.Bookmark)
        ProfileOption(text = "Preferences", icon = Icons.Default.Settings) {

        }
    }
}

@Composable
fun ProfileOption(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(16.dp)
        ) {
            Icon(icon, contentDescription = text, modifier = Modifier.padding(end = 16.dp))
            Text(text, style = MaterialTheme.typography.bodyLarge)
        }
    }
}

