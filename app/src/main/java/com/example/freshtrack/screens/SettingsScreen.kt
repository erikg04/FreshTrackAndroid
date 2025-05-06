package com.example.freshtrack.screens

import android.app.Activity
import android.content.Context
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth

@Composable
fun SettingsScreen(
    isDarkMode: Boolean,
    onThemeChange: (Boolean) -> Unit,
    isNotificationsEnabled: Boolean,
    onNotificationsToggle: (Boolean) -> Unit,
    onLogout: () -> Unit,
)


{
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    var showPasswordDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }


    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)
        .verticalScroll(rememberScrollState())){

        Text("Settings", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onBackground)

        Spacer(modifier = Modifier.height(16.dp))

        // Notifications toggle
        SettingSwitchItem(
            title = "Enable Notifications",
            checked = isNotificationsEnabled,
            onCheckedChange = onNotificationsToggle,

        )

        // Theme toggle
        SettingSwitchItem(
            title = "Dark Mode",
            checked = isDarkMode,
            onCheckedChange = onThemeChange
        )

        // Account Section
        Divider(Modifier.padding(vertical = 12.dp))
        Text("Account", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground)


        SettingTextItem(title = "Change Password") {
            showPasswordDialog = true
        }

        SettingTextItem(title = "Delete Account") {
            showDeleteDialog = true
        }

        // About Section
        Divider(Modifier.padding(vertical = 12.dp))
        Text("About", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground)

        SettingTextItem(title = "App Version: 1.25") {}
        SettingTextItem(title = "Send Feedback") {
            val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                type = "message/rfc822"
                putExtra(android.content.Intent.EXTRA_EMAIL, arrayOf("erik.gonzalez.0615@gmail.com"))
                putExtra(android.content.Intent.EXTRA_SUBJECT, "FreshTrack Feedback")
                putExtra(android.content.Intent.EXTRA_TEXT, "Hi team, I wanted to share some feedback...")
            }
            context.startActivity(android.content.Intent.createChooser(intent, "Send Feedback via..."))
        }


        Spacer(modifier = Modifier.weight(1f))

        // Log out button
        Button(
            onClick = onLogout,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Log Out")

        }
    }
    if (showPasswordDialog) {
        ChangePasswordDialog(
            onDismiss = { showPasswordDialog = false },
            onConfirm = { newPassword ->
                val user = FirebaseAuth.getInstance().currentUser
                if (user != null) {
                    user.updatePassword(newPassword)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Toast.makeText(context, "Password updated", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "Error: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                }
                showPasswordDialog = false
            }
        )
    }


    if (showDeleteDialog) {
        ConfirmDeleteDialog(
            context = context,
            onDismiss = { showDeleteDialog = false }
        )
    }

}

@Composable
fun SettingSwitchItem(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
fun SettingTextItem(title: String, onClick: () -> Unit) {
    TextButton(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
            modifier = Modifier.padding(vertical = 4.dp),


        )
    }
}

@Composable
fun ChangePasswordDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var showMismatchError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "Change Password")
        },
        text = {
            Column {
                OutlinedTextField(
                    value = newPassword,
                    onValueChange = {
                        newPassword = it
                        showMismatchError = false
                    },
                    label = { Text("New Password") },
                    visualTransformation = PasswordVisualTransformation()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = {
                        confirmPassword = it
                        showMismatchError = false
                    },
                    label = { Text("Verify Password") },
                    visualTransformation = PasswordVisualTransformation()
                )
                if (showMismatchError) {
                    Text(
                        text = "Passwords do not match",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (newPassword == confirmPassword && newPassword.isNotBlank()) {
                    onConfirm(newPassword)
                } else {
                    showMismatchError = true
                }
            }) {
                Text("Change")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}


@Composable
fun ConfirmDeleteDialog(
    context: Context,
    onDismiss: () -> Unit
) {
    var password by remember { mutableStateOf("") }
    var errorText by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Confirm Delete") },
        text = {
            Column {
                Text("Please enter your password to permanently delete your account.")
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    visualTransformation = PasswordVisualTransformation()
                )
                if (errorText != null) {
                    Text(text = errorText!!, color = MaterialTheme.colorScheme.error)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val user = FirebaseAuth.getInstance().currentUser
                val email = user?.email

                if (user != null && email != null && password.isNotBlank()) {
                    val credential = EmailAuthProvider.getCredential(email, password)
                    user.reauthenticate(credential).addOnCompleteListener { reauthTask ->
                        if (reauthTask.isSuccessful) {
                            user.delete().addOnCompleteListener { deleteTask ->
                                if (deleteTask.isSuccessful) {
                                    Toast.makeText(context, "Account deleted", Toast.LENGTH_SHORT).show()
                                    if (context is Activity) context.finish()
                                } else {
                                    errorText = "Delete failed: ${deleteTask.exception?.message}"
                                }
                            }
                        } else {
                            errorText = "Incorrect password. Try again."
                        }
                    }
                } else {
                    errorText = "Password cannot be empty."
                }
            }) {
                Text("Delete")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}





