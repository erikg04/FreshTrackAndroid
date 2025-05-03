package com.example.freshtrack.screens

import android.app.DatePickerDialog
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.freshtrack.api.RecipeResult
import com.example.freshtrack.api.RetrofitClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.*

@Composable
fun HomeScreen(navController: NavHostController) {
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    val db = FirebaseFirestore.getInstance()
    val apiKey = "8ab5c4c2685b4d119d9086796aa35484"

    val ingredients = remember { mutableStateListOf<String>() }
    val recipes = remember { mutableStateListOf<RecipeResult>() }
    val mealsByDate = remember { mutableStateMapOf<LocalDate, MutableList<String>>() }

    val scope = rememberCoroutineScope()
    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    val context = LocalContext.current

    DisposableEffect(userId) {
        val registration = db.collection("users")
            .document(userId)
            .collection("mealCalendar")
            .addSnapshotListener { snap, error ->
                if (error != null) {
                    Log.e("CALENDAR_LISTENER", error.message ?: "unknown error")
                    return@addSnapshotListener
                }
                if (snap != null) {
                    mealsByDate.clear()
                    snap.documents.forEach { doc ->
                        val date = LocalDate.parse(doc.id)
                        val list = (doc.get("meals") as? List<String>).orEmpty()
                        mealsByDate[date] = list.toMutableList()
                    }
                }
            }
        onDispose { registration.remove() }
    }

    LaunchedEffect(userId) {
        db.collection("users")
            .document(userId)
            .collection("ingredients")
            .get()
            .addOnSuccessListener { snapshot ->
                ingredients.clear()
                ingredients.addAll(snapshot.documents.mapNotNull { it.getString("name") })
            }
    }

    LaunchedEffect(ingredients.toList()) {
        if (ingredients.isNotEmpty()) {
            scope.launch {
                try {
                    val result = RetrofitClient.spoonacularApi.findRecipes(
                        ingredients = ingredients.joinToString(","),
                        number = 10,
                        ranking = 1,
                        ignorePantry = true,
                        apiKey = apiKey
                    )
                    recipes.clear()
                    recipes.addAll(result)
                } catch (e: Exception) {
                    Log.e("SPOONACULAR", e.message ?: "")
                } finally {
                    isLoading = false
                }
            }
        } else {
            isLoading = false
        }
    }

    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column(Modifier.fillMaxWidth()) {
                Text("FreshTrack", style = MaterialTheme.typography.headlineMedium)
                Spacer(Modifier.height(16.dp))

                SimpleCalendarScreen(
                    yearMonth = YearMonth.now(),
                    mealsByDate = mealsByDate,
                    onDateSelected = { date -> selectedDate = date }
                )

                Spacer(Modifier.height(16.dp))

                selectedDate?.let { date ->
                    Text(
                        "Meals for ${date.month.getDisplayName(TextStyle.FULL, Locale.getDefault())} ${date.dayOfMonth}",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(Modifier.height(8.dp))

                    val meals = mealsByDate[date].orEmpty()
                    if (meals.isEmpty()) {
                        Text("No meals added.", style = MaterialTheme.typography.bodyLarge)
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            meals.forEach { meal ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("• $meal", modifier = Modifier.weight(1f))
                                    IconButton(onClick = {
                                        db.collection("users")
                                            .document(userId)
                                            .collection("mealCalendar")
                                            .document(date.toString())
                                            .update("meals", FieldValue.arrayRemove(meal))
                                        mealsByDate[date]?.remove(meal)
                                    }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete")
                                    }
                                    IconButton(onClick = {
                                        DatePickerDialog(
                                            context,
                                            { _, y, m, d ->
                                                val newDate = LocalDate.of(y, m + 1, d)
                                                val oldKey = date.toString()
                                                val newKey = newDate.toString()
                                                db.collection("users")
                                                    .document(userId)
                                                    .collection("mealCalendar")
                                                    .document(oldKey)
                                                    .update("meals", FieldValue.arrayRemove(meal))
                                                mealsByDate[date]?.remove(meal)
                                                val newRef = db.collection("users")
                                                    .document(userId)
                                                    .collection("mealCalendar")
                                                    .document(newKey)
                                                newRef.update("meals", FieldValue.arrayUnion(meal))
                                                    .addOnFailureListener {
                                                        newRef.set(mapOf("meals" to listOf(meal)))
                                                    }
                                                mealsByDate.getOrPut(newDate) { mutableListOf() }.add(meal)
                                            },
                                            date.year,
                                            date.monthValue - 1,
                                            date.dayOfMonth
                                        ).show()
                                    }) {
                                        Icon(Icons.Default.Edit, contentDescription = "Move")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (isLoading) {
            item {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        } else {
            items(recipes) { recipe ->
                Card(
                    Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        AsyncImage(
                            model = recipe.image,
                            contentDescription = recipe.title,
                            Modifier
                                .fillMaxWidth()
                                .height(180.dp),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(recipe.title, style = MaterialTheme.typography.bodyLarge)
                        Text(
                            "${recipe.usedIngredientCount} used · ${recipe.missedIngredientCount} missing",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(8.dp))
                        Button(
                            onClick = {
                                val saveData = mapOf(
                                    "id" to recipe.id,
                                    "title" to recipe.title,
                                    "image" to recipe.image
                                )
                                db.collection("users")
                                    .document(userId)
                                    .collection("savedRecipes")
                                    .document(recipe.id.toString())
                                    .set(saveData, SetOptions.merge())
                            },
                            Modifier.fillMaxWidth()
                        ) {
                            Text("Save")
                        }
                    }
                }
            }
        }
    }
}




