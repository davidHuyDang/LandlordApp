package com.example.landlord_app_10

data class Property(
    val id: String = "",
    val address: String = "",
    val imageUrl: String = "",
    val price: Double = 0.0,
    val bedrooms: Int = 0,
    val available: Boolean = true,
    val userId: String = "" // New field for user ID to associate with properties.
)