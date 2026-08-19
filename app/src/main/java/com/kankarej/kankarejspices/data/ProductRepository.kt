package com.kankarej.kankarejspices.data

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.kankarej.kankarejspices.model.Banner
import com.kankarej.kankarejspices.model.Category
import com.kankarej.kankarejspices.model.ContactInfo
import com.kankarej.kankarejspices.model.ContactPerson
import com.kankarej.kankarejspices.model.Product
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class ProductRepository {

    private val baseUrl = "https://kankarej-spices-default-rtdb.asia-southeast1.firebasedatabase.app"
    private val db = FirebaseDatabase.getInstance(baseUrl).reference

    // Reads a Firebase child as a String no matter how it was actually typed
    // in the console. If someone enters a phone number without quotes,
    // Firebase stores it as a Long/Double instead of a String, and calling
    // getValue(String::class.java) on that throws a DatabaseException that
    // crashes the whole listener. Reading it as Any and converting avoids
    // that entirely, regardless of what type ends up in the DB.
    private fun DataSnapshot.stringValue(childKey: String): String {
        return try {
            child(childKey).getValue(Any::class.java)?.toString() ?: ""
        } catch (e: Exception) {
            ""
        }
    }

    // REALTIME: Fetch Contact Info
    fun getContactInfoFlow(): Flow<ContactInfo> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val gstin = snapshot.stringValue("gstin")
                val fssai = snapshot.stringValue("fssai")
                val address = snapshot.stringValue("address")
                // Digits-only WhatsApp number with country code, e.g. "919876543210".
                // Safe even if it was typed as a plain number in the Firebase console.
                val whatsapp = snapshot.stringValue("whatsapp")

                val teamList = mutableListOf<ContactPerson>()
                snapshot.child("team").children.forEach { child ->
                    try {
                        child.getValue(ContactPerson::class.java)?.let { teamList.add(it) }
                    } catch (e: Exception) {
                        // Skip a malformed team entry instead of crashing the whole screen.
                    }
                }

                trySend(ContactInfo(gstin, fssai, address, teamList, whatsapp))
            }
            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        val ref = db.child("contact_info")
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

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
                        try {
                            prodSnap.getValue(Product::class.java)?.let { allProducts.add(it) }
                        } catch (e: Exception) {
                            // Skip a malformed product entry instead of crashing the whole list.
                        }
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
                val p = try {
                    prodSnap.getValue(Product::class.java)
                } catch (e: Exception) {
                    null
                }
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
                val p = try {
                    prodSnap.getValue(Product::class.java)
                } catch (e: Exception) {
                    null
                }
                if (p != null && p.name.contains(query, ignoreCase = true)) {
                    all.add(p)
                }
            }
        }
        return all
    }
}