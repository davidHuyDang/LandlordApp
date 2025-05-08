package com.example.landlord_app_10

import android.Manifest
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.landlord_app_10.databinding.ActivityCreateListingsBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Locale

class CreateListingActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCreateListingsBinding // Binding to access UI elements
    private val db = FirebaseFirestore.getInstance() // Firestore instance for saving data
    private lateinit var fusedLocationClient: FusedLocationProviderClient // Client for location services
    private lateinit var locationCallback: LocationCallback // Callback for receiving location updates
    private var currentLatitude: Double? = null // Variable to store current latitude
    private var currentLongitude: Double? = null // Variable to store current longitude

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateListingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize the fused location client
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Set listener for "Use Current Location" button
        binding.btnUseCurrentLocation.setOnClickListener {
            if (checkLocationPermission()) { // Check if location permission is granted
                startLocationUpdates() // Start location updates if permission is granted
            } else {
                requestLocationPermission() // Request location permission if not granted
            }
        }

        binding.btnCreate.setOnClickListener {
            saveProperty() // Save the property to Firestore
        }

        setupLocationCallback() // Initialize the location callback
    }

    // Set up the callback to handle location updates
    private fun setupLocationCallback() {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                for (location in locationResult.locations) {
                    updateLocationUI(location) // Update UI with new location data
                    stopLocationUpdates() // Stop updates after getting current location
                }
            }
        }
    }

    // Update the UI with the latest location data
    private fun updateLocationUI(location: Location) {
        currentLatitude = location.latitude // Store latitude
        currentLongitude = location.longitude // Store longitude

        // Retrieve and display the address corresponding to the current location
        getAddressFromLocation(location.latitude, location.longitude)
    }

    // Start location updates
    private fun startLocationUpdates() {
        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            5000
        ) // High accuracy location updates
            .setMinUpdateIntervalMillis(2000) // Minimum interval for updates
            .build()

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        // Request location updates
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
    }

    // Stop location updates
    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    // Retrieve and display the address from latitude and longitude
    private fun getAddressFromLocation(latitude: Double, longitude: Double) {
        val geocoder =
            Geocoder(this, Locale.getDefault()) // Geocoder to convert coordinates to address
        try {
            val searchResults: List<Address>? = geocoder.getFromLocation(latitude, longitude, 1)

            if (searchResults.isNullOrEmpty()) {
                return
            }

            val address = searchResults[0].getAddressLine(0) // Get the address line
            binding.etAddress.setText(address) // Display address in UI

            // Update latitude and longitude variables
            currentLatitude = latitude
            currentLongitude = longitude

        } catch (e: Exception) {
        }
    }

    // Save the property data to Firestore
    private fun saveProperty() {
        val propertyData: MutableMap<String, Any> = HashMap()

        // Get current user's ID
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        // Retrieve and populate property data from UI
        propertyData["price"] = binding.etPrice.text.toString().toDoubleOrNull() ?: 0.0
        propertyData["bedrooms"] = binding.etBedrooms.text.toString().toIntOrNull() ?: 0
        propertyData["imageUrl"] = binding.etImageUrl.text.toString()

        // Ensure address is included
        val address = binding.etAddress.text.toString()
        if (address.isNotEmpty()) propertyData["address"] = address

        // Add user ID to property data
        propertyData["userId"] = userId

        // Add latitude and longitude if available
        currentLatitude?.let { propertyData["latitude"] = it }
        currentLongitude?.let { propertyData["longitude"] = it }

        try {
            // Validate data and save to Firestore
            if (propertyData["address"] != null && propertyData["price"] as Double > 0 && propertyData["bedrooms"] as Int > 0) {
                db.collection("properties")
                    .add(propertyData)
                    .addOnSuccessListener { docRef ->
                        finish() // Close the activity
                    }
                    .addOnFailureListener { ex ->
                        Toast.makeText(this, "Failed to save property.", Toast.LENGTH_SHORT).show()
                    }
            } else {
                Toast.makeText(this, "Please fill all fields correctly.", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Log.e("PropertySave", "Invalid property data: ${e.message}")
            Toast.makeText(this, "Invalid property data.", Toast.LENGTH_SHORT).show()
        }
    }

    // Check if location permission is granted
    private fun checkLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    // Request location permission
    private fun requestLocationPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            LOCATION_PERMISSION_REQUEST_CODE
        )
    }

    // Handle permission request result
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startLocationUpdates()
            } else {
                Toast.makeText(
                    this,
                    "Location permission denied. Please enable it in settings.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    // Resume location updates when the activity resumes
    override fun onResume() {
        super.onResume()
        if (checkLocationPermission()) {
            startLocationUpdates()
        }
    }

    // Stop location updates when the activity pauses
    override fun onPause() {
        super.onPause()
        stopLocationUpdates()
    }

    // Companion object to define constants
    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE =
            1 // Request code for location permission
    }
}
