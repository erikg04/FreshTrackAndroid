package com.example.freshtrack.screens

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.freshtrack.api.RecipeResult
import com.example.freshtrack.api.RetrofitClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth


@Composable
fun HomeScreen(navController: NavHostController) {
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    val db = FirebaseFirestore.getInstance()
    val apiKey = "8ab5c4c2685b4d119d9086796aa35484"

    val ingredients = remember { mutableStateListOf<String>() }
    val recipes = remember { mutableStateListOf<RecipeResult>() }
    val scope = rememberCoroutineScope()
    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    // Load ingredients and recipes
    LaunchedEffect(userId) {
        Log.d("DEBUG", "LaunchedEffect started for userId=$userId")

        db.collection("users").document(userId).collection("ingredients")
            .get()
            .addOnSuccessListener { snapshot ->
                val names = snapshot.documents.mapNotNull { it.getString("name") }
                Log.d("DEBUG", "Fetched ingredients: $names")

                ingredients.clear()
                ingredients.addAll(names)

                if (ingredients.isNotEmpty()) {
                    val ingredientQuery = ingredients.joinToString(",")
                    Log.d("DEBUG", "Querying Spoonacular with: $ingredientQuery")

                    scope.launch {
                        try {
                            val result = RetrofitClient.spoonacularApi.findRecipes(
                                ingredients = ingredientQuery,
                                number = 10,
                                ranking = 1,
                                ignorePantry = true,
                                apiKey = apiKey
                            )
                            recipes.clear()
                            recipes.addAll(result)
                            Log.d("DEBUG", "Fetched ${recipes.size} recipes from API")

                            // 👇 Move isLoading=false *only after successful fetch
                            isLoading = false
                        } catch (e: Exception) {
                            Log.e("DEBUG", "Spoonacular API call failed: ${e.message}")
                            isLoading = false
                        }
                    }
                } else {
                    Log.d("DEBUG", "No ingredients found in Firestore")
                    isLoading = false
                }
            }
            .addOnFailureListener {
                Log.e("DEBUG", "Firestore load failed: ${it.message}")
                isLoading = false
            }
    }

    // UI
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            // Calendar Section
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "FreshTrack",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(16.dp))

                SimpleCalendarScreen(
                    yearMonth = YearMonth.now(),
                    onDateSelected = { date: LocalDate ->
                        selectedDate = date
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                selectedDate?.let { date ->
                    Text(
                        text = "Meals for ${date.month.getDisplayName(java.time.format.TextStyle.FULL, java.util.Locale.getDefault())} ${date.dayOfMonth}",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
        }

        if (isLoading) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = androidx.compose.ui.Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        } else if (recipes.isNotEmpty()) {
            items(recipes) { recipe ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        AsyncImage(
                            model = recipe.image,
                            contentDescription = recipe.title,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp),
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(recipe.title, style = MaterialTheme.typography.bodyLarge)

                        Text(
                            text = "${recipe.usedIngredientCount} used · ${recipe.missedIngredientCount} missing",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(8.dp))

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
        } else {
            item {
                Text(
                    text = "No meal suggestions yet. Scan ingredients to get started!",
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.fillMaxWidth(),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }

}
