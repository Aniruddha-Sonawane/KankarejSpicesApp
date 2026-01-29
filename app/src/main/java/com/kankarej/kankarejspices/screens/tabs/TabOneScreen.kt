package com.kankarej.kankarejspices.screens.tabs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.kankarej.kankarejspices.data.ProductRepository
import com.kankarej.kankarejspices.model.Product

private val BgSecondary = Color(0xFFF5F5F5)
private val BorderColor = Color(0xFFE6E6E6)
private val TextMuted = Color(0xFF828282)

@Composable
fun TabOneScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 78.dp) // space for bottom nav
    ) {
        Spacer(Modifier.height(8.dp))
        SearchBar()
        PillsRow()
        Banner()
        CategoryRow()

        // ✅ REAL PRODUCTS FROM FIREBASE
        ProductScreen()
    }
}

/* ---------------- FIREBASE PRODUCT LIST ---------------- */

@Composable
private fun ProductScreen() {
    val repo = remember { ProductRepository() }
    var products by remember { mutableStateOf<List<Product>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        try {
            products = repo.getProducts()
        } catch (e: Exception) {
    e.printStackTrace()
    error = e.message ?: "Unknown error"
}
finally {
            isLoading = false
        }
    }

    when {
        isLoading -> {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        error != null -> {
            Text(
                text = "Failed to load products: $error",
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(16.dp)
            )
        }

        else -> {
            LazyColumn(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(products.size) { index ->
                    val product = products[index]

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AsyncImage(
                            model = product.imageUrl,
                            contentDescription = product.name,
                            modifier = Modifier
                                .size(80.dp)
                                .clip(RoundedCornerShape(8.dp))
                        )

                        Spacer(Modifier.width(12.dp))

                        Column {
                            Text(
                                text = product.name,
                                fontWeight = FontWeight.Bold
                            )
                            Text("₹${product.price}")
                            Text("Rating: ${product.rating}")
                        }
                    }
                }
            }
        }
    }
}

/* ---------------- SEARCH ---------------- */

@Composable
private fun SearchBar() {
    Row(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .height(40.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(BgSecondary)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Search,
            contentDescription = null,
            tint = TextMuted
        )
        Spacer(Modifier.width(12.dp))
        Text(
            text = "Search",
            color = TextMuted,
            fontSize = 16.sp
        )
    }
}

/* ---------------- PILLS ---------------- */

@Composable
private fun PillsRow() {
    val pills = listOf(
        "Favorites" to Icons.Default.Favorite,
        "History" to Icons.Default.History,
        "Following" to Icons.Default.Person
    )

    LazyRow(
        modifier = Modifier.padding(start = 16.dp, top = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(pills.size) { index ->
            Pill(
                label = pills[index].first,
                icon = pills[index].second
            )
        }
    }
}

@Composable
private fun Pill(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Row(
        modifier = Modifier
            .border(1.dp, BorderColor, RoundedCornerShape(6.dp))
            .padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(4.dp))
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

/* ---------------- BANNER ---------------- */

@Composable
private fun Banner() {
    Box(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 16.dp)
            .height(136.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFFECECEC))
    ) {
        Text(
            text = "Banner title",
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 20.dp),
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

/* ---------------- CATEGORY ---------------- */

@Composable
private fun CategoryRow() {
    SectionHeader("Title")

    LazyRow(
        modifier = Modifier.padding(start = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        items(4) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(76.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFEEEEEE))
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Title",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

/* ---------------- HEADER ---------------- */

@Composable
private fun SectionHeader(title: String) {
    Row(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = "›",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
