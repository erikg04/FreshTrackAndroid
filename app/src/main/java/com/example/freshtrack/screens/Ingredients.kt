package com.example.freshtrack.screens



import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun AddIngredientsScreen() {
    var searchQuery by remember { mutableStateOf("") }

    // Replace this with your real ingredient list or Firestore data later
    val allIngredients = listOf(
        "Tomato", "Onion", "Garlic", "Milk", "Egg", "Cheese",
        "Carrot", "Chicken", "Lettuce", "Spinach", "Butter", "Yogurt"
    )

    // Filter the ingredients based on search query
    val filteredIngredients = allIngredients.filter {
        it.contains(searchQuery, ignoreCase = true)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Search Ingredients", style = MaterialTheme.typography.headlineMedium)

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text("Search") },
            modifier = Modifier.fillMaxWidth()
        )

        Divider()
        Text("Results:", style = MaterialTheme.typography.titleMedium)

        LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            items(filteredIngredients) { ingredient ->
                ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = ingredient,
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }
}

