package com.example.freshtrack.screens

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

object AuthManager {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    fun signIn(
        email: String,
        password: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        auth.signInWithEmailAndPassword(email.trim(), password.trim())
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onSuccess()
                } else {
                    onError(task.exception?.message ?: "Sign in failed.")
                }
            }
    }


    fun signUp(
        email: String,
        name: String,
        password: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(email.trim(), password.trim())
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userId = auth.currentUser?.uid ?: return@addOnCompleteListener
                    val db = FirebaseFirestore.getInstance()

                    val userData = mapOf(
                        "name" to name,
                        "email" to email,
                        "allergies" to ""
                    )

                    db.collection("users").document(userId)
                        .set(userData)
                        .addOnSuccessListener {
                            onSuccess()
                        }
                        .addOnFailureListener { e ->
                            onError("Signup succeeded but failed to save profile: ${e.message}")
                        }
                } else {
                    onError(task.exception?.message ?: "Sign up failed.")
                }
            }
    }

    fun isUserLoggedIn(): Boolean {
        return auth.currentUser != null
    }

    fun signOut() {
        auth.signOut()
    }
}
