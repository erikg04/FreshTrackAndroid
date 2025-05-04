package com.example.freshtrack

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.*
import com.example.freshtrack.datastore.ThemePreferenceManager
import com.example.freshtrack.screens.*
import com.example.freshtrack.ui.components.BackgroundImage
import com.example.freshtrack.ui.theme.FreshTrackTheme
import com.example.freshtrack.viewmodel.ThemeViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val context = LocalContext.current
            val themeViewModel: ThemeViewModel = viewModel(factory = object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    val manager = ThemePreferenceManager(context)
                    return ThemeViewModel(manager) as T
                }
            })
            val isDarkMode by themeViewModel.isDarkMode
            var isLoggedIn by remember { mutableStateOf(AuthManager.isUserLoggedIn()) }

            FreshTrackTheme(darkTheme = isDarkMode) {
                Box(Modifier.fillMaxSize()) {
                    BackgroundImage(isDarkMode = isDarkMode)

                    if (isLoggedIn) {
                        FreshTrackApp(
                            isDarkMode = isDarkMode,
                            onThemeChange = { themeViewModel.toggleTheme(it) },
                            onLogout = {
                                AuthManager.signOut()
                                Toast.makeText(context, "You've been logged out", Toast.LENGTH_SHORT).show()
                                isLoggedIn = false
                            }
                        )
                    } else {
                        SignInScreen(
                            isDarkMode = isDarkMode,
                            onSignIn = { email, password ->
                                AuthManager.signIn(
                                    email, password,
                                    onSuccess = {
                                        isLoggedIn = true
                                        Toast.makeText(context, "Signed in!", Toast.LENGTH_SHORT).show()
                                    },
                                    onError = { message ->
                                        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                                    }
                                )
                            },
                            onSignUp = { name, email, password ->
                                AuthManager.signUp(
                                    name, email, password,
                                    onSuccess = {
                                        isLoggedIn = true
                                        Toast.makeText(context, "Account created!", Toast.LENGTH_SHORT).show()
                                    },
                                    onError = { message ->
                                        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                                    }
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}

sealed class Screen(val route: String, val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    object Home : Screen("home", "Home", Icons.Default.Home)
    object Profile : Screen("profile", "Profile", Icons.Default.Person)
    object Scan : Screen("scan", "Scan", Icons.Default.Add)
    object Ingredients : Screen("ingredients", "Ingredient", Icons.Default.FoodBank)
    object Settings : Screen("settings", "Settings", Icons.Default.Settings)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FreshTrackApp(
    isDarkMode: Boolean,
    onThemeChange: (Boolean) -> Unit,
    onLogout: () -> Unit
) {
    val navController = rememberNavController()

    Box(modifier = Modifier.fillMaxSize()) {
        BackgroundImage(isDarkMode = isDarkMode)

        Scaffold(
            containerColor = MaterialTheme.colorScheme.background.copy(alpha = 0.85f),
            bottomBar = {
                BottomNavigationBar(navController = navController, onScanClick = {
                    // TODO: Barcode scanner
                })
            }
        ) { paddingValues ->
            NavHost(
                navController = navController,
                startDestination = Screen.Home.route,
                modifier = Modifier.padding(paddingValues)
            ) {
                composable(Screen.Home.route) { HomeScreen(navController) }
                composable("calendar") { SimpleCalendarScreen() }
                composable(Screen.Profile.route) { ProfileScreen() }
                composable(Screen.Scan.route) { BarcodeScannerScreen() }
                composable(Screen.Ingredients.route) { AddIngredientsScreen() }
                composable(Screen.Settings.route) {
                    SettingsScreen(
                        isDarkMode = isDarkMode,
                        onThemeChange = onThemeChange,
                        isNotificationsEnabled = true,
                        onNotificationsToggle = {},
                        onLogout = onLogout
                    )
                }
                composable("details/{recipeId}") { backStackEntry ->
                    val recipeId = backStackEntry.arguments?.getString("recipeId")?.toIntOrNull()
                    recipeId?.let {
                        RecipeDetailScreen(recipeId = it)
                    }
                }

            }
        }
    }
}

@Composable
fun BottomNavigationBar(navController: NavHostController, onScanClick: () -> Unit) {
    val items = listOf(
        Screen.Home,
        Screen.Profile,
        Screen.Scan,
        Screen.Ingredients,
        Screen.Settings
    )
    NavigationBar {
        val currentDestination = navController.currentBackStackEntryAsState().value?.destination
        items.forEach { screen ->
            val selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true
            NavigationBarItem(
                icon = { Icon(screen.icon, contentDescription = screen.title) },
                label = { Text(screen.title) },
                selected = selected,
                onClick = {
                    navController.navigate(screen.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}
