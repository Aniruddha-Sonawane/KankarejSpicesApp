package com.kankarej.kankarejspices.data

import com.google.firebase.database.FirebaseDatabase
import com.kankarej.kankarejspices.model.Category
import com.kankarej.kankarejspices.model.Product
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout

class ProductRepository {

    private val baseUrl = "https://kankarej-spices-default-rtdb.asia-southeast1.firebasedatabase.app"
    private val db = FirebaseDatabase.getInstance(baseUrl).reference

    // Cache to keep order stable during pagination
    private var cachedAllProducts: List<Product>? = null
    private var cachedCategories: List<Category>? = null

    suspend fun getCategories(): List<Category> = withTimeout(10_000) {
        if (cachedCategories != null) return@withTimeout cachedCategories!!
        
        val snapshot = db.child("categories").get().await()
        val list = snapshot.children.mapNotNull { it.getValue(Category::class.java) }
        cachedCategories = list
        list
    }

    suspend fun getProductsPaged(
        offset: Int,
        limit: Int = 20,
        categoryFilter: String? = null
    ): List<Product> = withTimeout(10_000) {
        
        // 1. Fetch and Cache all products once if not already done
        if (cachedAllProducts == null) {
            val snapshot = db.child("products").get().await()
            val all = mutableListOf<Product>()
            for (catSnap in snapshot.children) {
                for (prodSnap in catSnap.children) {
                    prodSnap.getValue(Product::class.java)?.let { all.add(it) }
                }
            }
            cachedAllProducts = all.shuffled() // Shuffle once at startup
        }

        // 2. Filter
        val sourceList = if (categoryFilter != null) {
            cachedAllProducts!!.filter { it.category.equals(categoryFilter, ignoreCase = true) }
        } else {
            cachedAllProducts!!
        }

        // 3. Page
        if (offset >= sourceList.size) return@withTimeout emptyList()
        
        sourceList.drop(offset).take(limit)
    }

    suspend fun getProductByName(name: String): Product? {
        // Ideally fetch from cache first
        return cachedAllProducts?.find { it.name == name }
    }
    
    suspend fun searchProducts(query: String): List<Product> {
        if (cachedAllProducts == null) getProductsPaged(0, 1) // Ensure loaded
        return cachedAllProducts?.filter { 
            it.name.contains(query, ignoreCase = true) 
        } ?: emptyList()
    }
}