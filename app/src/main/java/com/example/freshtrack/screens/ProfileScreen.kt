package com.example.freshtrack.screens

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import coil.compose.AsyncImage

data class SavedRecipe(
    val id: Int = 0,
    val title: String = "",
    val image: String = ""
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen() {
    val auth = FirebaseAuth.getInstance()
    val user = auth.currentUser
    val emailFromAuth = user?.email ?: "Not logged in"

    var name by remember { mutableStateOf("John Doe") }
    var email by remember { mutableStateOf(emailFromAuth) }
    var allergies by remember { mutableStateOf("Peanuts, Dairy") }

    val db = FirebaseFirestore.getInstance()
    val savedRecipes = remember { mutableStateListOf<SavedRecipe>() }

    // 🔄 Load saved recipes
    LaunchedEffect(user?.uid) {
        if (user != null) {
            db.collection("users").document(user.uid)
                .collection("savedRecipes")
                .get()
                .addOnSuccessListener { snapshot ->
                    val data = snapshot.documents.mapNotNull { it.toObject(SavedRecipe::class.java) }
                    savedRecipes.clear()
                    savedRecipes.addAll(data)
                }
                .addOnFailureListener {
                    Log.e("FIRESTORE", "Failed to load saved recipes: ${it.message}")
                }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 👤 Profile Header
        Text("Your Profile", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onBackground)

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Name") },
            modifier = Modifier.fillMaxWidth()
        )

        // ✉️ Auto-filled email
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            enabled = false
        )

        OutlinedTextField(
            value = allergies,
            onValueChange = { allergies = it },
            label = { Text("Allergies (comma separated)") },
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = { /* Save logic here */ },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save Profile")
        }

        Divider(modifier = Modifier.padding(vertical = 8.dp))

        // 🍽️ Saved Recipes Section
        Text("Saved Recipes", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground)

        if (savedRecipes.isEmpty()) {
            Text("No saved recipes yet.", color = MaterialTheme.colorScheme.onBackground)
        } else {
            savedRecipes.forEach { recipe ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Column(Modifier.padding(12.dp)) {
                        AsyncImage(
                            model = recipe.image,
                            contentDescription = recipe.title,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(recipe.title, style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
        }

        Divider(modifier = Modifier.padding(vertical = 8.dp))

        // ➕ Preferences & More
        ProfileTextItem(title = "Preferences") {
            // Navigate to preferences
        }
    }
}

@Composable
fun ProfileTextItem(title: String, onClick: () -> Unit) {
    TextButton(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Text(text = title, modifier = Modifier.padding(vertical = 4.dp))
    }
}
