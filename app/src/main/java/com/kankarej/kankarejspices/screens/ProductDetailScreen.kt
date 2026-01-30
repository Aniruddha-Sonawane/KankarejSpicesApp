package com.kankarej.kankarejspices.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.kankarej.kankarejspices.data.ProductRepository
import com.kankarej.kankarejspices.model.Product
import com.kankarej.kankarejspices.ui.theme.KankarejGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(navController: NavController, productName: String) {
    val repo = remember { ProductRepository() }
    var product by remember { mutableStateOf<Product?>(null) }

    LaunchedEffect(productName) {
        product = repo.getProductByName(productName)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(product?.name ?: "Details", color = Color.Black) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.Black)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            Button(
                onClick = { /* TODO: Add to cart */ },
                colors = ButtonDefaults.buttonColors(containerColor = KankarejGreen),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .height(50.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Add to Cart", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }
    ) { padding ->
        if (product != null) {
            Column(
                modifier = Modifier
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .fillMaxSize()
                    .background(Color.White)
            ) {
                AsyncImage(
                    model = product!!.imageUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                )

                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = product!!.category,
                        color = KankarejGreen,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text(
                        text = product!!.name,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "₹${product!!.price}",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = KankarejGreen
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = "Description",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    Text(
                        text = "This is a premium quality ${product!!.name} sourced directly from the best farms. It adds authentic flavor and aroma to your dishes.",
                        color = Color.DarkGray,
                        lineHeight = 22.sp
                    )
                }
            }
        } else {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = KankarejGreen)
            }
        }
    }
}