package com.example.freshtrack.screens

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.freshtrack.api.RetrofitClient
import androidx.core.text.HtmlCompat

// --- Response Models ---
data class RecipeDetailsResponse(
    val title: String,
    val image: String,
    val summary: String,
    val instructions: String?,
    val nutrition: Nutrition
)

data class Nutrition(
    val nutrients: List<Nutrient>
)

data class Nutrient(
    val name: String,
    val amount: Double,
    val unit: String
)

// --- Nutrition Box Composable ---
@Composable
fun NutritionFactsBox(nutrients: List<Nutrient>) {
    val importantNutrients = listOf(
        "Calories", "Total Fat", "Saturated Fat", "Trans Fat",
        "Cholesterol", "Sodium", "Total Carbohydrate", "Dietary Fiber",
        "Total Sugars", "Protein", "Vitamin D", "Calcium", "Iron", "Potassium"
    )

    val filtered = nutrients.filter { it.name in importantNutrients }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(2.dp, MaterialTheme.colorScheme.onBackground)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            "Nutrition Facts",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Divider(thickness = 2.dp)

        filtered.forEach { nutrient ->
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    nutrient.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    "${nutrient.amount} ${nutrient.unit}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
            Divider(thickness = 1.dp)
        }

        Text(
            "* % Daily Value based on a 2,000 calorie diet.",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

// --- Detail Screen UI ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeDetailScreen(
    navController: NavController,
    recipeId: Int
) {
    var recipe by remember { mutableStateOf<RecipeDetailsResponse?>(null) }

    LaunchedEffect(recipeId) {
        try {
            recipe = RetrofitClient.spoonacularApi
                .getRecipeDetails(recipeId, true, "8ab5c4c2685b4d119d9086796aa35484")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    recipe?.let { it ->
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(it.title) },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                )
            }
        ) { innerPadding ->
            LazyColumn(
                contentPadding = innerPadding,
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                item {
                    Text(
                        it.title,
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                item {
                    AsyncImage(
                        model = it.image,
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                    )
                }

                item {
                    Text(
                        HtmlCompat.fromHtml(it.summary, HtmlCompat.FROM_HTML_MODE_LEGACY).toString(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                item {
                    NutritionFactsBox(it.nutrition.nutrients)
                }

                it.instructions?.let { inst ->
                    item {
                        Text(
                            "Instructions:",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                    item {
                        Text(
                            inst,
                            overflow = TextOverflow.Clip,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }

                item {
                    Spacer(Modifier.height(24.dp))
                    Button(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Go Back")
                    }
                }
            }
        }
    } ?: run {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }
}