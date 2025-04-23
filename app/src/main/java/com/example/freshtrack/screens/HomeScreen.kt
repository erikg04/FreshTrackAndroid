package com.example.freshtrack.screens

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.freshtrack.api.RecipeResult
import com.example.freshtrack.api.RetrofitClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import coil.compose.AsyncImage
import com.google.firebase.firestore.SetOptions

@Composable
fun HomeScreen(navController: NavHostController) {
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    val db = FirebaseFirestore.getInstance()
    val apiKey = "8ab5c4c2685b4d119d9086796aa35484"

    val ingredients = remember { mutableStateListOf<String>() }
    val recipes = remember { mutableStateListOf<RecipeResult>() }

    // Step 1: Load ingredients from Firestore
    LaunchedEffect(userId) {
        db.collection("users").document(userId).collection("ingredients")
            .get()
            .addOnSuccessListener { snapshot ->
                val names = snapshot.documents.mapNotNull { it.getString("name") }
                ingredients.clear()
                ingredients.addAll(names)
            }
            .addOnFailureListener {
                Log.e("FIRESTORE", "Error loading ingredients: ${it.message}")
            }
    }

    // Step 2: Fetch recipes when ingredients are ready
    LaunchedEffect(ingredients.toList()) {
        if (ingredients.isNotEmpty()) {
            try {
                val ingredientQuery = ingredients.joinToString(",")
                val result = RetrofitClient.spoonacularApi.findRecipes(
                    ingredients = ingredientQuery,
                    apiKey = apiKey
                )
                recipes.clear()
                recipes.addAll(result)
            } catch (e: Exception) {
                Log.e("SPOONACULAR", e.message ?: "API call failed")
            }
        }
    }

    // Step 3: UI
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "FreshTrack",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (recipes.isNotEmpty()) {
            Text(
                text = "Meals You Can Make:",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(recipes) { recipe ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            // 🖼️ Show recipe image
                            AsyncImage(
                                model = recipe.image,
                                contentDescription = recipe.title,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp)
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(recipe.title, style = MaterialTheme.typography.bodyLarge)

                            Text(
                                text = "${recipe.usedIngredientCount} used · ${recipe.missedIngredientCount} missing",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // ✅ Save button
                            Button(
                                onClick = {
                                    val saveData = mapOf(
                                        "id" to recipe.id,
                                        "title" to recipe.title,
                                        "image" to recipe.image
                                    )

                                    db.collection("users").document(userId)
                                        .collection("savedRecipes")
                                        .document(recipe.id.toString())
                                        .set(saveData, SetOptions.merge())
                                        .addOnSuccessListener {
                                            Log.d("SAVE_RECIPE", "Recipe saved: ${recipe.title}")
                                        }
                                        .addOnFailureListener {
                                            Log.e("SAVE_RECIPE", "Failed to save: ${it.message}")
                                        }
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Save")
                            }
                        }
                    }
                }
            }

        } else {
            Text("No meal suggestions yet. Scan ingredients to get started!", color = MaterialTheme.colorScheme.onBackground)
        }
    }
}
