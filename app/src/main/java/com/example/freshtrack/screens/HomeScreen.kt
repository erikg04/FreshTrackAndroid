package com.example.freshtrack.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination


@Composable
fun HomeScreen(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Welcome to FreshTrack!", style = MaterialTheme.typography.headlineMedium)
        Text("What would you like to do today?", style = MaterialTheme.typography.bodyLarge)

        ElevatedButton(
            onClick = { navController.navigate("scan") {
                popUpTo(navController.graph.findStartDestination().id) { saveState = true}
                launchSingleTop = true
                restoreState = true
            }
                      },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Scan Barcode")
        }


        ElevatedButton(onClick = { }, modifier = Modifier.fillMaxWidth()) {
            Text("View Grocery List")
        }

        ElevatedButton(onClick = { }, modifier = Modifier.fillMaxWidth()) {
            Text("Meal Planner")
        }
    }
}


