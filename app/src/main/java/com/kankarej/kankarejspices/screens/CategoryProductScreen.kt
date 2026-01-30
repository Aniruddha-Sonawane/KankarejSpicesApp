package com.kankarej.kankarejspices.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.kankarej.kankarejspices.data.ProductRepository
import com.kankarej.kankarejspices.model.Product
import com.kankarej.kankarejspices.navigation.Routes
import com.kankarej.kankarejspices.screens.tabs.ProductGridItem
import com.kankarej.kankarejspices.ui.theme.KankarejGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryProductScreen(navController: NavController, categoryName: String) {
    val repo = remember { ProductRepository() }
    var products by remember { mutableStateOf<List<Product>>(emptyList()) }

    LaunchedEffect(categoryName) {
        // Fetch products filtered by category
        // Note: For simplicity, fetching page 0 with high limit or modifying repo to get all by category
        // Reusing paginated method but effectively acting as a filter
        products = repo.getProductsPaged(0, 100, categoryName)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(categoryName, color = Color.Black) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.Black)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { padding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(16.dp),
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(Color.White)
        ) {
            items(products) { product ->
                ProductGridItem(product) {
                    navController.navigate(Routes.PRODUCT_DETAIL.replace("{productName}", product.name))
                }
            }
        }
    }
}