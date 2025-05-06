package com.example.freshtrack.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import com.example.freshtrack.R
import com.example.freshtrack.ui.components.BackgroundImage

@Composable
fun SignInScreen(
    isDarkMode: Boolean,
    onSignIn: (email: String, password: String) -> Unit,
    onSignUp: (email: String, name: String, password: String) -> Unit
) {
    var isSignUpMode by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()




    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color.Transparent
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // uses the dark background image
            BackgroundImage(isDarkMode = isDarkMode)


            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(Color.Black.copy(alpha = 0.3f))
            )


            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = if (isSignUpMode) "Sign Up" else "Sign In",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (isSignUpMode) {
                    TextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                TextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                TextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = PasswordVisualTransformation()
                )
                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        if (isSignUpMode) {
                            onSignUp(email, name, password)
                        } else {
                            onSignIn(email, password)
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = if (isSignUpMode) "Sign Up" else "Sign In")
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = { isSignUpMode = !isSignUpMode }) {
                        Text(
                            text = if (isSignUpMode)
                                "Already have an account? Sign In"
                            else
                                "Don't have an account? Sign Up",
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }

                    TextButton(
                        onClick = {
                            if (email.isNotBlank()) {
                                FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                                    .addOnCompleteListener { task ->
                                        scope.launch {
                                            val message = if (task.isSuccessful) {
                                                "Password reset email sent!"
                                            } else {
                                                "Failed: ${task.exception?.message}"
                                            }
                                            snackbarHostState.showSnackbar(message)
                                        }
                                    }
                            } else {
                                scope.launch {
                                    snackbarHostState.showSnackbar("Please enter your email.")
                                }
                            }
                        }
                    ) {
                        Text("Forgot password?", color = MaterialTheme.colorScheme.onBackground)
                    }
                }

            }
        }
    }
}

