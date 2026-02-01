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
import com.kankarej.kankarejspices.navigation.Routes
import com.kankarej.kankarejspices.screens.tabs.ProductGridItem
import com.kankarej.kankarejspices.ui.theme.SkeletonProductItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryProductScreen(navController: NavController, categoryName: String) {
    val repo = remember { ProductRepository() }
    val allProducts by repo.getProductsFlow().collectAsState(initial = emptyList())

    val categoryProducts by remember(allProducts, categoryName) {
        derivedStateOf { 
            allProducts.filter { it.category.equals(categoryName, ignoreCase = true) }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(categoryName, color = MaterialTheme.colorScheme.onSurface) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = MaterialTheme.colorScheme.onSurface)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { padding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(16.dp),
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            if (allProducts.isEmpty()) {
                items(6) { SkeletonProductItem() }
            } else {
                items(categoryProducts) { product ->
                    ProductGridItem(product) {
                        navController.navigate(Routes.PRODUCT_DETAIL.replace("{productName}", product.name))
                    }
                }
            }
        }
    }
}