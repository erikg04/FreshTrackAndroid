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
import androidx.compose.ui.graphics.Color

@OptIn(ExperimentalMaterial3Api::class)
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
        Text("Your Profile", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onBackground)

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
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),

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
        ProfileTextItem(title = "Saved Recipes"){

        }
        ProfileTextItem(title = "Preferences") {

        }
    }
}

@Composable
fun ProfileTextItem(title: String, onClick: () -> Unit) {
    TextButton(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
            modifier = Modifier.padding(vertical = 4.dp),


            )
    }
}
