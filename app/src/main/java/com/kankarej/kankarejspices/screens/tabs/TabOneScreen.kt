package com.kankarej.kankarejspices.screens.tabs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState // <--- ADDED THIS IMPORT
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.kankarej.kankarejspices.data.ProductRepository
import com.kankarej.kankarejspices.model.Category
import com.kankarej.kankarejspices.model.Product
import com.kankarej.kankarejspices.navigation.Routes
import com.kankarej.kankarejspices.ui.theme.KankarejGreen
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun TabOneScreen(rootNav: NavController) {
    val repo = remember { ProductRepository() }
    val scope = rememberCoroutineScope()
    
    // Data State
    var categories by remember { mutableStateOf<List<Category>>(emptyList()) }
    var products by remember { mutableStateOf<List<Product>>(emptyList()) }
    
    // Pagination State
    var offset by remember { mutableIntStateOf(0) }
    var loading by remember { mutableStateOf(false) }
    var endReached by remember { mutableStateOf(false) }
    val PAGE_SIZE = 20

    // Initial Load
    LaunchedEffect(Unit) {
        loading = true
        val cats = repo.getCategories()
        val prods = repo.getProductsPaged(0, PAGE_SIZE)
        categories = cats
        products = prods
        offset = prods.size
        loading = false
    }

    val gridState = rememberLazyGridState()
    
    // Infinite Scroll Logic
    val shouldLoadMore = remember {
        derivedStateOf {
            val lastVisibleItem = gridState.layoutInfo.visibleItemsInfo.lastOrNull()
            lastVisibleItem != null && lastVisibleItem.index >= products.size - 4 // Load when near bottom
        }
    }

    LaunchedEffect(shouldLoadMore.value) {
        if (shouldLoadMore.value && !loading && !endReached) {
            loading = true
            val newItems = repo.getProductsPaged(offset, PAGE_SIZE)
            if (newItems.isEmpty()) {
                endReached = true
            } else {
                products = products + newItems
                offset += newItems.size
            }
            loading = false
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.White)) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            state = gridState,
            contentPadding = PaddingValues(bottom = 80.dp), // Space for bottom nav
            modifier = Modifier.fillMaxSize()
        ) {
            
            // 1. Search Bar (Full Width)
            item(span = { GridItemSpan(2) }) {
                HomeSearchBar(onClick = { rootNav.navigate(Routes.SEARCH) })
            }

            // 2. Banner (Full Width)
            if (products.isNotEmpty()) {
                item(span = { GridItemSpan(2) }) {
                    AutoScrollBanner(products.take(5))
                }
            }

            // 3. Categories (Full Width - Horizontal Scroll)
            if (categories.isNotEmpty()) {
                item(span = { GridItemSpan(2) }) {
                    CategorySection(categories) { catName ->
                        rootNav.navigate(Routes.CATEGORY_LIST.replace("{categoryName}", catName))
                    }
                }
            }
            
            // 4. Section Title
            item(span = { GridItemSpan(2) }) {
                Text(
                    text = "Featured Products",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = KankarejGreen
                    ),
                    modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 8.dp)
                )
            }

            // 5. Product Grid (2 Columns)
            items(products) { product ->
                ProductGridItem(product) {
                    rootNav.navigate(Routes.PRODUCT_DETAIL.replace("{productName}", product.name))
                }
            }

            // 6. Loader at bottom
            if (loading) {
                item(span = { GridItemSpan(2) }) {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = KankarejGreen)
                    }
                }
            }
        }
    }
}

// --- COMPONENTS ---

@Composable
fun HomeSearchBar(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .shadow(4.dp, RoundedCornerShape(50))
            .background(Color.White, RoundedCornerShape(50))
            .border(1.dp, KankarejGreen.copy(alpha = 0.3f), RoundedCornerShape(50))
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Search, contentDescription = null, tint = KankarejGreen)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Search products...", color = Color.Gray)
        }
    }
}

@Composable
fun AutoScrollBanner(products: List<Product>) {
    val listState = rememberLazyListState()
    
    // Auto Scroll Logic
    LaunchedEffect(Unit) {
        while(true) {
            delay(3000)
            if (listState.firstVisibleItemIndex < products.size - 1) {
                listState.animateScrollToItem(listState.firstVisibleItemIndex + 1)
            } else {
                listState.scrollToItem(0)
            }
        }
    }

    LazyRow(
        state = listState,
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(products) { product ->
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(product.imageUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .width(300.dp)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.dp, Color.LightGray, RoundedCornerShape(16.dp))
            )
        }
    }
}

@Composable
fun CategorySection(categories: List<Category>, onCategoryClick: (String) -> Unit) {
    Column {
        Text(
            text = "Categories",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = KankarejGreen),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(categories) { category ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.clickable { onCategoryClick(category.name) }
                ) {
                    AsyncImage(
                        model = category.imageUrl,
                        contentDescription = category.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(70.dp)
                            .clip(CircleShape)
                            .border(2.dp, KankarejGreen, CircleShape)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = category.name,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Black,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
fun ProductGridItem(product: Product, onClick: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Column {
            Box(modifier = Modifier.height(140.dp).fillMaxWidth()) {
                AsyncImage(
                    model = product.imageUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = product.name,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.Black
                )
                Text(
                    text = product.category,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "₹${product.price}",
                        style = MaterialTheme.typography.bodyLarge.copy(color = KankarejGreen, fontWeight = FontWeight.Bold)
                    )
                    Spacer(Modifier.weight(1f))
                    Icon(Icons.Default.Star, null, tint = Color(0xFFFFD700), modifier = Modifier.size(16.dp))
                    Text(text = "${product.rating}", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}