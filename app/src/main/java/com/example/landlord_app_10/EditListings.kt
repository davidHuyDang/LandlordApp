package com.example.landlord_app_10

import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.landlord_app_10.databinding.ActivityEditPropertyBinding
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Locale

// Activity for editing existing property listings
class EditListings : AppCompatActivity() {
    private lateinit var binding: ActivityEditPropertyBinding
    private val db = FirebaseFirestore.getInstance()
    private lateinit var geocoder: Geocoder

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditPropertyBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Geocoder
        geocoder = Geocoder(applicationContext, Locale.getDefault())

        // Get the property ID passed via intent from the previous activity
        val propertyId = intent.getStringExtra("property_id") ?: return

        // Fetch current property data using the property ID
        fetchPropertyData(propertyId)

        // Set listener for "Save" button to update the property
        binding.btnSave.setOnClickListener {
            val address = binding.etAddress.text.toString() // Get updated address
            val price = binding.etPrice.text.toString().toDoubleOrNull() // Get updated price
            val bedrooms =
                binding.etBedrooms.text.toString().toIntOrNull() // Get updated bedrooms count
            val imageUrl = binding.etImageUrl.text.toString() // Get updated image URL
            val available = binding.switchAvailable.isChecked // Get updated availability status

            // Validate inputs and proceed with update
            if (address.isNotEmpty() && price != null && bedrooms != null) {
                // Get latitude and longitude for the new address
                getCoordinates(address) { latLng ->
                    if (latLng != null) {
                        val updatedProperty: MutableMap<String, Any> =
                            HashMap() // Data structure for updated property
                        updatedProperty["address"] = address
                        updatedProperty["price"] = price
                        updatedProperty["bedrooms"] = bedrooms
                        updatedProperty["imageUrl"] = imageUrl
                        updatedProperty["available"] = available
                        updatedProperty["latitude"] = latLng.latitude
                        updatedProperty["longitude"] = latLng.longitude

                        // Update the property in Firestore
                        db.collection("properties")
                            .document(propertyId)
                            .update(updatedProperty)
                            .addOnSuccessListener {
                                Toast.makeText(this, "Property updated", Toast.LENGTH_SHORT).show()
                                finish() // Close the activity after update
                            }
                            .addOnFailureListener { ex ->
                                Toast.makeText(
                                    this,
                                    "Failed to update property: ${ex.message}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                    } else {
                        Toast.makeText(
                            this,
                            "Unable to get location for address",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } else {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Fetch property data from Firestore and populate UI fields
    private fun fetchPropertyData(propertyId: String) {
        db.collection("properties").document(propertyId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val property = document.data // Get property data as a map
                    binding.etAddress.setText(property?.get("address") as? String) // Set address field
                    binding.etPrice.setText(property?.get("price").toString()) // Set price field
                    binding.etBedrooms.setText(
                        property?.get("bedrooms").toString()
                    ) // Set bedrooms field
                    binding.etImageUrl.setText(property?.get("imageUrl") as? String) // Set image URL field
                    binding.switchAvailable.isChecked =
                        property?.get("available") as? Boolean ?: true // Set availability
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load property data", Toast.LENGTH_SHORT).show()
            }
    }

    // Method to get latitude and longitude from address using Geocoder API
    private fun getCoordinates(address: String, callback: (LatLng?) -> Unit) {
        try {
            val searchResults: List<Address>? =
                geocoder.getFromLocationName(address, 1) // Get address

            // Check for valid results
            if (searchResults == null || searchResults.isEmpty()) {
                callback(null) // No results found
                return
            }

            // Get the first result and extract coordinates
            val foundAddress = searchResults[0]
            val latLng = LatLng(foundAddress.latitude, foundAddress.longitude)

            // Return the coordinates
            callback(latLng)

        } catch (ex: Exception) {
            ex.printStackTrace()
            callback(null) // Handle error, return null
        }
    }
}