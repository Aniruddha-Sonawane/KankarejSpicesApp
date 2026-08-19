package com.kankarej.kankarejspices.model

data class Product(
    val name: String = "",
    val price: Int = 0,
    val category: String = "",
    val rating: Double = 0.0,
    val imageUrl: String = "",
    val quantity: String = "",
    // NEW: editable straight from Firebase (products/{category}/{product}/description).
    // Falls back to a generic sentence in the UI if left blank.
    val description: String = ""
)