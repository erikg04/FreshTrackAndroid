package com.example.freshtrack.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.freshtrack.api.ProductData
import androidx.compose.material3.DismissDirection
import androidx.compose.material3.DismissValue
import androidx.compose.material3.SwipeToDismiss
import androidx.compose.material3.rememberDismissState
import androidx.compose.ui.Alignment
import androidx.compose.foundation.background



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddIngredientsScreen() {
    var searchQuery by remember { mutableStateOf("") }
    var ingredients by remember { mutableStateOf<List<String>>(emptyList()) }

    // Fetch user inventory once when screen loads
    LaunchedEffect(Unit) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            FirebaseFirestore.getInstance()
                .collection("users")
                .document(userId)
                .collection("scannedProducts")
                .get()
                .addOnSuccessListener { result ->
                    ingredients = result.mapNotNull { it.getString("name") }
                }
                .addOnFailureListener {
                    // Handle failure if needed
                }
        }
    }

    val filteredIngredients = ingredients.filter {
        it.contains(searchQuery, ignoreCase = true)
    }

    Column(
        Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Search Ingredients", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onBackground)

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Type to search…") },
            singleLine = true,
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),


        )

        Divider()
        Text("Inventory:", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground)

        LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            items(
                items = filteredIngredients,
                key = { it }  // Key is important for smooth swipe animations
            ) { ingredient ->

                // Set up dismiss state
                val dismissState = rememberDismissState(
                    confirmValueChange = { dismissValue ->
                        if (dismissValue == DismissValue.DismissedToStart) {
                            ingredients = ingredients - ingredient  // Remove from local list
                            val userId = FirebaseAuth.getInstance().currentUser?.uid
                            if (userId != null) {
                                val db = FirebaseFirestore.getInstance()

                                // 1. Delete from scannedProducts
                                db.collection("users")
                                    .document(userId)
                                    .collection("scannedProducts")
                                    .whereEqualTo("name", ingredient)
                                    .get()
                                    .addOnSuccessListener { result ->
                                        for (document in result) {
                                            document.reference.delete()
                                        }
                                    }

                                // 2. Delete from ingredients
                                db.collection("users")
                                    .document(userId)
                                    .collection("ingredients")
                                    .whereEqualTo("name", ingredient)
                                    .get()
                                    .addOnSuccessListener { result ->
                                        for (document in result) {
                                            document.reference.delete()
                                        }
                                    }
                            }
                            true
                        } else {
                            false
                        }
                    }
                )

                SwipeToDismiss(
                    state = dismissState,
                    directions = setOf(DismissDirection.EndToStart),
                    background = {

                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(12.dp),
                            contentAlignment = Alignment.CenterEnd
                        ) {
                            Text(
                                text = "Delete",
                                color = Color.Red,
                                style = MaterialTheme.typography.bodyLarge

                            )
                        }
                    },
                    dismissContent = {
                        // Your original card
                        ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = ingredient,
                                modifier = Modifier.padding(12.dp),
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                )
            }
        }
    }
}
