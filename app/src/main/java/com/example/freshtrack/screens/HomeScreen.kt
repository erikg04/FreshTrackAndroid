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
import coil.compose.AsyncImage
import com.example.freshtrack.api.RecipeResult
import com.example.freshtrack.api.RetrofitClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import java.time.LocalDate
import java.time.YearMonth

@Composable
fun HomeScreen(navController: NavHostController) {
    // Firestore & API setup
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    val db = FirebaseFirestore.getInstance()
    val apiKey = "8ab5c4c2685b4d119d9086796aa35484"

    // State holders
    val ingredients = remember { mutableStateListOf<String>() }
    val recipes = remember { mutableStateListOf<RecipeResult>() }
    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }

    // Load ingredients
    LaunchedEffect(userId) {
        db.collection("users")
            .document(userId)
            .collection("ingredients")
            .get()
            .addOnSuccessListener { snap ->
                ingredients.clear()
                ingredients.addAll(snap.documents.mapNotNull { it.getString("name") })
            }
    }

    // Fetch recipes
    LaunchedEffect(ingredients.toList()) {
        if (ingredients.isNotEmpty()) {
            try {
                val query = ingredients.joinToString(",")
                val result = RetrofitClient.spoonacularApi.findRecipes(
                    ingredients = query,
                    apiKey = apiKey
                )
                recipes.clear()
                recipes.addAll(result)
            } catch (e: Exception) {
                Log.e("SPOONACULAR", e.message ?: "API failed")
            }
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "FreshTrack",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(Modifier.height(16.dp))

        // 1. Embed the calendar
        SimpleCalendarScreen(
            yearMonth = YearMonth.now(),
            onDateSelected = { date: LocalDate ->
                selectedDate = date
                // TODO: load/filter meals for `date`
            }
        )

        Spacer(Modifier.height(16.dp))

        // 2. Show selected date header
        selectedDate?.let { date ->
            Text(
                text = "Meals for ${date.month.getDisplayName(java.time.format.TextStyle.FULL, java.util.Locale.getDefault())} ${date.dayOfMonth}",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(Modifier.height(8.dp))
        }

        // 3. Show recipe suggestions
        if (recipes.isNotEmpty()) {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(recipes) { recipe ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            AsyncImage(
                                model = recipe.image,
                                contentDescription = recipe.title,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp)
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(recipe.title, style = MaterialTheme.typography.bodyLarge)
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "${recipe.usedIngredientCount} used · ${recipe.missedIngredientCount} missing",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.height(8.dp))
                            Button(
                                onClick = {
                                    val save = mapOf(
                                        "id" to recipe.id,
                                        "title" to recipe.title,
                                        "image" to recipe.image
                                    )
                                    db.collection("users")
                                        .document(userId)
                                        .collection("savedRecipes")
                                        .document(recipe.id.toString())
                                        .set(save, SetOptions.merge())
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
            Text(
                "No meal suggestions yet. Scan ingredients to get started!",
                color = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}
