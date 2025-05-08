package com.example.landlord_app_10


import com.google.firebase.firestore.DocumentId


data class UserProfile(
    @DocumentId
    var docId: String = "",
    @JvmField
    var isLandlord: Boolean = true,
    var favColor: String = "",
    var name: String = "",
    var age: Int = 0,
    var phoneNumber: String = ""
)