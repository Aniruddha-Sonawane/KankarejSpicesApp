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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
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
import com.kankarej.kankarejspices.ui.theme.shimmerEffect

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(navController: NavController, productName: String) {
    val repo = remember { ProductRepository() }
    var product by remember { mutableStateOf<Product?>(null) }
    
    // Simulate slight delay to show skeleton if needed, or just fetch
    LaunchedEffect(productName) {
        product = repo.getProductByName(productName)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                modifier = Modifier.shadow(4.dp), // Shadow
                title = { Text(product?.name ?: "Loading...", color = Color.Black) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.Black)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            if (product != null) {
                Surface(modifier = Modifier.shadow(8.dp), color = Color.White) {
                    Button(
                        onClick = { /* TODO */ },
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
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .fillMaxSize()
                .background(Color.White)
        ) {
            if (product != null) {
                // REAL CONTENT
                AsyncImage(
                    model = product!!.imageUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxWidth().height(300.dp)
                )

                Column(modifier = Modifier.padding(16.dp)) {
                    Text(product!!.category, color = KankarejGreen, fontWeight = FontWeight.Bold)
                    Text(product!!.name, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    Text("₹${product!!.price}", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = KankarejGreen)
                    Spacer(Modifier.height(16.dp))
                    Text("Description", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text(
                        "This is a premium quality ${product!!.name} sourced directly from the best farms.",
                        color = Color.DarkGray, lineHeight = 22.sp
                    )
                }
            } else {
                // SKELETON CONTENT
                Box(Modifier.fillMaxWidth().height(300.dp).shimmerEffect())
                Column(modifier = Modifier.padding(16.dp)) {
                    Box(Modifier.width(100.dp).height(20.dp).clip(RoundedCornerShape(4.dp)).shimmerEffect())
                    Spacer(Modifier.height(8.dp))
                    Box(Modifier.fillMaxWidth(0.7f).height(30.dp).clip(RoundedCornerShape(4.dp)).shimmerEffect())
                    Spacer(Modifier.height(8.dp))
                    Box(Modifier.width(80.dp).height(24.dp).clip(RoundedCornerShape(4.dp)).shimmerEffect())
                    Spacer(Modifier.height(24.dp))
                    Box(Modifier.fillMaxWidth().height(100.dp).clip(RoundedCornerShape(4.dp)).shimmerEffect())
                }
            }
        }
    }
}