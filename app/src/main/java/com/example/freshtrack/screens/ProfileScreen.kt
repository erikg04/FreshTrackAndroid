// ProfileScreen.kt
package com.example.freshtrack.screens

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.launch
import androidx.compose.ui.layout.ContentScale

data class SavedRecipe(
    val id: Int = 0,
    val title: String = "",
    val image: String = ""
)

@Composable
fun ProfileScreen() {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val auth = FirebaseAuth.getInstance()
    val user = auth.currentUser
    val emailFromAuth = user?.email ?: "Not logged in"
    var name by remember { mutableStateOf("John Pork") }
    var email by remember { mutableStateOf(emailFromAuth) }
    var allergies by remember { mutableStateOf("Peanuts, Dairy") }
    val db = FirebaseFirestore.getInstance()
    val savedRecipes = remember { mutableStateListOf<SavedRecipe>() }

    LaunchedEffect(user?.uid) {
        user?.let { u ->
            db.collection("users").document(u.uid).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        name = document.getString("name") ?: ""
                        allergies = document.getString("allergies") ?: ""
                    }
                }
            db.collection("users").document(u.uid).collection("savedRecipes")
                .get()
                .addOnSuccessListener { snapshot ->
                    savedRecipes.clear()
                    savedRecipes.addAll(snapshot.documents.mapNotNull { it.toObject(SavedRecipe::class.java) })
                }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color.Transparent
    ) { innerPadding ->
        Column(
            Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Your Profile", style = MaterialTheme.typography.headlineMedium)
            OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth(), enabled = false)
            OutlinedTextField(value = allergies, onValueChange = { allergies = it }, label = { Text("Allergies (comma separated)") }, modifier = Modifier.fillMaxWidth())
            Button(onClick = {
                user?.uid?.let { uid ->
                    db.collection("users").document(uid).set(mapOf("name" to name, "allergies" to allergies), SetOptions.merge())
                        .addOnSuccessListener {
                            scope.launch { snackbarHostState.showSnackbar("✅ Profile saved successfully") }
                        }
                }
            }, modifier = Modifier.fillMaxWidth()) {
                Text("Save Profile")
            }
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            Text("Saved Recipes", style = MaterialTheme.typography.titleMedium)
            if (savedRecipes.isEmpty()) {
                Text("No saved recipes yet.")
            } else {
                savedRecipes.forEach { recipe ->
                    Card(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                        Column(Modifier.padding(12.dp)) {
                            AsyncImage(model = recipe.image, contentDescription = recipe.title, modifier = Modifier.fillMaxWidth().height(160.dp), contentScale = ContentScale.Crop)
                            Spacer(Modifier.height(8.dp))
                            Text(recipe.title, style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            }
        }
    }
}
