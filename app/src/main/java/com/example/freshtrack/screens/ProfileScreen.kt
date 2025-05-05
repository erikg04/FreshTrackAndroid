package com.example.freshtrack.screens

import android.app.DatePickerDialog
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.*
import androidx.compose.ui.layout.ContentScale

data class SavedRecipe(
    val id: Int = 0,
    val title: String = "",
    val image: String = ""
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen() {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val auth = FirebaseAuth.getInstance()
    val user = auth.currentUser
    val db = FirebaseFirestore.getInstance()
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf(user?.email ?: "Not logged in") }
    var allergies by remember { mutableStateOf("") }
    val savedRecipes = remember { mutableStateListOf<SavedRecipe>() }

    LaunchedEffect(user?.uid) {
        user?.uid?.let { uid ->
            db.collection("users").document(uid).get()
                .addOnSuccessListener { doc ->
                    name = doc.getString("name") ?: ""
                    allergies = doc.getString("allergies") ?: ""
                }
            db.collection("users").document(uid).collection("savedRecipes")
                .get()
                .addOnSuccessListener { snap ->
                    savedRecipes.clear()
                    savedRecipes.addAll(snap.documents.mapNotNull { it.toObject(SavedRecipe::class.java) })
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
            OutlinedTextField(value = allergies, onValueChange = { allergies = it }, label = { Text("Allergies") }, modifier = Modifier.fillMaxWidth())
            Button(onClick = {
                user?.uid?.let { uid ->
                    db.collection("users").document(uid)
                        .set(mapOf("name" to name, "allergies" to allergies), SetOptions.merge())
                        .addOnSuccessListener {
                            scope.launch { snackbarHostState.showSnackbar("✅ Profile saved") }
                        }
                }
            }, modifier = Modifier.fillMaxWidth()) {
                Text("Save Profile")
            }
            Divider(Modifier.padding(vertical = 8.dp))
            Text("Saved Recipes", style = MaterialTheme.typography.titleMedium)
            if (savedRecipes.isEmpty()) {
                Text("No saved recipes yet.")
            } else {
                savedRecipes.forEach { recipe ->
                    Column(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                        Card(Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(12.dp)) {
                                AsyncImage(model = recipe.image, contentDescription = recipe.title, modifier = Modifier.fillMaxWidth().height(160.dp), contentScale = ContentScale.Crop)
                                Spacer(Modifier.height(8.dp))
                                Text(recipe.title, style = MaterialTheme.typography.bodyLarge)
                            }
                        }
                        val calendar = Calendar.getInstance()
                        Button(
                            onClick = {
                                DatePickerDialog(
                                    context,
                                    { _, year, month, day ->
                                        val dateKey = LocalDate.of(year, month + 1, day).toString()
                                        val ref = db.collection("users")
                                            .document(user!!.uid)
                                            .collection("mealCalendar")
                                            .document(dateKey)
                                        ref.update("meals", FieldValue.arrayUnion(recipe.title))
                                            .addOnFailureListener {
                                                ref.set(mapOf("meals" to listOf(recipe.title)))
                                            }
                                        Toast.makeText(context, "Assigned to $dateKey", Toast.LENGTH_SHORT).show()
                                    },
                                    calendar.get(Calendar.YEAR),
                                    calendar.get(Calendar.MONTH),
                                    calendar.get(Calendar.DAY_OF_MONTH)
                                ).show()
                            },
                            modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                        ) {
                            Text("Assign to Date")
                        }
                    }
                }
            }
        }
    }
}
