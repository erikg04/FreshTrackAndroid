package com.example.freshtrack.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp

@Composable
fun SignInScreen(
    onSignIn: (email: String, password: String) -> Unit,
    onSignUp: ( email: String, name: String, password: String) -> Unit
) {
    var isSignUpMode by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("") }  // Only used for sign up (optional)
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = if (isSignUpMode) "Sign Up" else "Sign In",
            style = MaterialTheme.typography.headlineMedium
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
        Spacer(modifier = Modifier.height(16.dp))
        TextButton(onClick = { isSignUpMode = !isSignUpMode }) {
            Text(text = if (isSignUpMode) "Already have an account? Sign In" else "Don't have an account? Sign Up")
        }
    }
}
