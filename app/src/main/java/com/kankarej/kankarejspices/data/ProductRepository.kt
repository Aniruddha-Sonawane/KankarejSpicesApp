package com.kankarej.kankarejspices.data

import com.google.firebase.database.FirebaseDatabase
import com.kankarej.kankarejspices.model.Product
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout

class ProductRepository {

    private val databaseUrl =
        "https://kankarej-spices-default-rtdb.asia-southeast1.firebasedatabase.app"

    private val db = FirebaseDatabase
        .getInstance(databaseUrl)
        .reference
        .child("products")

    suspend fun getProducts(): List<Product> {
        return try {
            // ⏱ Prevent infinite suspension if Firebase never responds
            val snapshot = withTimeout(8_000) {
                db.get().await()
            }

            snapshot.children.mapNotNull { child ->
                child.getValue(Product::class.java)
            }
        } catch (e: Exception) {
            // Important for debugging visibility
            e.printStackTrace()
            throw e
        }
    }
}
