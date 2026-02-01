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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.kankarej.kankarejspices.data.ProductRepository
import com.kankarej.kankarejspices.model.Product
import com.kankarej.kankarejspices.ui.theme.KankarejGreen
import com.kankarej.kankarejspices.ui.theme.shimmerEffect
import com.kankarej.kankarejspices.util.getOptimizedUrl

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
                modifier = Modifier.shadow(4.dp),
                title = { Text(product?.name ?: "Loading...", color = MaterialTheme.colorScheme.onSurface) },
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
        },
        bottomBar = {
            if (product != null) {
                Surface(modifier = Modifier.shadow(8.dp), color = MaterialTheme.colorScheme.surface) {
                    Button(
                        onClick = { /* TODO: Add to cart */ },
                        colors = ButtonDefaults.buttonColors(containerColor = KankarejGreen),
                        modifier = Modifier.fillMaxWidth().padding(16.dp).height(50.dp),
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
                .background(MaterialTheme.colorScheme.background)
        ) {
            if (product != null) {
                SubcomposeAsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(getOptimizedUrl(product!!.imageUrl, width = 800))
                        .crossfade(true)
                        .build(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxWidth().height(300.dp),
                    loading = { Box(Modifier.fillMaxSize().shimmerEffect()) },
                    error = { Box(Modifier.fillMaxSize().background(Color.LightGray)) }
                )

                Column(modifier = Modifier.padding(16.dp)) {
                    Text(product!!.category, color = KankarejGreen, fontWeight = FontWeight.Bold)
                    Text(
                        text = product!!.name, 
                        fontSize = 24.sp, 
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.height(8.dp))
                    Text("₹${product!!.price}", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = KankarejGreen)
                    Spacer(Modifier.height(16.dp))
                    Text("Description", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurface)
                    Text(
                        "This is a premium quality ${product!!.name} sourced directly from the best farms.",
                        color = if(MaterialTheme.colorScheme.background == Color.White) Color.DarkGray else Color.LightGray, 
                        lineHeight = 22.sp
                    )
                }
            } else {
                Box(Modifier.fillMaxWidth().height(300.dp).shimmerEffect())
                Column(modifier = Modifier.padding(16.dp)) {
                    Box(Modifier.width(100.dp).height(20.dp).shimmerEffect())
                    Spacer(Modifier.height(8.dp))
                    Box(Modifier.fillMaxWidth(0.7f).height(30.dp).shimmerEffect())
                    Spacer(Modifier.height(8.dp))
                    Box(Modifier.width(80.dp).height(24.dp).shimmerEffect())
                }
            }
        }
    }
}