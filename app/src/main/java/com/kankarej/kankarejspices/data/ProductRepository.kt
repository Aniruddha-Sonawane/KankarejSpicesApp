package com.kankarej.kankarejspices.data

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.kankarej.kankarejspices.model.Banner
import com.kankarej.kankarejspices.model.Category
import com.kankarej.kankarejspices.model.Product
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class ProductRepository {

    private val baseUrl = "https://kankarej-spices-default-rtdb.asia-southeast1.firebasedatabase.app"
    private val db = FirebaseDatabase.getInstance(baseUrl).reference

    // REALTIME: Fetch Banners
    fun getBannersFlow(): Flow<List<Banner>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = snapshot.children.mapNotNull { it.getValue(Banner::class.java) }
                trySend(list)
            }
            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        val ref = db.child("slidingbanner")
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    // REALTIME: Returns a Flow that updates whenever 'categories' changes in DB
    fun getCategoriesFlow(): Flow<List<Category>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = snapshot.children.mapNotNull { it.getValue(Category::class.java) }
                trySend(list)
            }
            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        val ref = db.child("categories")
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    // REALTIME: Returns a Flow that updates whenever 'products' changes in DB
    fun getProductsFlow(): Flow<List<Product>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val allProducts = mutableListOf<Product>()
                // Iterate through Categories -> Products
                for (catSnap in snapshot.children) {
                    for (prodSnap in catSnap.children) {
                        prodSnap.getValue(Product::class.java)?.let { allProducts.add(it) }
                    }
                }
                trySend(allProducts) 
            }
            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        val ref = db.child("products")
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    suspend fun getProductByName(name: String): Product? {
        val snapshot = db.child("products").get().await()
        for (catSnap in snapshot.children) {
            for (prodSnap in catSnap.children) {
                val p = prodSnap.getValue(Product::class.java)
                if (p?.name == name) return p
            }
        }
        return null
    }

    suspend fun searchProducts(query: String): List<Product> {
        val snapshot = db.child("products").get().await()
        val all = mutableListOf<Product>()
        for (catSnap in snapshot.children) {
            for (prodSnap in catSnap.children) {
                val p = prodSnap.getValue(Product::class.java)
                if (p != null && p.name.contains(query, ignoreCase = true)) {
                    all.add(p)
                }
            }
        }
        return all
    }
}