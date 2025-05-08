package com.example.landlord_app_10

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.landlord_app_10.databinding.ActivityViewListingsBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ViewListings : AppCompatActivity() {

    private lateinit var binding: ActivityViewListingsBinding  // Binding for the Activity's layout.
    private lateinit var adapter: PropertyAdapter  // Adapter to manage the RecyclerView's data.
    private val properties = mutableListOf<Property>()  // Mutable list to hold properties data.
    private val db =
        FirebaseFirestore.getInstance()  // Firestore instance to interact with Firebase.

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding =
            ActivityViewListingsBinding.inflate(layoutInflater)  // Inflate the layout using ViewBinding.
        setContentView(binding.root)

        // Initialize the adapter with required actions: edit, delete, and toggle availability.
        adapter = PropertyAdapter(
            properties,
            onEdit = { property -> editProperty(property) },  // Edit property action.
            onDelete = { property -> deleteProperty(property) },  // Delete property action.
            onToggle = { property, isAvailable ->
                toggleAvailability(
                    property,
                    isAvailable
                )
            }  // Toggle availability action.
        )

        // Set up the RecyclerView with a LinearLayoutManager and the adapter.
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter

        // Fetch properties from Firestore based on the current user.
        fetchProperties()

        // Navigate to the CreateListingActivity when the FAB is clicked.
        binding.btnAdd.setOnClickListener {
            startActivity(Intent(this, CreateListingActivity::class.java))
        }

        // Set up the Sign-Out button to log out the user.
        binding.btnSignOut.setOnClickListener {
            signOutUser()
        }
    }

    // Called when the activity is resumed (user returns to this screen).
    override fun onResume() {
        super.onResume()
        fetchProperties()  // Refresh properties when returning to this activity.
    }

    // Fetch the properties from Firestore for the current user.
    private fun fetchProperties() {
        val userId =
            FirebaseAuth.getInstance().currentUser?.uid ?: return  // Get the current user's UID.

        // Query Firestore to fetch properties that belong to the current user.
        db.collection("properties")
            .whereEqualTo("userId", userId)  // Filter properties by the current user ID.
            .get()
            .addOnSuccessListener { result ->
                // Map Firestore documents to Property objects.
                val newProperties = result.documents.map { doc ->
                    Property(
                        id = doc.id,  // Use the document ID as the property ID.
                        address = doc.getString("address") ?: "",
                        imageUrl = doc.getString("imageUrl") ?: "",
                        price = doc.getDouble("price") ?: 0.0,
                        bedrooms = doc.getLong("bedrooms")?.toInt() ?: 0,
                        available = doc.getBoolean("available") ?: true,
                        userId = doc.getString("userId")
                            ?: ""  // Optional: Store user ID if needed.
                    )
                }
                adapter.updateProperties(newProperties)  // Update the adapter with the new properties.
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load properties", Toast.LENGTH_SHORT)
                    .show()  // Show error if data fetch fails.
            }
    }

    // Handle property editing: start the EditListings activity.
    private fun editProperty(property: Property) {
        val intent = Intent(this, EditListings::class.java)
        intent.putExtra("property_id", property.id)  // Pass the property ID to the edit screen.
        startActivity(intent)
    }

    // Handle property deletion: remove the property from Firestore.
    private fun deleteProperty(property: Property) {
        db.collection("properties").document(property.id)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(this, "Property deleted", Toast.LENGTH_SHORT)
                    .show()  // Show success message.
                fetchProperties()  // Refresh the list of properties after deletion.
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to delete property", Toast.LENGTH_SHORT)
                    .show()  // Show error message.
            }
    }

    // Handle toggling the availability status of a property.
    private fun toggleAvailability(property: Property, isAvailable: Boolean) {
        db.collection("properties").document(property.id)
            .update("available", isAvailable)  // Update the "available" field in Firestore.
            .addOnSuccessListener {
                Toast.makeText(this, "Property status updated", Toast.LENGTH_SHORT)
                    .show()  // Show success message.
                fetchProperties()  // Refresh the list after status update.
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to update property status", Toast.LENGTH_SHORT)
                    .show()  // Show error message.
            }
    }

    // Function to log out the user.
    private fun signOutUser() {
        FirebaseAuth.getInstance().signOut()  // Sign out the user.
        Toast.makeText(this, "Successfully signed out", Toast.LENGTH_SHORT)
            .show()  // Show sign-out success message.

        // Redirect to Login activity after sign out.
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()  // Finish the current activity so the user cannot navigate back to this screen.
    }
}