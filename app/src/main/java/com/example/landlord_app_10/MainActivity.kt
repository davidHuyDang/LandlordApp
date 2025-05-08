package com.example.landlord_app_10

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.landlord_app_10.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val auth = FirebaseAuth.getInstance()
    // Check if a user is already logged in
    val currentUser = auth.currentUser
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        if (currentUser != null) {
            // If user is logged in, go to the ViewListings screen
            navigateToViewListings()
        }

        // Set listener for the login button
        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                signInWithEmailAndPassword(email, password)
            } else {
                Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Function to sign in a user with email and password
    private fun signInWithEmailAndPassword(email: String, password: String) {
        // Attempt to sign in using Firebase Authentication
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // If sign-in is successful, get the current user's ID
                    val userId = auth.currentUser?.uid
                    if (userId != null) {
                        db.collection("userProfiles").document(userId)
                            .get()
                            .addOnSuccessListener { document ->
                                // Check if the user is a landlord
                                val isLandlord = document.getBoolean("isLandlord") ?: false
                                if (isLandlord) {
                                    // If the user is a landlord, navigate to the listings view
                                    navigateToViewListings()
                                } else {
                                    // If the user is not a landlord, show a toast message
                                    Toast.makeText(this, "You need to be a landlord", Toast.LENGTH_SHORT).show()
                                }
                            }
                            .addOnFailureListener { e ->
                                // If there's an error fetching the user profile, show an error message
                                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                    }
                } else {
                    // If sign-in fails, display a message to the user
                    Toast.makeText(this, "Authentication failed", Toast.LENGTH_SHORT).show()
                }
            }
    }


    // Navigate to the ViewListings screen
    private fun navigateToViewListings() {
        val intent = Intent(this, ViewListings::class.java)
        startActivity(intent)
        finish()  // Close the login activity to prevent the user from returning back
    }
}