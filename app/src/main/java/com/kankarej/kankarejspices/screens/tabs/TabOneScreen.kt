package com.kankarej.kankarejspices.screens.tabs

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
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
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.compose.SubcomposeAsyncImage // NEW: Allows loading state
import coil.imageLoader
import coil.request.ImageRequest
import com.kankarej.kankarejspices.data.ProductRepository
import com.kankarej.kankarejspices.model.Category
import com.kankarej.kankarejspices.model.Product
import com.kankarej.kankarejspices.navigation.Routes
import com.kankarej.kankarejspices.ui.theme.KankarejGreen
import com.kankarej.kankarejspices.ui.theme.SkeletonProductItem
import com.kankarej.kankarejspices.ui.theme.shimmerEffect
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun TabOneScreen(rootNav: NavController) {
    val repo = remember { ProductRepository() }
    val context = LocalContext.current
    
    // REALTIME FLOWS
    val categories by repo.getCategoriesFlow().collectAsState(initial = emptyList())
    val allProducts by repo.getProductsFlow().collectAsState(initial = emptyList())
    
    var displayedCount by remember { mutableIntStateOf(20) }
    
    val displayedProducts by remember(allProducts, displayedCount) {
        derivedStateOf { allProducts.take(displayedCount) }
    }

    // --- AGGRESSIVE PRELOADING (FIRST 20) ---
    LaunchedEffect(allProducts) {
        if (allProducts.isNotEmpty()) {
            // Preload 20 items immediately
            allProducts.take(20).forEach { product ->
                val request = ImageRequest.Builder(context)
                    .data(product.imageUrl)
                    .size(600) // Optimization: Ask for smaller size if possible (server dependent)
                    .build()
                context.imageLoader.enqueue(request)
            }
        }
    }

    val gridState = rememberLazyGridState()
    val shouldLoadMore by remember {
        derivedStateOf {
            val lastVisible = gridState.layoutInfo.visibleItemsInfo.lastOrNull()
            lastVisible != null && lastVisible.index >= displayedProducts.size - 4
        }
    }

    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore && displayedCount < allProducts.size) {
            displayedCount += 20
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                modifier = Modifier.shadow(4.dp),
                title = { 
                    Text("Kankarej Spices", fontWeight = FontWeight.Bold, color = KankarejGreen) 
                },
                actions = {
                    IconButton(onClick = { rootNav.navigate(Routes.SEARCH) }) {
                        Icon(Icons.Default.Search, "Search", tint = KankarejGreen)
                    }
                    IconButton(onClick = { rootNav.navigate(Routes.MODAL) }) {
                        Icon(Icons.Default.Info, "Info", tint = KankarejGreen)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues).background(Color.White)) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                state = gridState,
                contentPadding = PaddingValues(bottom = 80.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                if (allProducts.isEmpty()) {
                    items(8) { SkeletonProductItem() }
                } else {
                    item(span = { GridItemSpan(2) }) {
                        FullWidthBannerPager(allProducts.take(5))
                    }

                    if (categories.isNotEmpty()) {
                        item(span = { GridItemSpan(2) }) {
                            CategorySection(categories) { catName ->
                                rootNav.navigate(Routes.CATEGORY_LIST.replace("{categoryName}", catName))
                            }
                        }
                    }
                    
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

                    items(displayedProducts) { product ->
                        ProductGridItem(product) {
                            rootNav.navigate(Routes.PRODUCT_DETAIL.replace("{productName}", product.name))
                        }
                    }
                    
                    if (displayedCount < allProducts.size) {
                         item(span = { GridItemSpan(2) }) {
                            Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(color = KankarejGreen)
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- UPDATED COMPONENTS ---

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FullWidthBannerPager(products: List<Product>) {
    val startIndex = Int.MAX_VALUE / 2
    val pagerState = rememberPagerState(initialPage = startIndex, pageCount = { Int.MAX_VALUE })
    
    LaunchedEffect(Unit) {
        while (true) {
            delay(10_000)
            try { pagerState.animateScrollToPage(pagerState.currentPage + 1) } catch (_: Exception) {}
        }
    }

    Box(modifier = Modifier.fillMaxWidth().height(220.dp).background(Color(0xFFEEEEEE))) {
        HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
            val product = products[page % products.size]
            SubcomposeAsyncImage( // Better loading handling
                model = ImageRequest.Builder(LocalContext.current)
                    .data(product.imageUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
                loading = { 
                    Box(Modifier.fillMaxSize().shimmerEffect()) // Shimmer while loading banner
                }
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
                    SubcomposeAsyncImage(
                        model = category.imageUrl,
                        contentDescription = category.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(70.dp)
                            .clip(CircleShape)
                            .border(2.dp, KankarejGreen, CircleShape),
                        loading = { Box(Modifier.fillMaxSize().background(Color(0xFFEEEEEE))) }
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
                // Use SubcomposeAsyncImage to show a placeholder color/shimmer
                SubcomposeAsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(product.imageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                    loading = {
                        // While image downloads, show this nice gray box
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color(0xFFF0F0F0))
                                .shimmerEffect() 
                        )
                    },
                    error = {
                        // If download fails, show a gray box with an icon (optional)
                        Box(Modifier.fillMaxSize().background(Color.LightGray))
                    }
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