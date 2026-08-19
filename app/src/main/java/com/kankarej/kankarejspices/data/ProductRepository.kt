package com.kankarej.kankarejspices.data

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.kankarej.kankarejspices.model.AppSettings
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

    private val baseUrl =
        "https://kankarej-spices-default-rtdb.asia-southeast1.firebasedatabase.app"

    private val db =
        FirebaseDatabase.getInstance(baseUrl).reference

    private fun DataSnapshot.stringValue(
        childKey: String
    ): String {
        return try {
            child(childKey)
                .getValue(Any::class.java)
                ?.toString()
                ?: ""
        } catch (e: Exception) {
            ""
        }
    }

    fun getAppSettingsFlow(): Flow<AppSettings> =
        callbackFlow {

            val listener = object : ValueEventListener {

                override fun onDataChange(
                    snapshot: DataSnapshot
                ) {
                    val settings = AppSettings(
                        appLink = snapshot
                            .stringValue("app_link")
                            .trim(),

                        rateUsLink = snapshot
                            .stringValue("rate_us_link")
                            .trim(),

                        privacyPolicyLink = snapshot
                            .stringValue("privacy_policy_link")
                            .trim()
                    )

                    trySend(settings)
                }

                override fun onCancelled(
                    error: DatabaseError
                ) {
                    close(error.toException())
                }
            }

            val ref = db.child("settings")

            ref.addValueEventListener(listener)

            awaitClose {
                ref.removeEventListener(listener)
            }
        }

    fun getContactInfoFlow(): Flow<ContactInfo> =
        callbackFlow {

            val listener = object : ValueEventListener {

                override fun onDataChange(
                    snapshot: DataSnapshot
                ) {
                    val gstin =
                        snapshot.stringValue("gstin")

                    val fssai =
                        snapshot.stringValue("fssai")

                    val address =
                        snapshot.stringValue("address")

                    val whatsapp =
                        snapshot.stringValue("whatsapp")

                    val phone =
                        snapshot.stringValue("phone")

                    val teamList =
                        mutableListOf<ContactPerson>()

                    snapshot
                        .child("team")
                        .children
                        .forEach { child ->

                            try {
                                child
                                    .getValue(
                                        ContactPerson::class.java
                                    )
                                    ?.let {
                                        teamList.add(it)
                                    }
                            } catch (e: Exception) {
                                // Ignore malformed team entries.
                            }
                        }

                    trySend(
                        ContactInfo(
                            gstin = gstin,
                            fssai = fssai,
                            address = address,
                            teamList = teamList,
                            whatsappNumber = whatsapp,
                            phoneNumber = phone
                        )
                    )
                }

                override fun onCancelled(
                    error: DatabaseError
                ) {
                    close(error.toException())
                }
            }

            val ref =
                db.child("contact_info")

            ref.addValueEventListener(listener)

            awaitClose {
                ref.removeEventListener(listener)
            }
        }

    fun getBannersFlow(): Flow<List<Banner>> =
        callbackFlow {

            val listener = object : ValueEventListener {

                override fun onDataChange(
                    snapshot: DataSnapshot
                ) {
                    val list =
                        snapshot.children.mapNotNull {

                            try {
                                it.getValue(
                                    Banner::class.java
                                )
                            } catch (e: Exception) {
                                null
                            }
                        }

                    trySend(list)
                }

                override fun onCancelled(
                    error: DatabaseError
                ) {
                    close(error.toException())
                }
            }

            val ref =
                db.child("slidingbanner")

            ref.addValueEventListener(listener)

            awaitClose {
                ref.removeEventListener(listener)
            }
        }

    fun getCategoriesFlow(): Flow<List<Category>> =
        callbackFlow {

            val listener = object : ValueEventListener {

                override fun onDataChange(
                    snapshot: DataSnapshot
                ) {
                    val list =
                        snapshot.children.mapNotNull {

                            try {
                                it.getValue(
                                    Category::class.java
                                )
                            } catch (e: Exception) {
                                null
                            }
                        }

                    trySend(list)
                }

                override fun onCancelled(
                    error: DatabaseError
                ) {
                    close(error.toException())
                }
            }

            val ref =
                db.child("categories")

            ref.addValueEventListener(listener)

            awaitClose {
                ref.removeEventListener(listener)
            }
        }

    fun getProductsFlow(): Flow<List<Product>> =
        callbackFlow {

            val listener = object : ValueEventListener {

                override fun onDataChange(
                    snapshot: DataSnapshot
                ) {
                    val allProducts =
                        mutableListOf<Product>()

                    for (catSnap in snapshot.children) {
                        for (prodSnap in catSnap.children) {

                            try {
                                prodSnap
                                    .getValue(Product::class.java)
                                    ?.let {
                                        allProducts.add(it)
                                    }
                            } catch (e: Exception) {
                                // Ignore malformed products.
                            }
                        }
                    }

                    trySend(allProducts)
                }

                override fun onCancelled(
                    error: DatabaseError
                ) {
                    close(error.toException())
                }
            }

            val ref =
                db.child("products")

            ref.addValueEventListener(listener)

            awaitClose {
                ref.removeEventListener(listener)
            }
        }

    suspend fun getProductByName(
        name: String
    ): Product? {

        val snapshot =
            db.child("products")
                .get()
                .await()

        for (catSnap in snapshot.children) {

            for (prodSnap in catSnap.children) {

                val product =
                    try {
                        prodSnap.getValue(
                            Product::class.java
                        )
                    } catch (e: Exception) {
                        null
                    }

                if (product?.name == name) {
                    return product
                }
            }
        }

        return null
    }

    suspend fun searchProducts(
        query: String
    ): List<Product> {

        val snapshot =
            db.child("products")
                .get()
                .await()

        val all =
            mutableListOf<Product>()

        for (catSnap in snapshot.children) {

            for (prodSnap in catSnap.children) {

                val product =
                    try {
                        prodSnap.getValue(
                            Product::class.java
                        )
                    } catch (e: Exception) {
                        null
                    }

                if (
                    product != null &&
                    product.name.contains(
                        query,
                        ignoreCase = true
                    )
                ) {
                    all.add(product)
                }
            }
        }

        return all
    }
}
